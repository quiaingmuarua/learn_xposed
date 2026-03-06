package com.demo.java.xposed.rcs.model;

/*

{
    "fakeSimInfo": "xx",
    "realSimInfo": "xxx",
    "result":"xxx",
    "VerifyPhoneNumberRequest":{},
    "registerStatusList":["xx"],
    "ProvisioningHttpRequest":"xxx"
}
 */

import static com.demo.java.xposed.utils.ProcessUtil.getCurrentProcessName;

import android.text.TextUtils;

import com.demo.java.xposed.device.model.SimInfoModel;
import com.demo.java.xposed.rcs.config.FilePathConfig;
import com.demo.java.xposed.rcs.enums.GlobalRcsStatusEnum;
import com.demo.java.xposed.device.PluginInit;
import com.demo.java.xposed.utils.FileUtils;
import com.demo.java.xposed.utils.collection.ListUtils;
import com.demo.java.xposed.utils.LogUtils;
import com.demo.java.xposed.utils.TimeUtils;
import com.google.gson.Gson;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class RegisterKeyInfo {

    private static final RegisterKeyInfo INSTANCE = new RegisterKeyInfo();

    private static RegisterKeyInfo historyInstance = null;

    // Member variables
    SimInfoModel fakeSimInfo;
    Map<String, String> realSimInfo;
    Map<String, String> versionConfig = new HashMap<>();
    Map<String, String> accountInfo = new HashMap<>();
    String result = "初始化";
    String xposedVersion = PluginInit.version;
    String VerifyPhoneNumberRequest;
    String strategyWay; //激活策略
    List<String> registerStatusList = new ArrayList<String>();
    String ProvisioningHttpRequest;
    Map<String, String> gmsSignMap = new HashMap<>();
    int curStatus = 0; //当前状态
    List<String> statusList = new ArrayList<>();

    //记录时间有关信息
    private Map<String, String> timeRecord = new HashMap<>();
    String DisplayRcsChatsStatus;


    // Private constructor to prevent instantiation
    private RegisterKeyInfo() {
        // Initialization code here if needed
    }

    // Public method to provide access to the singleton instance
    // 提供一个全局访问点来获取实例
    public static RegisterKeyInfo getInstance() {
        return INSTANCE;
    }


    public static boolean isRegistered() {
        return Objects.equals(getInstance().DisplayRcsChatsStatus, "AVAILABLE");
    }


    // 设置 FakeFingerprintInfo
    public void setFakeFingerprintInfo(Map<String, String> realSimInfo, SimInfoModel fakeSimInfo, Map<String, String> versionConfig) {
        // 更新 realSimInfo
        updateProperty(this.realSimInfo, realSimInfo, () -> this.realSimInfo = realSimInfo, null);

        // 更新 fakeSimInfo
        updateProperty(this.fakeSimInfo, fakeSimInfo, () -> this.fakeSimInfo = fakeSimInfo, null);

        // 更新 versionConfig
        updateProperty(this.versionConfig, versionConfig, () -> this.versionConfig = versionConfig, null);
    }

    public void setResult(String result) {
        this.result = result;
    }


    public void setAccountInfo(Map<String, String> accountInfo) {
        updateProperty(this.accountInfo, accountInfo, () -> this.accountInfo = accountInfo, null);

    }

    public void addStatusList(String newStatusStr) {
        GlobalRcsStatusEnum globalRcsStatusEnum = GlobalRcsStatusEnum.fromString(newStatusStr);
        if (globalRcsStatusEnum != null) {
            //两种状况更新，一，状态在递进，二，突然从注册后切换成未注册
            if (globalRcsStatusEnum.value > this.curStatus || (globalRcsStatusEnum.value < 200 && this.curStatus >= 200 && !Objects.equals(newStatusStr, "AVAILABLE"))) {
                LogUtils.show("addStatusList " + globalRcsStatusEnum + " value " + globalRcsStatusEnum.value);
                this.statusList.add(newStatusStr);
                this.curStatus = globalRcsStatusEnum.value;
                toLocalFile();  // 保存到本地文件

            }
        }


    }

    public void setVerifyPhoneNumberRequest(String verifyPhoneNumberRequest, String strategyWay) {
        // 更新 VerifyPhoneNumberRequest
        updateProperty(this.VerifyPhoneNumberRequest, verifyPhoneNumberRequest,
                () -> this.VerifyPhoneNumberRequest = verifyPhoneNumberRequest,
                request -> request.contains("RCS_DEFAULT_ON_LEGAL_FYI"));

        // 更新 strategyWay
        updateProperty(this.strategyWay, strategyWay, () -> this.strategyWay = strategyWay, null);


    }

    public void addGmsSignMap(String key, String value) {
        if (key == null || key.isEmpty() || value == null || value.isEmpty()) {
            return;
        }
        gmsSignMap.put(key, value);
        toLocalFile();  // 保存到本地文件
    }

    public void addRegisterStatus(String registerStatus) {
        if (registerStatus == null || registerStatus.isEmpty()) {
            return;
        }
        this.registerStatusList.add(registerStatus);
        toLocalFile();  // 保存到本地文件
    }

    public void setProvisioningHttpRequest(String provisioningHttpRequest) {
        updateProperty(this.ProvisioningHttpRequest, provisioningHttpRequest,
                () -> this.ProvisioningHttpRequest = provisioningHttpRequest,
                request -> {
                    if (!request.contains("https")) {
                        LogUtils.show("非法请求 provisioningHttpRequest " + request);
                        return false;
                    }
                    return true;
                }
        );
    }


    public void setDisplayRcsChatsStatus(String displayRcsChatsStatus) {
        updateProperty(this.DisplayRcsChatsStatus, displayRcsChatsStatus, () -> this.DisplayRcsChatsStatus = displayRcsChatsStatus, null);
        //print the status
        LogUtils.show("setDisplayRcsChatsStatus " + displayRcsChatsStatus);

    }

    public void setTimeRecord(String key, String value) {
        if (key == null || key.isEmpty() || value == null || value.isEmpty()) {
            return;
        }
        timeRecord.put(key, value);
    }


    // 通用的属性更新方法
    // 通用的属性更新方法
    private <T> void updateProperty(T currentValue, T newValue, Runnable updateAction, java.util.function.Predicate<T> condition) {
        if (newValue == null || (newValue instanceof String && ((String) newValue).isEmpty())) {
            return;
        }

        // 检查是否满足传入的条件
        if (condition != null && !condition.test(newValue)) {
            return;
        }

        if (currentValue == null || !currentValue.equals(newValue)) {
            updateAction.run();
            toLocalFile();  // 保存到本地文件
        }
    }


    // 将对象转换为 JSON 字符串的方法
    private String toJsonStr() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }

    private void restoreFromHistory() {
        LogUtils.show("start from restoreFromHistory");
        String processName = getCurrentProcessName();
        if (!TextUtils.isEmpty(processName) && !processName.contains("messaging")) {
            LogUtils.show("restoreFromHistory 当前进程不是messages进程，不写入文件 " + processName);


        }
        if (fakeSimInfo == null || fakeSimInfo.getSimCountryName() == null || fakeSimInfo.getPhoneNumber() == null) {
            return;
        }
        Gson gson = new Gson();

        FileReader reader = null;  // 文件路径
        try {
            reader = new FileReader(FilePathConfig.getRegisterPath(fakeSimInfo.getPhoneNumber(true,false)));
            historyInstance = gson.fromJson(reader, RegisterKeyInfo.class);
            LogUtils.show("restoreFromHistory back_up_from_history" + historyInstance);
        } catch (FileNotFoundException e) {
            LogUtils.show("restoreFromHistory exception" + e);
        }

    }

    public void mergeFromHistory(RegisterKeyInfo historyInstance) {
        if (historyInstance == null || !Objects.equals(fakeSimInfo.getPhoneNumber(), historyInstance.fakeSimInfo.getPhoneNumber())) {
            return;
        }
        if (!Objects.equals(historyInstance.DisplayRcsChatsStatus, "AVAILABLE")) {
            LogUtils.show("mergeFromHistory cur DisplayRcsChatsStatus " + historyInstance.DisplayRcsChatsStatus);
            return;
        }
        LogUtils.show("mergeFromHistory " + historyInstance);
        if (!historyInstance.statusList.isEmpty()) {
            // 使用 Streams 合并
            this.statusList = ListUtils.getLastNElements(Stream.concat(this.statusList.stream(), historyInstance.statusList.stream())
                    .collect(Collectors.toList()), 100);
        }
        if (!historyInstance.registerStatusList.isEmpty()) {
            this.registerStatusList = ListUtils.getLastNElements(Stream.concat(this.registerStatusList.stream(), historyInstance.registerStatusList.stream())
                    .collect(Collectors.toList()), 100);
        }
        if (TextUtils.isEmpty(this.ProvisioningHttpRequest) && !TextUtils.isEmpty(historyInstance.ProvisioningHttpRequest)) {
            this.ProvisioningHttpRequest = historyInstance.ProvisioningHttpRequest;
        }
        if (this.gmsSignMap.isEmpty() && !historyInstance.gmsSignMap.isEmpty()) {
            this.gmsSignMap = historyInstance.gmsSignMap;
        }
        LogUtils.show("mergeFromHistory merge_result " + this);

    }


    // 将 JSON 数据写入本地文件的方法，并在必要时创建目录
    private void toLocalFile() {
        try {

            //获取当前进程是否是messages进程
            String processName = getCurrentProcessName();
            if (!TextUtils.isEmpty(processName) && !processName.contains("messaging")) {
                LogUtils.show("RegisterKeyInfo 当前进程不是messages进程，不写入文件 " + processName);
                return;
            }
            if (fakeSimInfo == null || fakeSimInfo.getSimCountryName() == null || fakeSimInfo.getPhoneNumber() == null) {
                return;
            }
            if (RegisterKeyInfo.historyInstance == null) {
                restoreFromHistory();
                mergeFromHistory(historyInstance);
            }
            if (this.gmsSignMap.isEmpty()) {
                LogUtils.show("gmsSignMap is empty  us local_sig_env");
                SigEnv.saveSigEnv(null);
            }
            timeRecord.put("bootTime", TimeUtils.getBootTimeString());
            timeRecord.put("bootTimeSeconds", String.valueOf(TimeUtils.getUptimeSeconds()));
            String jsonStr = toJsonStr();


            FileUtils.writeToFile(FilePathConfig.getRegisterPath(fakeSimInfo.getPhoneNumber(true,false)), jsonStr);
            FileUtils.writeToFile(FilePathConfig.getVersionPath(), xposedVersion);


            if (GlobalRcsStatusEnum.fromValue(curStatus) != null) {
                assert GlobalRcsStatusEnum.fromValue(curStatus) != null;
                FileUtils.writeToFile(FilePathConfig.getRcsStatusPath(fakeSimInfo.getPhoneNumber(true,false)), GlobalRcsStatusEnum.fromValue(curStatus).toString());
            }

            LogUtils.show("RegisterKeyInfo toLocalFile jsonStr " + jsonStr);
            // 写入 JSON 数据到文件

        } catch (Exception e) {
            LogUtils.show("sim_register Error writing JSON to file: " + e.getLocalizedMessage());
        }
    }

}


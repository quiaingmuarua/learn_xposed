package com.demo.java.xposed.rcs.model;



import static com.demo.java.xposed.utils.ProcessUtil.getCurrentProcessName;

import android.annotation.SuppressLint;
import android.text.TextUtils;

import com.demo.java.xposed.device.model.SimInfoModel;
import com.demo.java.xposed.rcs.config.FilePathConfig;
import com.demo.java.xposed.device.PluginInit;
import com.demo.java.xposed.utils.FileUtils;
import com.demo.java.xposed.utils.LogUtils;
import com.google.gson.Gson;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

public class SendMsgKeyInfo {


    private static String baseFilePath="/data/data/com.google.android.apps.messaging/code_cache/";

    private static SendMsgKeyInfo historyInstance = null;

    private static final SendMsgKeyInfo INSTANCE = new SendMsgKeyInfo();
    public Map<String, MsgItem> msgItemMap = new HashMap<>();
    public Map<String, Integer> msgSendCountMap = new LinkedHashMap<>();
    public Map<String, String> phoneMap = new LinkedHashMap<>();
    SimInfoModel fakeSimInfo;
    String xposedVersion = PluginInit.version;
    Map<String, String> gmsSignMap = new HashMap<>();

    private Map<String,Set<String>> groupDeliveredMembers = new HashMap<>();
    public Map<String,String> phoneRcsStatusMap= new HashMap<>();

    public static int curStatus; //当前状态

    public static SendMsgKeyInfo getInstance() {
        return INSTANCE;
    }

    private SendMsgKeyInfo() {

    }

    public void addPhoneRcsStatus(String phone,String status){
        phoneRcsStatusMap.put(phone,status);
        toLocalFile();
    }

    public void setMsgItemMap(Map<String, MsgItem> msgItemMap) {
        this.msgItemMap = msgItemMap;
    }

    public void setFakeSimInfo(SimInfoModel fakeSimInfo) {
        this.fakeSimInfo = fakeSimInfo;
    }

    private void addMsgItem(MsgItem msgItem) {
        msgItemMap.put(msgItem.getMessageId(), msgItem);
    }


    public void addDeliveredMember(String groupId, String member) {
        if (groupId == null || member == null) return;

        groupDeliveredMembers
                .computeIfAbsent(groupId, k -> new TreeSet<>())  // 使用 TreeSet 去重 & 排序
                .add(member.trim());  // 去除前后空格以防冗余
    }



    public MsgItem getMsgItem(String msgId, boolean autoAdd) {
        if (!msgItemMap.containsKey(msgId) && autoAdd) {
            addMsgItem(new MsgItem(msgId));
        }
        return msgItemMap.get(msgId);
    }

    public void addGmsSignMap(String key, String value) {
        if (key == null || key.isEmpty() || value == null || value.isEmpty()) {
            return;
        }
        gmsSignMap.put(key, value);
        toLocalFile();
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
        String fileName = fakeSimInfo.getSimCountryName() + "_" + fakeSimInfo.getPhoneNumber() + "_send"+".json";
        @SuppressLint("SdCardPath")String filePath = baseFilePath+ fileName;
        FileReader reader = null;  // 文件路径
        try {
            reader = new FileReader(filePath);
            historyInstance = gson.fromJson(reader, SendMsgKeyInfo.class);
            LogUtils.show("restoreFromHistory back_up_from_history " + historyInstance);
        } catch (FileNotFoundException e) {
            LogUtils.show("restoreFromHistory exception" + e);
        }

    }

    private void mergeFromHistory(SendMsgKeyInfo historyInstance) {
        if (historyInstance == null || !Objects.equals(fakeSimInfo.getPhoneNumber(), historyInstance.fakeSimInfo.getPhoneNumber())) {
            return;
        }
        LogUtils.show("mergeFromHistory " + historyInstance);
        if (!historyInstance.msgItemMap.isEmpty()) {
            this.msgItemMap = historyInstance.msgItemMap;
        }
        if(!historyInstance.phoneRcsStatusMap.isEmpty()){
            this.phoneRcsStatusMap=historyInstance.phoneRcsStatusMap;
        }
        if (this.gmsSignMap.isEmpty() && !historyInstance.gmsSignMap.isEmpty()) {
            this.gmsSignMap = historyInstance.gmsSignMap;
        }
        //merge  groupDeliveredMembers
        if (!historyInstance.groupDeliveredMembers.isEmpty() && this.groupDeliveredMembers.isEmpty()) {
           this.groupDeliveredMembers=historyInstance.groupDeliveredMembers;
        }
        LogUtils.show("mergeFromHistory merge_result " + this);

    }


    private String toJsonStr() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }

    public void toLocalFile() {
        if (fakeSimInfo == null || fakeSimInfo.getSimCountryName() == null || fakeSimInfo.getPhoneNumber() == null) {
            return;
        }
        //获取当前进程是否是messages进程
        String processName = getCurrentProcessName();
        if (!TextUtils.isEmpty(processName) && !processName.contains("messaging")) {
            LogUtils.show("SendMsgKeyInfo 当前进程不是messages进程，不写入文件 " + processName);
            return;
        }

        if (SendMsgKeyInfo.historyInstance == null) {
            restoreFromHistory();
            mergeFromHistory(historyInstance);
        }

        msgSendCountMap.clear();
        phoneMap.clear();
        List<MsgItem> msgItemList = new ArrayList<>(msgItemMap.values());
        msgItemList.sort(Comparator.comparingLong(MsgItem::getCreateTime));

        for (MsgItem item : msgItemList) {
            String[] receiverArray = item.getReceiver().split(",");
//            LogUtils.show("SendMsgKeyInfo receiver " + Arrays.toString(receiverArray));
            msgSendCountMap.put(item.getStatus(), msgSendCountMap.getOrDefault(item.getStatus(), 0) + 1);
            for (String s : receiverArray) {
                phoneMap.put(s.trim(), item.getStatus());
            }


        }
        FileUtils.writeToFile(FilePathConfig.getVersionPath(), xposedVersion);
        String jsonStr = toJsonStr();

        FileUtils.writeToFile(FilePathConfig.getSendMsgPath(fakeSimInfo.getPhoneNumber(true,false)), jsonStr);
        LogUtils.show("SendMsgKeyInfo toLocalFile jsonStr " + jsonStr);

    }
    //MessageFlagger cancelling alert scheduled for -180 seconds later
}
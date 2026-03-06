package com.demo.java.xposed.device.model;

import android.text.TextUtils;

import com.demo.java.xposed.device.config.SimCardConfig;
import com.demo.java.xposed.rcs.model.GmsSignConfigModel;
import com.demo.java.xposed.rcs.model.RegisterKeyInfo;
import com.demo.java.xposed.rcs.model.SendMsgKeyInfo;
import com.demo.java.xposed.utils.LogUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.Map;
import java.util.Objects;

public class SimInfoModel {

    private  static GmsSignConfigModel gmsSignConfigModel=GmsSignConfigModel.getInstance();

    //mcc
    private String mcc;

    //mnc
    private String mnc;


    //simOperator
    private String simOperator;

    //simOperatorName
    private String simOperatorName;


    //carrierId
    private String carrierId;


    //simSerialNumber
    private String iccId;

    //simCountryIso
    private String simCountryIso;

    //simCountryIsoCode
    private String simCountryIsoCode;


    //SubscriberId IMSI 是由运营商分配给每个 SIM 卡的一个独特的 15 位数字串。IMSI 的格式和生成规则遵循国际电信联盟（ITU）规定的标准。
    private String subscriberId;


    private String phoneNumber;


    private String deviceId;


    public static SimInfoModel simInfoModel;


    public static SimInfoModel getInstance(String packageName) {
        if(TextUtils.isEmpty(packageName)){
            LogUtils.show("SimInfoModel packageName is empty");
        }
        return parseFromFile(packageName);
    }


    private static SimInfoModel parseFromFile(String packageName) {
        Map<String, String> simInfo = SimCardConfig.getSimInfo(packageName);
        Map<String, String> realSimInfo = SimCardConfig.getRealSimInfo(packageName);
        Map<String, String> versionConfig = SimCardConfig.getVersionConfig(packageName);

        if (simInfo.isEmpty()) {
            LogUtils.show("无法找到 simInfo !!!!! package=" + packageName + " simInfo=" + simInfo);
        }
        LogUtils.show("获取到simInfo package=" + packageName + " 结果 " + simInfo);

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        SimInfoModel simInfoModel = gson.fromJson(gson.toJson(simInfo), SimInfoModel.class);

        RegisterKeyInfo.getInstance().setFakeFingerprintInfo(realSimInfo, simInfoModel, versionConfig);
        RegisterKeyInfo.getInstance().setTimeRecord("fingerModifyTime", SimCardConfig.getFakeFingerprintTime());
        SendMsgKeyInfo.getInstance().setFakeSimInfo(simInfoModel);
        return simInfoModel;
    }


    public String getMcc() {
        return this.mcc;
    }

    public String getMnc() {
        return this.mnc;
    }

    public String getSimOperator() {
        return this.simOperator;
    }

    public String getSimOperatorName() {
        return simOperatorName;
    }

    public String getCarrierId() {
        return this.carrierId;
    }


    public String getIccId() {
        return iccId;
    }

    public String getImei() {
        return deviceId;
    }

    public String getSimCountryIsoCode() {
        return Objects.requireNonNullElse(simCountryIsoCode, "");
    }

    //获取国家名称
    public String getSimCountryName() {
        return Objects.requireNonNullElse(simCountryIso, "");
    }

    public String getSubscriberId() {
        return subscriberId;
    }

    @Override
    public String toString() {
        return "SimInfoModel{" +
                "mcc='" + mcc + '\'' +
                ", mnc='" + mnc + '\'' +
                ", simOperator='" + simOperator + '\'' +
                ", simOperatorName='" + simOperatorName + '\'' +
                ", carrierId='" + carrierId + '\'' +
                ", iccId='" + iccId + '\'' +
                ", simCountryIso='" + simCountryIso + '\'' +
                ", simCountryIsoCode='" + simCountryIsoCode + '\'' +
                ", subscriberId='" + subscriberId + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", deviceId='" + deviceId + '\'' +
                '}';
    }

    public String getPhoneNumber() {

        return "";
    }


    public String getPhoneNumber(boolean withCountryCode, boolean withPlus) {
//        return "";
       String  number = this.phoneNumber;
        if (withCountryCode) {
            number = this.simCountryIsoCode + number;
        }
        if (withPlus) {
            number = "+" + number;
        }
        return number;
    }


    public static String formatNumber(int number) {
        if (number < 10) {
            return "0" + number;
        } else {
            return String.valueOf(number);
        }
    }
}


/*
{
"getIccId": "8944110068871232646",
"getNetworkOperator": "",
"getSubscriberId": "234107633026171",
"getVoiceMailNumber": "901",
"getDeviceSoftwareVersion": "62",
"getSimState": 5,
"getGroupIdLevel1": "85FFFFFFFFFF47454E4945494E20202020202020",
"getPhoneType": 1,
"getLine1Number": "",
"getDeviceId": "352932101701942",
"getSimOperatorName": "O2",
"getVoiceMailAlphaTag": "Voicemail",
"getSimCountryIso": "gb",
"getNetworkOperatorName": "",
"getNetworkCountryIso": "",
"getNetworkType": 0,
"getSimOperator": "23410"
}


{
"getIccId": "89441000304439712635",
"getNetworkOperator": "46001",
"getSubscriberId": "234159355701263",
"getVoiceMailNumber": "121",
"getDeviceSoftwareVersion": "77",
"getSimState": 5,
"getGroupIdLevel1": "90",
"getPhoneType": 1,
"getLine1Number": "",
"getDeviceId": "351430255682944",
"getSimOperatorName": "Lebara",
"getVoiceMailAlphaTag": "语音信箱",
"getSimCountryIso": "gb",
"getNetworkOperatorName": "China Unicom",
"getNetworkCountryIso": "cn",
"getNetworkType": 9,
"getSimOperator": "23415"
}


{
"getIccId": "89441000304614049688",
"getNetworkOperator": "46001",
"getSubscriberId": "234159358764988",
"getVoiceMailNumber": "121",
"getDeviceSoftwareVersion": "34",
"getSimState": 5,
"getGroupIdLevel1": "90",
"getPhoneType": 1,
"getLine1Number": "",
"getDeviceId": "358275098209527",
"getSimOperatorName": "Lebara",
"getVoiceMailAlphaTag": "Voicemail",
"getSimCountryIso": "gb",
"getNetworkOperatorName": "CHN-UNICOM",
"getNetworkCountryIso": "cn",
"getNetworkType": 10,
"getSimOperator": "23415"
}
 */





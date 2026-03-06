package com.demo.java.xposed.rcs.model;

import android.os.Build;
import android.text.TextUtils;

import com.demo.java.xposed.utils.LogUtils;
import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SigEnv {

    public static String country = "cn";

    public static String sdkInt = String.valueOf(Build.VERSION.SDK_INT);

    public static String brand = Build.BRAND;
    public static String releaseVersion = android.os.Build.VERSION.RELEASE;

    //{\"country\":\"DE\",\"sdkInt\":\"28\",\"orderId\":\"faede0cc1b75439a869735f321a34459\",\"release\":\"9.0\",\"gradeType\":\"B\",\"channel\":\"tugezuz5cBc6mbNTv02\",\"count\":0,\"brand\":\"ALK\",\"token\":\"f12199729684b14472a45358c12f19de\"}
    private static String genSigEnv(String orderId, String token) {
        if (TextUtils.isEmpty(orderId)){
            orderId= String.valueOf(UUID.randomUUID()).replace("-", "");
        }
        if (TextUtils.isEmpty(token)){
            token = String.valueOf(UUID.randomUUID()).replace("-", "");
        }
        Gson gson = new Gson();
        Map<String, String> sigEnv = new HashMap<>();
        sigEnv.put("country", "cn");
        sigEnv.put("sdkInt", sdkInt);
        sigEnv.put("orderId", orderId);
        sigEnv.put("release", releaseVersion);
        sigEnv.put("gradeType", "B");
        sigEnv.put("channel", "self");
        sigEnv.put("brand", brand);
        sigEnv.put("token", token);
        return gson.toJson(sigEnv);

    }


    public static void saveSigEnv(ResponseData responseData){
        LogUtils.show("saveSigEnv " +responseData);
        if (responseData==null){
            RegisterKeyInfo.getInstance().addGmsSignMap("sigEnv",  SigEnv.genSigEnv(null,null));
            SendMsgKeyInfo.getInstance().addGmsSignMap("sigEnv",  SigEnv.genSigEnv(null,null));

        }else{
            RegisterKeyInfo.getInstance().addGmsSignMap("sigEnv", responseData.getData().get("sigEnv"));
            SendMsgKeyInfo.getInstance().addGmsSignMap("sigEnv", responseData.getData().get("sigEnv"));
        }
    }
}

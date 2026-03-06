package com.demo.java.xposed.rcs.config;

import android.annotation.SuppressLint;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.HashMap;
import java.util.Map;

public class FilePathConfig {
    public static final String baseFilePath = "/data/data/com.google.android.apps.messaging/code_cache/";

    public static String getRegisterPath(String phoneNumber) {

        @SuppressLint("SdCardPath") String path = baseFilePath + phoneNumber + "_register.json";
        return path;
    }

    public static String getSendMsgPath(String phoneNumber) {
        @SuppressLint("SdCardPath") String path = baseFilePath + phoneNumber + "_send.json";
        return path;
    }


    public static String getVersionPath() {
        @SuppressLint("SdCardPath") String path = baseFilePath +   "version.txt";
        return path;
    }


    //rcs_status_
    public static String getRcsStatusPath(String phoneNumber) {
        @SuppressLint("SdCardPath") String path = baseFilePath + phoneNumber + "_rcs_status";
        return path;
    }


    public static String toJSON(String phoneNumber) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        Map<String, String> filePathMap = new HashMap<>();
        filePathMap.put("register", getRegisterPath(phoneNumber));
        filePathMap.put("send", getSendMsgPath(phoneNumber));
        filePathMap.put("version", getVersionPath());
        filePathMap.put("rcs_status", getRcsStatusPath(phoneNumber));

        return gson.toJson(filePathMap);
    }

}

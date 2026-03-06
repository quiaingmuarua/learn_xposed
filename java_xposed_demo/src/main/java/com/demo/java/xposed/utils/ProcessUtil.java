package com.demo.java.xposed.utils;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.os.Build;

import java.io.BufferedReader;
import java.io.FileReader;

public class ProcessUtil {

    public static boolean isCurrentProcessMessagingApp(Context context) {
        if (context == null) {
            return false;
        }
        // 获取当前进程的名称
        String currentProcessName = getCurrentProcessName(context);
        LogUtils.show("currentProcessName =" + currentProcessName);
        return "com.google.android.apps.messaging".equals(currentProcessName);
    }


    public static String getCurrentProcessName() {
        String currentProcessName="";
        try {
            if (Build.VERSION.SDK_INT >= 28) {
                 currentProcessName = Application.getProcessName();
                LogUtils.show("currentProcessName =" + currentProcessName);

            }

            currentProcessName=getProcessName();
        } catch (Exception e) {
            LogUtils.show("isCurrentProcessMessagingApp"+ e.getMessage());

        }

        return  currentProcessName;




    }

    private static String getCurrentProcessName(Context context) {
        int pid = android.os.Process.myPid();
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningAppProcessInfo processInfo : activityManager.getRunningAppProcesses()) {
            if (processInfo.pid == pid) {
                return processInfo.processName;
            }
        }
        return null;
    }


    public static String getProcessName() {
        try {
            BufferedReader reader = new BufferedReader(new FileReader("/proc/self/cmdline"));
            String processName = reader.readLine().trim();
            reader.close();
            return processName;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
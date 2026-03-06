package com.demo.java.xposed.whatsapp;

import com.demo.java.xposed.utils.BaseHook;
import com.demo.java.xposed.utils.LogUtils;

import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class GmsIntegrityHook {

    public static void run(XC_LoadPackage.LoadPackageParam loadPackageParam) {
        LogUtils.show("Whatsapp GmsIntegrityHook run");

        ClassLoader classLoader = loadPackageParam.classLoader;
        try {
            Class<?> StandardIntegrityManagerClass = classLoader.loadClass("com.google.android.play.core.integrity.StandardIntegrityManager");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

}

package com.demo.java.xposed.whatsapp;

import com.demo.java.xposed.base.BaseAppHook;
import com.demo.java.xposed.utils.LogUtils;

import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class CommonWhatsHook extends BaseAppHook {


    public static void run(XC_LoadPackage.LoadPackageParam loadPackageParam) {
        LogUtils.show("Whatsapp CommonWhatsHook run");
        ClassLoader classLoader = loadPackageParam.classLoader;

        Class<?> LogClass = XposedHelpers.findClassIfExists("com.whatsapp.util.Log", classLoader);
        if(LogClass==null){
            LogClass=XposedHelpers.findClassIfExists("com.whatsapp.infra.logging.Log", classLoader);
        }

        if (LogClass != null) {
            XposedHelpers.setStaticIntField(LogClass, "level", 5);
            LogUtils.show("Log level is 5");
        }


    }
}

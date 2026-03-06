package com.demo.java.xposed.app.demo.geekrun;

import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class ProtectAppEnvSample  {




    public static void handleLoadPackage(XC_LoadPackage.LoadPackageParam loadPackageParam) {
        ClassLoader classLoader=loadPackageParam.classLoader;
//        JavaDeviceHook.hookAllInfo(classLoader);



    }
}

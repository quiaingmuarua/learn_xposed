package com.demo.java.xposed.app.twitter;

import android.app.Application;
import android.content.Context;

import com.demo.java.xposed.base.BaseAppHook;
import com.demo.java.xposed.system.OkhttpHook;
import com.demo.java.xposed.utils.LogUtils;
import com.demo.java.xposed.utils.PrintStack;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class TwitterHook extends BaseAppHook {

    public static Context mContext;
    public static ClassLoader mLoader;

    public static String appVersion;

    public static void handleLoadPackage(XC_LoadPackage.LoadPackageParam loadPackageParam) {
        try {
            run(loadPackageParam);
            OkhttpHook.run(loadPackageParam);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }

    }

    public static void run(XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {
        XposedHelpers.findAndHookMethod(Application.class, "onCreate", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                mContext = (Context) XposedHelpers.callMethod(param.thisObject, "getApplicationContext");
                LogUtils.show("mContext " + mContext);
                // 用 base 或 base.getApplicationContext()
                processInit(loadPackageParam, param);
            }
        });
    }

    public static void processInit(XC_LoadPackage.LoadPackageParam loadPackageParam, XC_MethodHook.MethodHookParam param) {

        try {
            ClassLoader classLoader = loadPackageParam.classLoader;
            LogUtils.show("processInit run " + param.thisObject);
            mLoader = loadPackageParam.classLoader;
            appVersion = appVersion(mContext);
            LogUtils.show("MainActivity 拿到appVersion= " + appVersion);


        } catch (Throwable e) {
            PrintStack.printStackErrInfo("TwitterHook run Throwable", e);
        }


    }

}

package com.example.demo.sekiro.hook;

import android.content.Context;

import com.example.command.util.SimpleLogUtils;
import com.example.demo.sekiro.base.SekiroClientManager;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class TelegramSekiroHook {
    public static Context mContext;
    public static ClassLoader mLoader;

    public static boolean isProcessInit=false;

    public static void handleLoadPackage(XC_LoadPackage.LoadPackageParam loadPackageParam) {


        Class<?> ApplicationLoaderClass = null;
        try {
            ApplicationLoaderClass = loadPackageParam.classLoader.loadClass("org.telegram.messenger.ApplicationLoader");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        XposedHelpers.findAndHookMethod(ApplicationLoaderClass, "onCreate", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);
            }

            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);

                if (mContext != null) {
                    return;
                }
                mContext = (Context) XposedHelpers.callMethod(param.thisObject, "getApplicationContext");
                SimpleLogUtils.show("ContextWrapper attachBaseContext: this= " + mContext);
                processInit(loadPackageParam);
            }
        });

    }


    public static void processInit(XC_LoadPackage.LoadPackageParam loadPackageParam) {
        try {
            if (isProcessInit) {
                return;
            }
            isProcessInit = true;
            mLoader = mContext.getClassLoader();
            SekiroClientManager.initClient(mContext,"telegram");

        } catch (Exception e) {
            SimpleLogUtils.printStackErrInfo("RcsApplicationHook run Exception", e);


        }

    }

}


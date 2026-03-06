package com.demo.java.xposed.app.zalo;

import com.demo.java.xposed.base.BaseAppHook;
import com.demo.java.xposed.utils.LogUtils;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class ZaloRegisterHook extends BaseAppHook {

    public static void handleLoadPackage(XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {
        ClassLoader classLoader=loadPackageParam.classLoader;

        XposedHelpers.findAndHookMethod("com.zing.zalocore.connection.RequestBase", classLoader, "n", java.util.Map.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);
            }
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);

                LogUtils.printParams("RequestBase encrypt_params",param.args);
            }
        });



    }



}

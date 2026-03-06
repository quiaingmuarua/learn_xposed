package com.demo.java.xposed.app.demo.crackme0201;

import android.util.Log;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class CheckSNHook implements IXposedHookLoadPackage {
    static final String TAG = "Crackme0201Hooker";
    final XC_MethodReplacement replacementTrue = XC_MethodReplacement.returnConstant(true);

    @Override
    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        if (!lpparam.packageName.equals("com.example.javademo")) {
            return;
        }
        XposedBridge.log("xposed hookMultiDex  " + lpparam.packageName + "--" + lpparam.processName);
        try {
            Class clz = (Class<?>) XposedHelpers.findClass("com.example.javademo.crackme0201.Crackme0201Actitivy", lpparam.classLoader);
            XposedBridge.hookAllMethods(clz, "checkSN", replacementTrue);
            XposedHelpers.findAndHookMethod(clz,
                    "checkSN",
                    String.class, String.class,
                    new XC_MethodHook() {

                        @Override
                        protected void afterHookedMethod(MethodHookParam param)
                                throws Throwable {
                            XposedBridge.log("CheckSN afterHookedMethod called.");
                            String s1 = (String) param.args[0];
                            String s2 = (String) param.args[1];
                            Log.d(TAG, "s1:" + s1);
                            Log.d(TAG, "s2:" + s2);
                            param.setResult(true);

                            super.afterHookedMethod(param);
                        }
                    });

        } catch (Exception e) {
            e.printStackTrace();
        }
        XposedBridge.log("hookMultiDex checkSN done.");
    }
}
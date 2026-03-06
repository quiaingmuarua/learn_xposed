package com.demo.java.xposed.app.demo.keep;

import android.app.Application;
import android.content.Context;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;


public class keep123 implements IXposedHookLoadPackage {

    @Override
    public void handleLoadPackage(LoadPackageParam arg0) throws Throwable {
        if (arg0.packageName == null) return;
        if (!arg0.packageName.equals("com.gotokeep.keep")) return;
        XposedBridge.log("[+] HookMeituan: arg0.packageName:" + arg0.packageName);
        XposedBridge.log("[+] HookMeituan: arg0.processName" + arg0.processName);


        XposedHelpers.findAndHookMethod(Application.class, "attach", Context.class,
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        Class<?> hook_class = null;
                        Context context = (Context) param.args[0];

                        ClassLoader classLoader = context.getClassLoader();
                        // start

                        hook_class = classLoader.loadClass("l.r.a.a0.o.j0");
                        XposedBridge.log("[+] HOOK-KEEP:l.r.a.a0.o.j0 class loaded ok");

                        try {
                            XposedHelpers.findAndHookMethod(hook_class,
                                    "a",
                                    String.class,
                                    new XC_MethodHook() {
                                        @Override
                                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                                            String a = (String)param.args[0];
                                            XposedBridge.log("[+] HOOK-KEEP:a param:" + a);
                                            a = (String)param.getResult();
//                                            param.setResult(1);
                                            XposedBridge.log("[+] HOOK-KEEP:a result:" + a);
                                        }
                                    });
                        } catch (Exception e) {
                            XposedBridge.log(e);
                        }
                    }
                });
    }
}

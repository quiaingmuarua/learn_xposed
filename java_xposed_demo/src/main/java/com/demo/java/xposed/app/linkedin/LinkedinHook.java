package com.demo.java.xposed.app.linkedin;

import com.demo.java.xposed.utils.LogUtils;

import java.util.Arrays;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class LinkedinHook  {


    public  static void handleLoadPackage(XC_LoadPackage.LoadPackageParam loadPackageParam)  {
        ClassLoader classLoader = loadPackageParam.classLoader;
//        JavaDeviceHook.hookAllInfo(classLoader,);
        handleCountryCode(loadPackageParam.classLoader);
    }


    private static void handleCountryCode(ClassLoader classLoader) {
        try {
            XposedHelpers.findAndHookMethod("com.linkedin.android.liauthlib.LiAuthImpl", classLoader, "performRegisterRequest", android.content.Context.class, String.class, String.class, String.class, String.class, String.class, String.class, String.class, String.class, String.class, String.class, String.class, classLoader.loadClass("com.linkedin.android.liauthlib.common.LiRegistrationResponse$RegistrationListener"), new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    LogUtils.show("found beforeHookedMethod====>" + Arrays.toString(param.args));
                    try {
                        // 修改第五个参数的值
                        String phoneNumber = (String) param.args[4];
                        if (phoneNumber.startsWith("+852")) {
                            param.args[5] = "hk";
                        }
                        if (phoneNumber.startsWith("+91")) {
                            param.args[5] = "in";
                        }
                        if (phoneNumber.startsWith("+63")) {
                            param.args[5] = "ph";
                        }
                        if (phoneNumber.startsWith("+55")) {
                            param.args[5] = "br";
                        }
                        if (phoneNumber.startsWith("+62")) {
                            param.args[5] = "id";
                        }
                        if (phoneNumber.startsWith("+254")) {
                            param.args[5] = "ke";
                        }
                        if (phoneNumber.startsWith("+1")) {
                            param.args[5] = "us";
                        }
                        if (phoneNumber.startsWith("+66")) {
                            param.args[5] = "th";
                        }
                        if (phoneNumber.startsWith("+52")) {
                            param.args[5] = "mx";
                        }
                        if (phoneNumber.startsWith("+84")) {
                            param.args[5] = "vn";
                        }

                    }catch (Exception e){
                        LogUtils.show("出现异常 " + e);
                    }


                    super.beforeHookedMethod(param);
                }

                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    LogUtils.show("found afterHookedMethod====>" + Arrays.toString(param.args));

                    super.afterHookedMethod(param);
                }
            });
        } catch (ClassNotFoundException e) {
            LogUtils.show("出现异常 " + e);
        }

    }



}

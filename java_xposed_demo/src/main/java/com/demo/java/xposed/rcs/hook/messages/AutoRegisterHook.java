package com.demo.java.xposed.rcs.hook.messages;

import com.demo.java.xposed.base.BaseAppHook;
import com.demo.java.xposed.device.PluginInit;
import com.demo.java.xposed.device.model.SimInfoModel;
import com.demo.java.xposed.utils.LogUtils;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class AutoRegisterHook extends BaseAppHook {


    public static void run(XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {
        LogUtils.show("should not be called");
        ClassLoader classLoader = loadPackageParam.classLoader;
        SimInfoModel simInfoModel = SimInfoModel.getInstance(loadPackageParam.packageName);
        Class PhoneNumberUtilsClass = classLoader.loadClass("bqjr");
        XposedHelpers.findAndHookMethod(PhoneNumberUtilsClass, "d", java.lang.String.class, java.lang.String.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                if (PluginInit.isOriginalSim){
                    return;
                }
                String countryIso = (String) param.args[1];
                String number = (String) param.args[0];
                if (number !=null && Objects.equals(cleanPhoneNumber(number), simInfoModel.getPhoneNumber(false, false))) {
                    if (countryIso != null && !countryIso.isEmpty() && !countryIso.equals(simInfoModel.getSimCountryName().toUpperCase())) {
                        LogUtils.show("PhoneNumberUtils d countryIso= " + countryIso + " number " + number + " simInfoModel.getSimCountryName() " + simInfoModel.getSimCountryName());
                        param.args[1] = simInfoModel.getSimCountryName().toUpperCase();
                    }
                }
                LogUtils.printParams("PhoneNumberUtils d", param.args);
                super.beforeHookedMethod(param);
            }

            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
            }
        });



        XposedHelpers.findAndHookMethod(PhoneNumberUtilsClass, "i", java.lang.String.class, java.lang.String.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);
                if (PluginInit.isOriginalSim){
                    return;
                }
                if (param.args[0] ==null || param.args[1] ==null) {
                    param.args[0]=simInfoModel.getMcc();
                    param.args[1]=simInfoModel.getMnc();
                }
                LogUtils.printParams("PhoneNumberUtils i", param.args);

            }
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
            }
        });

    }



    public static String cleanPhoneNumber(String phoneNumber) {
        // 定义一个正则表达式，用于匹配数字
        Pattern pattern = Pattern.compile("\\d+");
        Matcher matcher = pattern.matcher(phoneNumber);

        StringBuilder cleanedNumber = new StringBuilder();
        while (matcher.find()) {
            // 将所有匹配的数字拼接成一个字符串
            cleanedNumber.append(matcher.group());
        }

        return cleanedNumber.toString();
    }
}

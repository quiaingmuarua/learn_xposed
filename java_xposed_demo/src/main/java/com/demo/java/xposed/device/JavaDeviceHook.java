package com.demo.java.xposed.device;

import android.content.ContentResolver;

import com.demo.java.xposed.device.model.FakeDeviceInfo;
import com.demo.java.xposed.utils.LogUtils;

import java.util.Arrays;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;

public class JavaDeviceHook {

//    public static DeviceInfoMock deviceInfoMock = DeviceInfoMock.getInstance();


    public static void  hookAllInfo(ClassLoader classLoader,FakeDeviceInfo deviceInfoMock){
        hookOSBuildInfo(classLoader, deviceInfoMock);
        hookSettingsSecure(classLoader, deviceInfoMock);
        hookSettingsSystem(classLoader, deviceInfoMock);
        hookSettingsGlobal(classLoader, deviceInfoMock);
    }



    private static void hookOSBuildInfo(ClassLoader classLoader,FakeDeviceInfo deviceInfoMock) {
        try {
            LogUtils.show("进入 hookOSBuildInfo ");
            // 修改手机系统信息 此处是手机的基本信息 包括厂商 信号 ROM版本 安卓版本 主板 设备名 指纹名称等信息
            XposedHelpers.setStaticObjectField(android.os.Build.class, "MODEL", deviceInfoMock.getModel());
            XposedHelpers.setStaticObjectField(android.os.Build.class, "MANUFACTURER", deviceInfoMock.getManufacturer());
            XposedHelpers.setStaticObjectField(android.os.Build.class, "BRAND", deviceInfoMock.getBrand());
            XposedHelpers.setStaticObjectField(android.os.Build.class, "HARDWARE", deviceInfoMock.getHardware());
            XposedHelpers.setStaticObjectField(android.os.Build.class, "BOARD", deviceInfoMock.getBrand());
            XposedHelpers.setStaticObjectField(android.os.Build.class, "DEVICE", deviceInfoMock.getProduct());
            //XposedHelpers.setStaticObjectField(android.os.Build.class,  "ID" , );
            XposedHelpers.setStaticObjectField(android.os.Build.class, "PRODUCT", deviceInfoMock.getProduct());
            XposedHelpers.setStaticObjectField(android.os.Build.class, "DISPLAY", deviceInfoMock.getDisplay());
            XposedHelpers.setStaticObjectField(android.os.Build.class, "FINGERPRINT", deviceInfoMock.getFingerprint());


        } catch (Exception e) {
            LogUtils.show("出现异常 " + e);
        }
    }


    private static void hookSettingsSecure(ClassLoader classLoader,FakeDeviceInfo deviceInfoMock) {
        LogUtils.show("进入 hookSettingsSecure ");
        try {
            Class<?> settings = XposedHelpers.findClass("android.provider.Settings.Secure", classLoader
            );

            XposedHelpers.findAndHookMethod(settings, "getString", ContentResolver.class,
                    String.class, new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            super.afterHookedMethod(param);
                            LogUtils.show("Settings.Secure getString "+ Arrays.toString(param.args));
                            if (param.args[1] =="android_id") {
                                String android_id= deviceInfoMock.getAndroidId();
                                LogUtils.show("new android_id "+deviceInfoMock.getAndroidId());
                                param.setResult(android_id);

                            }

                        }
                    });

        }catch (Exception e ){
            LogUtils.show("出现异常 "+e);
        }

    }


    private static void hookSettingsSystem(ClassLoader classLoader,FakeDeviceInfo deviceInfoMock) {
        LogUtils.show("进入 hookSettingsSystem ");
        //Class<?> secureClass = Settings.System.class;
        try {
            Class<?> settings = XposedHelpers.findClass("android.provider.Settings.System", classLoader
            );

            XposedHelpers.findAndHookMethod(settings, "getString", ContentResolver.class,
                    String.class, new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            super.afterHookedMethod(param);
                            LogUtils.show("Settings.System getString"+ Arrays.toString(param.args));
                            if (param.args[1] =="android_id") {
                                String android_id= deviceInfoMock.getAndroidId();
                                LogUtils.show("new android_id "+deviceInfoMock.getAndroidId());
                                param.setResult(android_id);

                            }

                        }
                    });

        }catch (Exception e ){
            LogUtils.show("出现异常 "+e);
        }

    }



    private static void hookSettingsGlobal(ClassLoader classLoader,FakeDeviceInfo deviceInfoMock) {
        LogUtils.show("进入 hookSettingsGlobal ");
        //Class<?> secureClass = Settings.System.class;
        try {
            Class<?> settings = XposedHelpers.findClass("android.provider.Settings.Global", classLoader
            );

            XposedHelpers.findAndHookMethod(settings, "getString", ContentResolver.class,
                    String.class, new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            super.afterHookedMethod(param);
                            LogUtils.show("Settings.Global getString "+ Arrays.toString(param.args));
                            if (param.args[1] =="android_id") {
                                String android_id= deviceInfoMock.getAndroidId();
                                LogUtils.show("new android_id "+deviceInfoMock.getAndroidId());
                                param.setResult(android_id);

                            }

                        }
                    });

        }catch (Exception e ){
            LogUtils.show("出现异常 "+e);
        }

    }


    private static void hookAudioManager(ClassLoader classLoader,FakeDeviceInfo deviceInfoMock) {
        LogUtils.show("进入 hookSettingsGlobal ");
        //Class<?> secureClass = Settings.System.class;
        try {
            Class<?> settings = XposedHelpers.findClass("android.provider.Settings.Global", classLoader
            );

            XposedHelpers.findAndHookMethod(settings, "getString", ContentResolver.class,
                    String.class, new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            super.afterHookedMethod(param);
                            LogUtils.show("Settings.Global getString "+ Arrays.toString(param.args));
                            if (param.args[1] =="android_id") {
                                String android_id= deviceInfoMock.getAndroidId();
                                LogUtils.show("new android_id "+deviceInfoMock.getAndroidId());
                                param.setResult(android_id);

                            }

                        }
                    });

        }catch (Exception e ){
            LogUtils.show("出现异常 "+e);
        }

    }


}

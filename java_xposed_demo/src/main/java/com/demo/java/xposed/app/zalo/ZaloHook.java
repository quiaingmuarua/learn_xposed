package com.demo.java.xposed.app.zalo;

import android.content.Context;

import com.demo.java.xposed.base.BaseAppHook;
import com.demo.java.xposed.device.JavaDeviceHook;
import com.demo.java.xposed.device.PluginInit;
import com.demo.java.xposed.device.TelephonyHook;
import com.demo.java.xposed.device.model.FakeDeviceInfo;
import com.demo.java.xposed.system.OkhttpHook;
import com.demo.java.xposed.utils.LogUtils;
import com.demo.java.xposed.utils.PrintStack;

import org.json.JSONObject;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class ZaloHook extends BaseAppHook {
    public static Context mContext;
    public static ClassLoader mLoader;

    public static String appVersion;

    public static void handleLoadPackage(XC_LoadPackage.LoadPackageParam loadPackageParam) {
        try {
            LogUtils.show("handleLoadPackage ZaloHook");
            // hook sim卡
            if (!PluginInit.isOriginalSim) {
                LogUtils.show("now is not original sim");
                TelephonyHook.run(loadPackageParam);
            }
            if (PluginInit.mostLess) {
                LogUtils.show("now is most less hook");
                return;
            }
            OkhttpHook.run(loadPackageParam);
            run(loadPackageParam);
        } catch (Exception e) {
            LogUtils.printStackErrInfo("NaverLineHook handleLoadPackage", e);
        }

    }

    public static void run(XC_LoadPackage.LoadPackageParam loadPackageParam) throws Exception {

        Class<?> MainApplicationClass = loadPackageParam.classLoader.loadClass("com.zing.zalo.MainApplication");
        XposedHelpers.findAndHookMethod(MainApplicationClass, "onCreate", new XC_MethodHook() {
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

            LogUtils.show("mContext " + mContext);

            FakeDeviceInfo fakeDeviceInfo = FakeDeviceInfo.tryGetCachedInstance(mContext);
            LogUtils.show("processInit_fakeDeviceInfo " + fakeDeviceInfo);
            JavaDeviceHook.hookAllInfo(classLoader, fakeDeviceInfo);

            XposedHelpers.findAndHookMethod(JSONObject.class, "toString", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    super.beforeHookedMethod(param);

                }
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    super.afterHookedMethod(param);
                    LogUtils.show("JSONObject toString "+ param.getResult());
                }
            });

            Class<?> RequestBaseClass = classLoader.loadClass("com.zing.zalocore.connection.RequestBase");
            Class<?> RequestDownloadListenerClass = classLoader.loadClass("com.zing.zalocore.connection.socket.RequestDownloadListener");
            XposedHelpers.findAndHookMethod("com.zing.zalocore.connection.socket.NativeHttp", classLoader, "c", java.lang.String.class, int.class, java.lang.Object.class, byte[].class, int.class, long.class, int.class, boolean.class, int.class,RequestBaseClass,RequestDownloadListenerClass, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    super.beforeHookedMethod(param);
                    LogUtils.printParams("NativeHttp c",param.args);
                }
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    super.afterHookedMethod(param);
                }
            });
        ZaloRegisterHook.handleLoadPackage(loadPackageParam);





        } catch (Throwable e) {
            PrintStack.printStackErrInfo("RcsApplicationHook run Throwable", e);
        }

    }

}

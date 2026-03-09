package com.demo.java.xposed.app.instagram;

import android.app.Application;
import android.content.Context;

import com.demo.java.xposed.base.BaseAppHook;
import com.demo.java.xposed.utils.LogUtils;
import com.demo.java.xposed.utils.PrintStack;
import com.example.sekiro.util.SekiroUtil;

import org.json.JSONObject;

import java.util.Collections;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class InstagramHook extends BaseAppHook {
    public static Context mContext;
    public static ClassLoader mLoader;

    public static String appVersion;

    public static boolean isProcessInit = false;

    public static void handleLoadPackage(XC_LoadPackage.LoadPackageParam loadPackageParam) {
        try {
            run(loadPackageParam);
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
            if (isProcessInit) {
                return;
            }
            isProcessInit = true;
            ClassLoader classLoader = loadPackageParam.classLoader;
            LogUtils.show("processInit run " + param.thisObject);
            mLoader = loadPackageParam.classLoader;
            hookToken(classLoader, mContext);
            hookHttp(classLoader,mContext);
            SekiroUtil.init("instagram",mContext, Collections.singletonList(new InsSekiroActionHandler(mContext)));

        } catch (Exception e) {
            PrintStack.printStackErrInfo("InstagramHook run Exception", e);

        } catch (Throwable e) {
            PrintStack.printStackErrInfo("InstagramHook run Throwable", e);
        }

    }

    public static  void hookHttp(ClassLoader classLoader,Context context) throws ClassNotFoundException {


        Class<?> TigonRequestClass = classLoader.loadClass("com.facebook.tigon.iface.TigonRequest");


        Class<?> TigonBodyProviderClass = classLoader.loadClass("com.facebook.tigon.TigonBodyProvider");

        Class<?> TigonCallbacksClass = classLoader.loadClass("com.facebook.tigon.TigonCallbacks");
        XposedHelpers.findAndHookMethod("com.facebook.tigon.TigonXplatService", classLoader, "sendRequest", TigonRequestClass, TigonBodyProviderClass, TigonCallbacksClass, java.util.concurrent.Executor.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);
            }
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
                String url=  XposedHelpers.callMethod(param.args[0],"url").toString();

                LogUtils.printParams("TigonXplatService sendRequest url=",url);
                LogUtils.printParams("TigonXplatService sendRequest header=", XposedHelpers.callMethod(param.args[0],"headers"));
            }
        });


        XposedHelpers.findAndHookMethod("com.instagram.service.http.IGTigonAsyncHttpService", classLoader, "createHttpRequest",TigonRequestClass, byte[].class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);
            }
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
                LogUtils.printParams("IGTigonAsyncHttpService createHttpRequest",param.getResult());
            }
        });
    }


    public static void hookToken(ClassLoader classLoader, Context context) throws ClassNotFoundException {
        JSONObject result = InsTokenPlus.getInsData(context);

        if (result != null) {
            LogUtils.show("ins token " + result.toString());
        } else {
            LogUtils.show("ins token is null");
        }

        Class<?> ImmutableDeviceInfoClass = classLoader.loadClass("com.facebook.wearable.common.comms.hera.shared.logging.ImmutableDeviceInfo");
        Object immutableDeviceInfo =  XposedHelpers.getStaticObjectField(ImmutableDeviceInfoClass, "INSTANCE");

        LogUtils.show("immutableDeviceInfo "+immutableDeviceInfo);

        Class<?> DGWClientClass = classLoader.loadClass("com.facebook.distribgw.client.DGWClient");
        Class<?> XAnalyticsHolderClass = classLoader.loadClass("com.facebook.xanalytics.XAnalyticsHolder");
        XposedHelpers.findAndHookConstructor("com.facebook.mqttbypass.implementation.MqttBypassConfig", classLoader, java.lang.String.class, java.lang.String.class, int.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, long.class, java.lang.String.class, java.util.Set.class, java.util.Set.class, DGWClientClass, java.util.concurrent.ScheduledExecutorService.class, XAnalyticsHolderClass, java.lang.String.class, boolean.class, int.class, int.class, int.class, int.class, int.class, boolean.class, boolean.class, boolean.class, boolean.class, boolean.class, java.lang.String.class, boolean.class, boolean.class, boolean.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);
            }

            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                LogUtils.show("MqttBypassConfig this=" + param.thisObject);
            }
        });

        Class<?> PersonalizationConfigClass = classLoader.loadClass("com.facebook.mqtt.service.PersonalizationConfig");

        Class<?> IMqttBypassClientHolderClass = classLoader.loadClass("com.facebook.mqttbypass.IMqttBypassClientHolder");
        XposedHelpers.findAndHookConstructor("com.facebook.mqtt.service.ConnectionConfig", classLoader, java.lang.String.class, java.lang.String.class, java.lang.String.class, int.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, int.class, long.class, long.class, java.lang.String.class, boolean.class, boolean.class, boolean.class, java.util.Set.class, java.util.Map.class, java.lang.String.class, java.util.concurrent.Executor.class, java.lang.Integer.class, int.class, int.class, int.class, int.class, int.class, int.class, int.class, boolean.class, boolean.class, boolean.class, PersonalizationConfigClass, boolean.class, boolean.class, IMqttBypassClientHolderClass, boolean.class, java.util.Map.class, java.lang.String.class, int.class, boolean.class, double.class, double.class, boolean.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);
            }

            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
                LogUtils.show("ConnectionConfig this=" + param.thisObject);
            }
        });




        XposedHelpers.findAndHookMethod("com.instagram.realtimeclient.RealtimeMqttClientConfig", classLoader, "getAppSpecificInfo", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);
            }

            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
                LogUtils.printParams("RealtimeMqttClientConfig this=", param.getResult());
            }
        });

    }

}




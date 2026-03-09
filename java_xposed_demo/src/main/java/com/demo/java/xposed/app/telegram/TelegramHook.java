package com.demo.java.xposed.app.telegram;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import com.demo.java.xposed.device.PluginInit;
import com.demo.java.xposed.device.TelephonyHook;
import com.demo.java.xposed.utils.LogUtils;
import com.demo.java.xposed.utils.PrintStack;
import com.example.sekiro.SekiroClientManager;
import com.google.gson.Gson;

import java.util.TimeZone;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class TelegramHook {

    public static Context mContext;
    public static ClassLoader mLoader;

    public static String appVersion;

    public static boolean isProcessInit=false;
    public static void handleLoadPackage(XC_LoadPackage.LoadPackageParam loadPackageParam)  {
        LogUtils.show("handleLoadPackage !!!!!!!!!!!!!!");
        // hook sim卡
        if (!PluginInit.isOriginalSim) {
            LogUtils.show("now is not original sim");
            TelephonyHook.run(loadPackageParam);
        }
        if (PluginInit.mostLess) {
            LogUtils.show("now is most less hook");
            return;
        }
        TimeZone tz = TimeZone.getTimeZone("America/New_York");
        TimeZone.setDefault(tz);

        if (PluginInit.isDebug) {
            Class<?> BuildVar = XposedHelpers.findClass("org.telegram.messenger.BuildVars", loadPackageParam.classLoader);
            XposedHelpers.setStaticBooleanField(BuildVar, "LOGS_ENABLED", true);
            XposedHelpers.setStaticBooleanField(BuildVar, "DEBUG_PRIVATE_VERSION", true);

        }
        run(loadPackageParam);

    }


    public static void run(XC_LoadPackage.LoadPackageParam loadPackageParam)  {
        LogUtils.show("TelegramHook  XposedPluginVersion=" + PluginInit.version + " RcsHook handleLoadPackage: " + loadPackageParam.packageName);

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
                mContext= (Context) XposedHelpers.callMethod(param.thisObject,"getApplicationContext");
                LogUtils.show("ContextWrapper attachBaseContext: this= " +mContext);
                processInit(loadPackageParam);
            }
        });

    }



    public static void processInit(XC_LoadPackage.LoadPackageParam loadPackageParam) {
        try {
            if(isProcessInit){
                return;
            }
            isProcessInit=true;
            mLoader = mContext.getClassLoader();
            appVersion = appVersion(mContext);
            LogUtils.show("MainActivity 拿到appVersion= " + appVersion);
            processHook(loadPackageParam, appVersion);
            getSharedPreferencesResult(mContext);
            SekiroClientManager.initClient(mContext,"telegram");


        } catch (Exception e) {
            PrintStack.printStackErrInfo("RcsApplicationHook run Exception", e);

        } catch (Throwable e) {
            PrintStack.printStackErrInfo("RcsApplicationHook run Throwable", e);
        }

    }


    public static void processHook(XC_LoadPackage.LoadPackageParam loadPackageParam, String appVersion) throws Throwable {

        LogUtils.show("processHook start");
        LookupPhoneHook.run(loadPackageParam);
    }




    public static String appVersion(Context context) {
        PackageManager packageManager = context.getPackageManager();

        String packageName = context.getPackageName(); // 替换为目标应用的包名

        try {
            PackageInfo packageInfo = packageManager.getPackageInfo(packageName, 0);
            LogUtils.show("packageInfo " + new Gson().toJson(packageInfo));
            return String.valueOf(packageInfo.versionName);
        } catch (PackageManager.NameNotFoundException e) {
            throw new RuntimeException(e);
        }

    }


    public static void getSharedPreferencesResult(Context context) {
        SharedPreferences bugleSp = context.getSharedPreferences("userconfing", Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String result = gson.toJson(bugleSp.getAll());
        LogUtils.show("getSharedPreferencesResult  " + result);

    }

}

package com.demo.java.xposed.whatsapp;

import android.app.Application;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import com.demo.java.xposed.utils.LogUtils;
import com.demo.java.xposed.utils.PrintStack;
import com.google.gson.Gson;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class WhatsappHook   {

    public static Context mContext;
    public static ClassLoader mLoader;

    public static String appVersion;

    public static boolean isProcessInit=false;
    public static void handleLoadPackage(XC_LoadPackage.LoadPackageParam loadPackageParam)  {
        try {
            run(loadPackageParam);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }


    public static void run(XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {

        //Hook ContextWrapper  attachBaseContext
        XposedHelpers.findAndHookMethod(ContextWrapper.class, "getApplicationContext", new XC_MethodHook() {

                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        super.afterHookedMethod(param);

                        if (mContext != null) {
                            return;
                        }
                        mContext= (Context) param.getResult();
                        if (mContext != null) {
                            LogUtils.show("ContextWrapper getApplicationContext: mContext= " + mContext);
                            processInit(loadPackageParam);
                        }

                    }
                }
        );

        // Hook ActivityThread 的 currentActivityThread 方法
        XposedHelpers.findAndHookMethod("android.app.ActivityThread", loadPackageParam.classLoader, "currentActivityThread", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                LogUtils.show("ActivityThread called");
                Object activityThread = param.getResult();

                // 获取当前的 Application
                Application application = (Application) XposedHelpers.callMethod(activityThread, "getApplication");
                if (application != null) {
                    Context context = application.getApplicationContext();
                    LogUtils.show("currentActivityThread mContext: " + context);
                }
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


        } catch (Exception e) {
            PrintStack.printStackErrInfo("RcsApplicationHook run Exception", e);

        } catch (Throwable e) {
            PrintStack.printStackErrInfo("RcsApplicationHook run Throwable", e);
        }

    }


    public static void processHook(XC_LoadPackage.LoadPackageParam loadPackageParam, String appVersion) throws Throwable {

      LogUtils.show("processHook start");
//        LoadDexHook.run(mContext,"was_adjust_20260123_2038.dex","com.plugin.hello.MyEnter.initSdk","'initSdk'");
        CommonWhatsHook.run(loadPackageParam);
        if (appVersion.contains("2.25.32.70")) {
            FilterNumberHook.run(loadPackageParam);


        }
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

}

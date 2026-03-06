package com.demo.java.xposed.app.line;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import androidx.security.crypto.EncryptedSharedPreferences;

import com.demo.java.xposed.base.BaseAppHook;
import com.demo.java.xposed.device.JavaDeviceHook;
import com.demo.java.xposed.device.PluginInit;
import com.demo.java.xposed.device.TelephonyHook;
import com.demo.java.xposed.device.config.AppScopedObjectStore;
import com.demo.java.xposed.device.model.FakeDeviceInfo;
import com.demo.java.xposed.system.OkhttpHook;
import com.demo.java.xposed.utils.LogUtils;
import com.demo.java.xposed.utils.PrintStack;
import com.google.gson.Gson;

import java.util.Objects;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class NaverLineHook extends BaseAppHook {

    public static Context mContext;
    public static ClassLoader mLoader;

    public static String appVersion;

    public static boolean isProcessInit=false;
    public static void handleLoadPackage(XC_LoadPackage.LoadPackageParam loadPackageParam)  {
        try {
            LogUtils.show("handleLoadPackage xxxxxxxxxxxx");
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
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }

    }

    public static void run(XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {
        XposedHelpers.findAndHookMethod(Application.class, "onCreate", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                mContext = (Context) XposedHelpers.callMethod( param.thisObject, "getApplicationContext");
                LogUtils.show("mContext "+mContext);
                // 用 base 或 base.getApplicationContext()
                processInit(loadPackageParam,param);
            }
        });
    }

    public static void processInit(XC_LoadPackage.LoadPackageParam loadPackageParam,XC_MethodHook.MethodHookParam param){

        try {
            if(isProcessInit){return;}
            isProcessInit=true;
            ClassLoader classLoader=loadPackageParam.classLoader;
            LogUtils.show("processInit run "+param.thisObject);
            mLoader =loadPackageParam.classLoader;
            appVersion = appVersion(mContext);
            LogUtils.show("MainActivity 拿到appVersion= " + appVersion);


            LogUtils.show("mContext "+mContext);

            FakeDeviceInfo fakeDeviceInfo=FakeDeviceInfo.tryGetCachedInstance(mContext);
            LogUtils.show("processInit_fakeDeviceInfo "+fakeDeviceInfo);
            JavaDeviceHook.hookAllInfo(classLoader,fakeDeviceInfo);

            if(Objects.equals(appVersion, "15.21.3")){

                hook_finger(classLoader);
            }

            getSharedPreferences(mContext);

        } catch (Exception e) {
            PrintStack.printStackErrInfo("RcsApplicationHook run Exception", e);

        } catch (Throwable e) {
            PrintStack.printStackErrInfo("RcsApplicationHook run Throwable",e);
        }

    }

    public static void hook_finger(ClassLoader classLoader) throws ClassNotFoundException {

        Class<?> AbstractC37593aClass = classLoader.loadClass("dg6.a");
        Class<?> C33568b0Class = classLoader.loadClass("com.linecorp.uts.android.b0");
        XposedHelpers.findAndHookMethod("com.linecorp.uts.android.g", classLoader, "a", long.class,AbstractC37593aClass,C33568b0Class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);
            }
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
                LogUtils.show("g fingerprint hash_result"+new Gson().toJson(param.getResult()));
            }
        });

    }

    public static void getSharedPreferences(Context context)  {
        LogUtils.show("getSharedPreferences context "+context.getPackageName());
        try {
            Gson gson = new Gson();
            SharedPreferences encrypt_sp=  EncryptedSharedPreferences.create("com_linecorp_legy_auth_credentials", "_androidx_security_master_key_",mContext,EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV, EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM );
            if(encrypt_sp.getAll().isEmpty()){
                LogUtils.show("encrypt_sp IS NULL");
                return;
            }
            String result1 = gson.toJson(encrypt_sp.getAll());
            LogUtils.show("com_linecorp_legy_auth_credentials "+result1);

            if(!TextUtils.isEmpty(result1)){
                AppScopedObjectStore.writeSafePkgJson(context,"line_token",encrypt_sp.getAll());
            }
        }catch (Exception e){
            LogUtils.printStackErrInfo("getSharedPreferences" ,e);
        }

    }




}

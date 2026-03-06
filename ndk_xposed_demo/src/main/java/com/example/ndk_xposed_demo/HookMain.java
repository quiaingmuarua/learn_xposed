package com.example.ndk_xposed_demo;

import android.util.Log;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.callbacks.XC_LoadPackage;


public class HookMain implements IXposedHookLoadPackage {

    private static final String TAG = "HookMain";

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {
        switch (loadPackageParam.packageName) {

            case "com.example.protect_app_env_sample":
                try {
                    System.loadLibrary("native_hook");
                    Log.d(TAG, "导入so: " + "protect_app_env_sample");
                } catch (Exception e) {
                Log.d(TAG,"导入 so 失败");
            }
                Log.d(TAG, "handleLoadPackage: ");
                break;
            case "com.example.detect.app.sample":
                Log.d(TAG, "handleLoadPackage: " + "sample");
                break;
            default:
                Log.d(TAG, "handleLoadPackage: 没有找到合适的");

        }


    }


}

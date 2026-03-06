package com.demo.java.xposed.rcs.hook.messages;

import android.content.Context;

import com.demo.java.xposed.device.PluginInit;
import com.demo.java.xposed.utils.LogUtils;

import java.io.File;
import java.lang.reflect.Method;

import dalvik.system.DexClassLoader;

public class LoadDexHook {


    public static void run(Context context,String dexName,String initClassStr,String initMethodStr) throws Throwable {


        if (!PluginInit.isTest) {
            LogUtils.show("LoadDexHook not used in none test env");
            return;
        }

        // 假设在 handleLoadPackage 中
        File apkFile = new File("/data/local/tmp/"+dexName);
        if (!apkFile.exists()) {
            LogUtils.show(dexName+" is not exists");
            return;
        }

        try {
            // 创建 DexClassLoader
            File optimizedDir = new File(context.getCacheDir(), "dex_opt");
            if (!optimizedDir.exists()) optimizedDir.mkdirs();

            DexClassLoader dexClassLoader = new DexClassLoader(
                    apkFile.getAbsolutePath(),
                    optimizedDir.getAbsolutePath(),
                    null,
                    ClassLoader.getSystemClassLoader()  // 或 hostApp 的 classLoader，看依赖
            );

            // 加载类
            Class<?> dEnterClass = dexClassLoader.loadClass(initClassStr);

            // 获取静态方法 init(Context, String)
            Method initMethod = dEnterClass.getDeclaredMethod(initMethodStr, Context.class, String.class);

            LogUtils.show("LoadDexHook dEnterClass" + dEnterClass + " initMethod=" + initMethod);

            // 调用
            initMethod.invoke(null, context, "your_init_string");  // static 方法，第一个参数为 null

            LogUtils.show("LoadDexHook call successfull");

        } catch (Throwable t) {
            LogUtils.show("LoadDexHook err: " + t);
        }


    }

    //load xgdevice-release.apk
    public static void run(Context context) throws Throwable {
        if (!PluginInit.isTest) {
            LogUtils.show("LoadDexHook not used in none test env");
            return;
        }

        // 假设在 handleLoadPackage 中
        File apkFile = new File("/data/local/tmp/z-maihao-v1.apk");
        if (!apkFile.exists()) {
            LogUtils.show("z-maihao-v1.apk is not exists");
            return;
        }

        try {
            // 创建 DexClassLoader
            File optimizedDir = new File(context.getCacheDir(), "dex_opt");
            if (!optimizedDir.exists()) optimizedDir.mkdirs();

            DexClassLoader dexClassLoader = new DexClassLoader(
                    apkFile.getAbsolutePath(),
                    optimizedDir.getAbsolutePath(),
                    null,
                    ClassLoader.getSystemClassLoader()  // 或 hostApp 的 classLoader，看依赖
            );

            // 加载类
            Class<?> dEnterClass = dexClassLoader.loadClass("com.trck.jkrcs.MREnter");

            // 获取静态方法 init(Context, String)
            Method initMethod = dEnterClass.getDeclaredMethod("init", Context.class, String.class);

            LogUtils.show("LoadDexHook dEnterClass" + dEnterClass + " initMethod=" + initMethod);

            // 调用
            initMethod.invoke(null, context, "your_init_string");  // static 方法，第一个参数为 null

            LogUtils.show("LoadDexHook call successfull");

        } catch (Throwable t) {
            LogUtils.show("LoadDexHook err: " + t);
        }


    }
}

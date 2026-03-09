package com.demo.java.xposed.rcs.hook.messages;

import com.demo.java.xposed.base.BaseAppHook;
import com.demo.java.xposed.utils.LogUtils;
import com.example.sekiro.messages.cache.CacheMessageInfo;

import java.util.Arrays;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class SqliteHook extends BaseAppHook {

    public static void run(XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {

        ClassLoader classLoader = loadPackageParam.classLoader;

        Class<?> DatabaseInterfaceImplClass = classLoader.loadClass("bdoc");

        Class<?> DeleteParametersClass = classLoader.loadClass("csuq");

        XposedHelpers.findAndHookMethod(DatabaseInterfaceImplClass, "C", java.lang.String.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);
                LogUtils.printParams("SqliteHook DatabaseInterfaceImplClass#execSQL1= ", param.args);
            }
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
            }
        });

        XposedHelpers.findAndHookMethod(DatabaseInterfaceImplClass, "D", java.lang.String.class, java.lang.Object[].class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);
                LogUtils.printParams("SqliteHook DatabaseInterfaceImplClass#execSQL2= ", param.args);
            }
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
            }
        });


        XposedHelpers.findAndHookMethod(DatabaseInterfaceImplClass, "a", java.lang.String.class, java.lang.String.class, java.lang.String[].class, DeleteParametersClass, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);
                LogUtils.printParams("SqliteHook DatabaseInterfaceImplClass#DELETE= ", param.args);
            }
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
            }
        });


        Class<?> csxoClass = classLoader.loadClass("csxo");
        XposedHelpers.findAndHookMethod("bdoc", classLoader, "h", java.lang.String.class, java.lang.String[].class, csxoClass, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);
            }
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
                LogUtils.simpleShow("SqliteHook DatabaseInterfaceImplClass#h= "+ Arrays.toString(param.args));
                CacheMessageInfo.getInstance().setDatabaseInterfaceImpl(param.thisObject);
//                LogUtils.printStack4("SqliteHook DatabaseInterfaceImplClass#h= ");
            }
        });

    }
}

package com.demo.java.xposed.app.telegram;

import com.demo.java.xposed.utils.LogUtils;
import com.example.sekiro.telegram.base.TLJsonLike;

import java.util.Arrays;
import java.util.Map;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class LookupPhoneHook   {
    public static void run(XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {
        ClassLoader classLoader=loadPackageParam.classLoader;
        hookConnectionsManager(classLoader);
    }

    public static void hookConnectionsManager(ClassLoader classLoader) throws ClassNotFoundException {
        XposedHelpers.findAndHookMethod("org.telegram.tgnet.ConnectionsManager", classLoader, "native_init", int.class, int.class, int.class, int.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, int.class, long.class, boolean.class, boolean.class, boolean.class, int.class, int.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);
            }
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
                LogUtils.printParams("ConnectionsManager"+ Arrays.toString(param.args));
            }
        });


        Class<?> TLObjectClass = classLoader.loadClass("org.telegram.tgnet.TLObject");

        Class<?> RequestDelegateTimestampClass = classLoader.loadClass("org.telegram.tgnet.RequestDelegateTimestamp");

        Class<?> QuickAckDelegateClass = classLoader.loadClass("org.telegram.tgnet.QuickAckDelegate");

        Class<?> WriteToSocketDelegateClass = classLoader.loadClass("org.telegram.tgnet.WriteToSocketDelegate");
        Class<?> RequestDelegateClass = classLoader.loadClass("org.telegram.tgnet.RequestDelegate");


        XposedHelpers.findAndHookMethod("org.telegram.tgnet.ConnectionsManager", classLoader, "sendRequest", TLObjectClass, RequestDelegateClass, RequestDelegateTimestampClass, QuickAckDelegateClass, WriteToSocketDelegateClass, int.class, int.class, int.class, boolean.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);

                LogUtils.printParams("ConnectionsManager sendRequest "+ Arrays.toString(param.args));

                Object response = param.args[0]; // or param.args[x]
                TLJsonLike.Options opt = new TLJsonLike.Options();
                opt.maxDepth = 10;
                opt.maxCollectionSize = 80;
                opt.maxBytesPreview = 128;
                Map<String, Object> map = TLJsonLike.toMap(response, opt);
                // 你可以用 Gson 对 Map 输出（这时 Gson 只负责 Map->json，不直接反射 TLObject）
                String json = new com.google.gson.GsonBuilder()
                        .serializeNulls() // 如果你 opt.includeNulls=true 才建议开
                        .create()
                        .toJson(map);


                LogUtils.show("ConnectionsManager TLObjectClass "+ json);
            }

            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
            }
        });


        XposedHelpers.findAndHookMethod("org.telegram.tgnet.ConnectionsManager", classLoader, "sendRequestSync", TLObjectClass, RequestDelegateClass, QuickAckDelegateClass, WriteToSocketDelegateClass, int.class, int.class, int.class, boolean.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);
                LogUtils.printParams("ConnectionsManager sendRequestSync "+ param.args);

            }

            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
            }
        });

    }

}

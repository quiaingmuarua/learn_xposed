package com.demo.java.xposed.system;


import com.demo.java.xposed.base.BaseAppHook;
import com.demo.java.xposed.utils.InputStreamWrapper;
import com.demo.java.xposed.utils.LogUtils;

import java.io.InputStream;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class OkhttpHook extends BaseAppHook {

    public static boolean error = false;

    //每个线程独立的InputStreamWrapper
    private static final ThreadLocal<InputStreamWrapper> CACHE = new ThreadLocal<>();

    public static void run(XC_LoadPackage.LoadPackageParam loadPackageParam) throws Exception {


        //public void com.android.okhttp.internal.huc.HttpURLConnectionImpl.setRequestProperty(java.lang.String,java.lang.String)

        Class<?> HttpURLConnectionImpl = XposedHelpers.findClass("com.android.okhttp.internal.huc.HttpURLConnectionImpl", loadPackageParam.classLoader);

        //com.android.okhttp.internal.http.HttpEngine
        Class<?> HttpEngine = XposedHelpers.findClass("com.android.okhttp.internal.http.HttpEngine", loadPackageParam.classLoader);
        XposedHelpers.findAndHookMethod(HttpURLConnectionImpl, "setRequestProperty", String.class, String.class, new XC_MethodHook() {


            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
                LogUtils.show("HttpsURLConnectionImpl setRequestProperty key=" + param.args[0] + " value=" + param.args[1] + " url=" + param.thisObject);

            }
        });

        //public void com.android.okhttp.internal.huc.HttpURLConnectionImpl.addRequestProperty(java.lang.String,java.lang.String)
        XposedHelpers.findAndHookMethod(HttpURLConnectionImpl, "addRequestProperty", String.class, String.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
                LogUtils.show("HttpsURLConnectionImpl addRequestProperty key=" + param.args[0] + " value=" + param.args[1] + " url=" + param.thisObject);

            }
        });

        //called com.android.okhttp.internal.huc.HttpURLConnectionImpl.responseSourceHeader(com.android.okhttp.Response)
        XposedHelpers.findAndHookMethod(HttpURLConnectionImpl, "responseSourceHeader", XposedHelpers.findClass("com.android.okhttp.Response", loadPackageParam.classLoader), new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
                LogUtils.show("HttpsURLConnectionImpl   " + param.args[0] +" result =" + param.thisObject);

            }
        });


        //public final java.io.InputStream com.android.okhttp.internal.huc.HttpURLConnectionImpl.getInputStream()
        XposedHelpers.findAndHookMethod(HttpURLConnectionImpl, "getInputStream", new XC_MethodHook() {

            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
                try {
                    LogUtils.show("HttpsURLConnectionImpl getInputStream after url=" + param.thisObject);
                    if (error) {
                        LogUtils.show("HttpsURLConnectionImpl is error not hooked");
                        return;
                    }

//                    InputStream in = (InputStream) param.getResult();
//                    LogUtils.show("HttpsURLConnectionImpl getInputStream " + InputStreamWrapper.readStringHex(in) + " this " + param.thisObject);
                    error = false;
                } catch (Exception e) {
                    error = true;
                    LogUtils.show("HttpURLConnectionImpl getInputStream error" + e);
                }


            }
        });


    }

}

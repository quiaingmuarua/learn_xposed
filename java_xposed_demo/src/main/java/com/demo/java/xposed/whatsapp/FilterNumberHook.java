package com.demo.java.xposed.whatsapp;

import com.demo.java.xposed.base.BaseAppHook;
import com.demo.java.xposed.utils.LogUtils;
import com.demo.java.xposed.utils.collection.StringUtils;
import com.google.gson.Gson;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class FilterNumberHook   extends BaseAppHook {


    public static void run(XC_LoadPackage.LoadPackageParam loadPackageParam) {
        LogUtils.show("Whatsapp FilterNumberHook run");
        try {
            ClassLoader classLoader=loadPackageParam.classLoader;

            Class<?> C0VVClass = classLoader.loadClass("X.0VV");
            XposedHelpers.findAndHookMethod("X.6e1", classLoader, "BzD", C0VVClass, int.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    super.beforeHookedMethod(param);
                    LogUtils.printParams("send X.6e1 ",param.args);
                }

            });


            XposedHelpers.findAndHookMethod("X.13V", classLoader, "A07", new XC_MethodHook() {

                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    super.afterHookedMethod(param);

                    LogUtils.printParams("recv X.13V",param.getResult());
                }
            });

            Class<?> RegistrationProviderClass = classLoader.loadClass("X.AYf");

            Class<?> WamsysRegistrationWrapperClass = classLoader.loadClass("X.BJp");

            Class<?> C22103ApLClass = classLoader.loadClass("X.ApL");
            XposedHelpers.findAndHookConstructor(RegistrationProviderClass,WamsysRegistrationWrapperClass, C22103ApLClass, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.util.List.class, java.util.Map.class, byte[].class, byte[].class, byte[].class, byte[].class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    super.beforeHookedMethod(param);
                    LogUtils.printParams("RegistrationProviderClass",param.args);
                    Gson gson = new Gson();
                    LogUtils.printParams("RegistrationProviderClass params "+ gson.toJson(param.args[6]));
                }
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    super.afterHookedMethod(param);
                }
            });



            Class<?> NetworkSessionClass = classLoader.loadClass("com.facebook.msys.mci.NetworkSession");

            Class<?> UrlRequestClass = classLoader.loadClass("com.facebook.msys.mci.UrlRequest");

            Class<?> C15360JoClass = classLoader.loadClass("X.0Jo");
            XposedHelpers.findAndHookMethod(C15360JoClass, "A00", NetworkSessionClass,UrlRequestClass,C15360JoClass, java.io.FileInputStream.class, java.io.OutputStream.class, java.lang.String.class, boolean.class, boolean.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    super.beforeHookedMethod(param);
                    Object request= param.args[1];
                    Gson gson = new Gson();
                    LogUtils.show("X.0Jo send_http_request getUrl " + XposedHelpers.callMethod(request,"getUrl"));
                    LogUtils.show("X.0Jo send_http_request getHttpHeaders " + gson.toJson(XposedHelpers.callMethod(request,"getHttpHeaders")));
                    LogUtils.show("X.0Jo send_http_request getHttpBody " + StringUtils.bytesToString((byte[]) XposedHelpers.callMethod(request,"getHttpBody")));
                }
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    super.afterHookedMethod(param);
                    Object request= param.args[1];
                    LogUtils.printParams("X.0Jo send_http_request after ",param.args);

                }
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
}

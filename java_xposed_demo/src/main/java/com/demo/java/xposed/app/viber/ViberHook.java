package com.demo.java.xposed.app.viber;

import android.util.Log;

import com.demo.java.xposed.utils.LogUtils;
import com.google.gson.Gson;

import java.util.Objects;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class ViberHook implements IXposedHookLoadPackage {
    boolean isConnect = false;
    private static String requestId = "";
    private static String CMD = "";
    private static String UsersDetails = "UsersDetails";
    private String TARGET = "Viber";
    public boolean isLogin = false;



    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {
        String VIBER_PACKAGE_NAME = "com.viber.voip";
        if (!loadPackageParam.packageName.contains(VIBER_PACKAGE_NAME)) {
            return;
        }
//        try {
//            /**
//             *
//             */
//            XposedHelpers.findAndHookMethod("com.viber.jni.im2.Im2ReceiverImpl", loadPackageParam.classLoader, "onIM2MessageReceived", long.class, new XC_MethodHook() {
//                @Override
//                public void afterHookedMethod(MethodHookParam param) {
//                    try {
//                        Long data = (Long) param.args[0];
//                        LogUtils.show("afterHookedMethod====>" + data);
//                        String message = (String) XposedHelpers.callStaticMethod(XposedHelpers.findClass("com.viber.jni.im2.Im2MessageNative", loadPackageParam.classLoader), "getMessageJson", data);
//                        JSONObject jsonObject = JSON.parseObject(message);
//                        LogUtils.show("JSONObject====>" + jsonObject);
//                        if (message.contains(UsersDetails)) {
//                            JSONArray jsonArray = jsonObject.getJSONArray(UsersDetails);
//                            LogUtils.show("jsonArray====>" + jsonArray);
//                            //jsonarr.size=0时等于筛号失败.不做处理。因为有时候会莫名其妙触发userDetails={}，有消息来时，只请求到{}时就当作超时处理
//
//
//                        }
//                    } catch (Exception e2) {
//                        LogUtils.show("message___error====>" + e2.getMessage());
//                    }
//                }
//            });
//            XposedHelpers.findAndHookMethod("com.viber.jni.NativeEngineBridge", loadPackageParam.classLoader, "isConnected", new XC_MethodHook() {
//                @Override
//                public void afterHookedMethod(MethodHookParam param) throws Throwable {
//                    boolean isConnected = (boolean) param.getResult();
//                    LogUtils.show("isConnected===>" + isConnected);
//
//
//                }
//            });
//            XposedHelpers.findAndHookMethod("com.viber.voip.HomeActivity", loadPackageParam.classLoader, "onCreate", Bundle.class, new XC_MethodHook() {
//                @Override
//                public void afterHookedMethod(MethodHookParam param) throws Throwable {
//                    Context context = (Context) param.thisObject;
//                    Log.e("xposed !!!!!!!", "connect=======");
//                    String[] data_array ={"+8613373013509","+85212345678"};
//                    LogUtils.show("findAndHookMethod===>" + Arrays.toString(data_array));
//                    ViberUtil.filterPhoneNumber(loadPackageParam.classLoader, data_array);
//
//                }
//            });
//
//            XposedHelpers.findAndHookMethod("com.viber.voip.pixie.PixieProxySelector", loadPackageParam.classLoader, "isFrontingDomain", java.lang.String.class, new XC_MethodHook() {
//                @Override
//                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
//                    super.beforeHookedMethod(param);
//                }
//
//                @Override
//                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
//                    super.afterHookedMethod(param);
//                    String str = (String) param.args[0];
//                    Log.e("xposed", "str:" + str);
//                    param.setResult(false);
//                }
//            });
//
//        } catch (Throwable t) {
//
//            Log.e("Throwable !!!!!!!", Objects.requireNonNull(t.getMessage()));
//
//        }


        try {
            XposedHelpers.findAndHookMethod("com.viber.jni.im2.Im2ReceiverImpl", loadPackageParam.classLoader, "processMessage", long.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    super.beforeHookedMethod(param);
                    Long data = (Long) param.args[0];
                    LogUtils.show("NEW_afterHookedMethod====>" + data);
                    String message = (String) XposedHelpers.callStaticMethod(XposedHelpers.findClass("com.viber.jni.im2.Im2MessageNative", loadPackageParam.classLoader), "getMessageJson", data);
                    LogUtils.show("NEW_JSONObject====>" + message);
                }
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    super.afterHookedMethod(param);
                }
            });




            XposedHelpers.findAndHookMethod("com.viber.voip.HomeActivity", loadPackageParam.classLoader, "onCreate", android.os.Bundle.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    super.beforeHookedMethod(param);
                    LogUtils.show("HomeActivity===>");

                }
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    super.afterHookedMethod(param);
                }
            });
        }catch (Throwable t) {

            Log.e("My_Throwable !!!!!!!", Objects.requireNonNull(t.getMessage()));

        }
    }


}

package com.demo.java.xposed.rcs.hook.gms;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Process;
import android.text.TextUtils;
import android.widget.Toast;

import com.demo.java.xposed.rcs.fake.FakerSimMessage;
import com.demo.java.xposed.base.BaseAppHook;
import com.demo.java.xposed.rcs.hook.messages.CommonMessageClass;
import com.demo.java.xposed.rcs.pdu.BuildSmsUtils;
import com.demo.java.xposed.caller.BroadcastDispatcher;
import com.demo.java.xposed.utils.LogUtils;
import com.demo.java.xposed.utils.PrintStack;
import com.demo.java.xposed.utils.collection.StringUtils;
import com.google.gson.Gson;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class SmsMessageHook extends BaseAppHook {

    private static final String CHANNEL_ID = "xposed_channel";

    @SuppressLint("PrivateApi")
    public static void run(XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {

        LogUtils.show("hook SmsMessage");
        //hook called com.android.internal.telephony.gsm.SmsMessage.createFromPdu([B)
        Class<?> smsMessageClass = XposedHelpers.findClass("com.android.internal.telephony.gsm.SmsMessage", loadPackageParam.classLoader);
        XposedHelpers.findAndHookMethod(
                smsMessageClass,
                "createFromPdu", byte[].class, new XC_MethodHook() {

                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
//                        PrintStack.printStack("createFromPdu");
                        String originalHex = StringUtils.bytesToHexString((byte[]) param.args[0]);
                        LogUtils.show("SmsMessage createFromPdu beforeHookedMethod originalHex " + originalHex);
                        super.beforeHookedMethod(param);


                    }

                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        LogUtils.show("createFromPdu afterHookedMethod" + Arrays.toString(param.args));
                        super.afterHookedMethod(param);

                        Object result = param.getResult();
                        Class<?> targetClass = result.getClass();

                        java.lang.reflect.Method getMessageBody = targetClass.getMethod("getMessageBody");
                        java.lang.reflect.Method getOriginatingAddress = targetClass.getMethod("getOriginatingAddress");
                        String message = "message_body = " + getMessageBody.invoke(result) + " " + "originating_address = " + getOriginatingAddress.invoke(result);
                        LogUtils.show("createFromPdu new message " + message);
                        try {
//                            showToast(RcsApplicationHook.mContext, message);
                        } catch (Exception e) {
                            LogUtils.show("SmsMessage createFromPdu afterHookedMethod error " + e);
                        }

                    }
                });


        //"com.google.android.apps.messaging.shared.receiver.SmsDeliverReceiver"
        String methodName= CommonMessageClass.smsDeliverReceiverMethod;
        LogUtils.show("SmsDeliverReceiver method "+methodName);
        XposedHelpers.findAndHookMethod("com.google.android.apps.messaging.shared.receiver.SmsDeliverReceiver", loadPackageParam.classLoader,methodName, android.content.Context.class, android.content.Intent.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {

                handleMessage(param, "SmsDeliverReceiver");

                super.beforeHookedMethod(param);
            }

            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                LogUtils.show("SmsDeliverReceiver afterHookedMethod");
                super.afterHookedMethod(param);
            }
        });


        XposedHelpers.findAndHookMethod("com.google.android.apps.messaging.shared.receiver.ConfigSmsReceiver", loadPackageParam.classLoader, CommonMessageClass.configSmsReceiverMethod, android.content.Context.class, android.content.Intent.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                if (handleAction(param,loadPackageParam.classLoader)){
                    LogUtils.show("handleAction has processed");
                    return;
                }
                handleMessage(param, "ConfigSmsReceiver");

                super.beforeHookedMethod(param);
            }

            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
            }
        });


    }
    static  boolean handleAction(XC_MethodHook.MethodHookParam param,ClassLoader classLoader){
        Intent intent = (Intent) param.args[1];
        Context context = (Context) param.args[0];
        String xpAction = intent.getStringExtra("xp_action");
        LogUtils.show("handleAction  beforeHookedMethod intent= " + intentToJson(intent) +" xp_action=" +xpAction);

        if (TextUtils.isEmpty(xpAction)){
            return  false;
        }
        BroadcastDispatcher.handle(context, intent, classLoader);
        return true;
    }


    static void handleMessage(XC_MethodHook.MethodHookParam param, String tag) {
        try {
            Intent intent = (Intent) param.args[1];
            Context context = (Context) param.args[0];
            LogUtils.show(tag + " beforeHookedMethod intent= " + intentToJson(intent));
            String verifyCode = intent.getStringExtra("aa");
            String body = intent.getStringExtra("bb");
            if (body == null && verifyCode == null) {
                LogUtils.show("body == null && verifyCode == null " + intentToJson(intent));
                return;

            }
            String sender = intent.getStringExtra("sender");
            String serviceCenter = intent.getStringExtra("serviceCenter");
            int expectedPort = 37273;
            int subId = FakerSimMessage.getDefaultSubId(context);

            if (body == null || body.isEmpty()) {
                if (verifyCode != null && !verifyCode.isEmpty()) {
                    LogUtils.show(tag + " beforeHookedMethod handler verify message=" + verifyCode);
                    body = "Your Messenger verification code is G-" + verifyCode;
                }
            }
            if (sender == null || sender.isEmpty()) {
                sender = "JibeRCS";
            }

            if (serviceCenter == null || serviceCenter.isEmpty()) {
                serviceCenter = "+4921197713795";

            }


            Intent fakeIntent = BuildSmsUtils.getSmsIntent(sender, body, subId, serviceCenter, intent.getAction(), randLong((long) Process.myPid()), intent.getComponent());
            LogUtils.show("fakeIntent = " + intentToJson(fakeIntent) + " sender = " + sender + " body = " + body + " subId = " + subId + " serviceCenter = " + serviceCenter);
            param.args[1] = fakeIntent;
        } catch (Exception e) {
            PrintStack.printStackErrInfo(tag, e);
        }
    }


    static void showToast(final Context context, final String message) {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context, message, Toast.LENGTH_LONG).show();
                Toast.makeText(context, message, Toast.LENGTH_LONG).show();
                Toast.makeText(context, message, Toast.LENGTH_LONG).show();
            }
        });
    }


    public static String intentToJson(Intent intent) {
        if (intent == null) {
            try {
                Map<String, Object> intentMap = new HashMap<>();
                intentMap.put("action", intent.getAction() != null ? intent.getAction() : "");
                intentMap.put("data", intent.getDataString() != null ? intent.getDataString() : "");
                intentMap.put("type", intent.getType() != null ? intent.getType() : "");
                intentMap.put("package", intent.getPackage() != null ? intent.getPackage() : "");
                intentMap.put("component", intent.getComponent() != null ? intent.getComponent().getClassName() : "");
                intentMap.put("flags", intent.getFlags() != 0 ? intent.getFlags() : null);
                intentMap.put("categories", intent.getCategories() != null ? intent.getCategories() : "");
                intentMap.put("extras", intent.getExtras() != null ? convertBundleToMap(intent.getExtras()) : "");
                Gson gson = new Gson();
                return gson.toJson(intentMap);
            } catch (Exception e) {

                LogUtils.show("err " +intent +"intentToJson error " + e );
            }

        }
        return intent.toString();
    }

    private static Map<String, Object> convertBundleToMap(Bundle extras) {
        Map<String, Object> extrasMap = new HashMap<>();
        for (String key : extras.keySet()) {
            extrasMap.put(key, extras.get(key));
        }
        return extrasMap;
    }

    public static Long randLong(Long seed) {
        Random random = new Random(seed);
        return Math.abs(random.nextLong());
    }


    public static Long randLong() {
        Random random = new Random();
        return Math.abs(random.nextLong());
    }


}

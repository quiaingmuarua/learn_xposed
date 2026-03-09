package com.demo.java.xposed.rcs.hook;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.text.TextUtils;

import com.demo.java.xposed.base.BaseAppHook;
import com.demo.java.xposed.caller.WebsocketDispatcher;
import com.demo.java.xposed.caller.XposedHttpServer;
import com.demo.java.xposed.device.PluginInit;
import com.demo.java.xposed.device.model.SimInfoModel;
import com.demo.java.xposed.rcs.SimInfoFingerPrint;
import com.demo.java.xposed.rcs.apiCaller.core.GrpcCallHelper;
import com.example.sekiro.messages.core.GrpcCallSender;
import com.demo.java.xposed.rcs.apiCaller.core.RcsCommandRegistry;
import com.demo.java.xposed.rcs.hook.gms.SmsMessageHook;
import com.demo.java.xposed.rcs.hook.messages.AutoRegisterHook;
import com.demo.java.xposed.rcs.hook.messages.CommonMessageClass;
import com.demo.java.xposed.rcs.hook.messages.DeliverReport;
import com.demo.java.xposed.rcs.hook.messages.LoadDexHook;
import com.demo.java.xposed.rcs.hook.messages.LogHook;
import com.demo.java.xposed.rcs.hook.messages.QuickUiHook;
import com.demo.java.xposed.rcs.hook.messages.RcsInnerInfoHook;
import com.demo.java.xposed.rcs.hook.messages.RcsStatusHook;
import com.demo.java.xposed.rcs.hook.messages.RegisterHook;
import com.demo.java.xposed.rcs.hook.messages.SqliteHook;
import com.demo.java.xposed.rcs.hook.protocol.GrpcHook;
import com.demo.java.xposed.rcs.hook.protocol.ProtoHook;
import com.demo.java.xposed.rcs.hook.protocol.RcsProtocolHook;
import com.demo.java.xposed.rcs.hook.protocol.TachyonTokenHook;
import com.demo.java.xposed.rcs.model.KeyCommonInfo;
import com.demo.java.xposed.rcs.model.KeyInfo;
import com.demo.java.xposed.rcs.model.RegisterKeyInfo;
import com.demo.java.xposed.utils.CacheUtils;
import com.demo.java.xposed.utils.LogUtils;
import com.demo.java.xposed.utils.PrintStack;
import com.demo.java.xposed.utils.ReliableWebSocketClient;
import com.example.sekiro.messages.cache.XposedClassCacher;
import com.example.sekiro.messages.shared.CommandContext;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import fi.iki.elonen.NanoHTTPD;

public class RcsApplicationHook extends BaseAppHook {

    public static Context mContext;
    public static ClassLoader mLoader;

    public static String appVersion;

    public static SimInfoModel simInfoModel ;

    private static XposedHttpServer server = null;
    private static boolean serverStarted = false;

    public static void run(XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {
        simInfoModel= SimInfoModel.getInstance(loadPackageParam.packageName);
        XposedHelpers.findAndHookMethod(Application.class, "attach",
                Context.class,
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) {
                        LogUtils.show("Application attach");
                        processInit(loadPackageParam,param);

                    }
                });

        // Hook ActivityThread 的 currentActivityThread 方法
        XposedHelpers.findAndHookMethod("android.app.ActivityThread", loadPackageParam.classLoader, "currentActivityThread", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                Object activityThread = param.getResult();

                // 获取当前的 Application
                Application application = (Application) XposedHelpers.callMethod(activityThread, "getApplication");
                if (application != null) {
                    Context context = application.getApplicationContext();
                    LogUtils.show("currentActivityThread context: " + context);
                }
            }
        });

        XposedHelpers.findAndHookMethod("com.google.android.apps.messaging.main.MainActivity", loadPackageParam.classLoader, "onCreate", android.os.Bundle.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);
                LogUtils.show("MainActivity onCreate");
                processInit(loadPackageParam,param);
            }

            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
            }
        });


    }

    public static void processInit(XC_LoadPackage.LoadPackageParam loadPackageParam,XC_MethodHook.MethodHookParam param){

        try {
            if (mContext!=null){
                LogUtils.show("mContext is not null");
                return;
            }
            LogUtils.show("processInit run");
            mContext = (Context) XposedHelpers.callMethod(param.thisObject, "getApplicationContext");
            mLoader = mContext.getClassLoader();
            appVersion = appVersion(mContext);
            LogUtils.show("MainActivity 拿到appVersion= " + appVersion);
            getSharedPreferences(mContext);
            CommandContext.init(mContext);
            CacheUtils.checkAndRecordLaunchTime(mContext);
//            initWebSocket(mContext,mLoader);
            processHook(loadPackageParam, appVersion);
            SimInfoFingerPrint.handTelephonyManager(mContext);
            LoadDexHook.run(mContext);
//            runHttpServer(mContext);   //nano http
            RegisterKeyInfo.getInstance().setTimeRecord("lastLaunchTime", Objects.requireNonNull(CacheUtils.getLastLaunchTime(mContext)));
            RegisterKeyInfo.getInstance().setTimeRecord("cacheClearedTime", Objects.requireNonNull(CacheUtils.getLastCacheClearedTime(mContext)));

        } catch (Exception e) {
            PrintStack.printStackErrInfo("RcsApplicationHook run Exception", e);

        } catch (Throwable e) {
            PrintStack.printStackErrInfo("RcsApplicationHook run Throwable",e);
        }

    }


    public static void processHook(XC_LoadPackage.LoadPackageParam loadPackageParam, String appVersion) throws Throwable {

        if (appVersion.contains("20240519")) {
            LogUtils.show("20240519 is match now is hook version " + appVersion);
            //grpc远程调用相关

            CommonMessageClass.run(loadPackageParam);
            RegisterHook.run(loadPackageParam);
            XposedClassCacher.run(loadPackageParam);
            GrpcCallSender.run(loadPackageParam);
            RcsCommandRegistry.init(loadPackageParam);
            SmsMessageHook.run(loadPackageParam);


            GrpcCallHelper.run(loadPackageParam);
            DeliverReport.runV240519(loadPackageParam);
            RegisterHook.runV240519(loadPackageParam);
            RcsStatusHook.runV240519(loadPackageParam);

            LogHook.runV240519(loadPackageParam);
            RcsInnerInfoHook.run(loadPackageParam);
            GrpcHook.run(loadPackageParam);
            RcsProtocolHook.run(loadPackageParam);
            TachyonTokenHook.run(loadPackageParam);
            ProtoHook.run(loadPackageParam);
            QuickUiHook.run(loadPackageParam);
            SqliteHook.run(loadPackageParam);
            return;
        }

        if(appVersion.contains("20250319")){
            RegisterHook.runV20250319(loadPackageParam);
            RcsStatusHook.runV20250319(loadPackageParam);
            DeliverReport.runV20250319(loadPackageParam);
            LogHook.runV20250319(loadPackageParam);
        }

        if (appVersion.contains("20240213")) {
            LogUtils.show("20240213 is match now is hook version " + appVersion);
            if (!PluginInit.isOriginalSim) {
                LogUtils.show("now is original sim");
                AutoRegisterHook.run(loadPackageParam);

            }
            RegisterHook.runV20240213(loadPackageParam);
            RcsStatusHook.run(loadPackageParam);
            DeliverReport.run(loadPackageParam);
            if (PluginInit.isDebug) {
                LogHook.run(loadPackageParam);
            }
        }

    }


    //get app version
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


    public static void getSharedPreferences(Context context) {
        Gson gson = new Gson();
        Map<String, String> accountInfo = getSharedPreferencesResult(context);

        LogUtils.show("getSharedPreferences " + accountInfo);
        RegisterKeyInfo.getInstance().setAccountInfo(accountInfo);
        KeyInfo.printLog(new KeyInfo(KeyCommonInfo.Tag.environment, accountInfo));


    }

    public static Map<String, String> getSharedPreferencesResult(Context context) {
        SharedPreferences bugleSp = context.getSharedPreferences("bugle", Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String result = gson.toJson(bugleSp.getAll());
        Map<String, String> resultMap = new HashMap<String, String>();
        resultMap.put("newest", "true");
        resultMap.put("login_status", "false");
        resultMap.put("msg", "未激活");
        LogUtils.show("getSharedPreferencesResult  " + result);
        if (result.contains("provisioning_engine_rcs_configuration") || result.contains("tachyon_registration_token")) {
            String simID = bugleSp.getString("UPSELL_PROMPT_SIM_ID", "");
            String phoneNumber = bugleSp.getString("msisdn_for_iccid_" + simID, "");
            if (!simID.isEmpty() && !phoneNumber.isEmpty()) {
                LogUtils.show("rcs 已激活");
                resultMap.put("login_status", "true");
                resultMap.put("iccId", simID);
                resultMap.put("phoneNumber", phoneNumber);
                resultMap.put("msg", "已激活");

            } else {
                LogUtils.show("rcs 激活中");
                resultMap.put("login_status", "false");
                resultMap.put("iccId", simID);
                resultMap.put("msg", "激活中");

            }
            //when simID is not empty, simInfo iccId is not empty and they value aren't equal
            if (!simID.isEmpty() && !simInfoModel.getIccId().isEmpty() && !simID.equals(simInfoModel.getIccId())) {
                resultMap.put("newest", "false");
            }

        } else {
            LogUtils.show("rcs 未激活");
        }

        return resultMap;

    }



    private static  void initWebSocket(Context context,ClassLoader classLoader) {
//        ReliableWebSocketClient socket = ReliableWebSocketClient.getInstance(context, "ws://192.168.6.210:18765");
        ReliableWebSocketClient socket = ReliableWebSocketClient.getInstance(context, "ws://34.143.182.93:2052");

        socket.setListener(new ReliableWebSocketClient.WebSocketEventListener() {
            @Override public void onOpen() {
                LogUtils.show( "Connected to WebSocket");
//                Gson gson=new Gson();
//                socket.send(gson.toJson(new ApiRequest(0,"1223123123",1)));
            }

            @Override public void onMessage(String message) {
                LogUtils.show( "ReliableWebSocketClient Received onMessage: " + message);
                // 可选：传给自定义命令分发器
              String response = WebsocketDispatcher.handle(message);
              if(!TextUtils.isEmpty(response)){
                  socket.send(response);
              }
            }

            @Override public void onClosed(String reason) {
                LogUtils.show("WebSocket closed: " + reason);
            }

            @Override public void onFailure(String error) {
                LogUtils.show( "WebSocket error: " + error);
            }
        });

        socket.connect(); // 持久连接
    }



    public static void runHttpServer(Context context) {
        if (!serverStarted) {
            serverStarted = true;

            new Thread(() -> {
                try {
                    server = new XposedHttpServer(18899);
                    server.start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);
                    LogUtils.show("runHttpServer Xposed HTTP server started on port 18899");
                } catch (IOException e) {
                    LogUtils.printStackErrInfo("Failed to start HTTP server: ", e);
                    serverStarted = false; // 失败后重置
                }
            }).start();
        } else {
            LogUtils.show("HTTP server already started");
        }
    }





}

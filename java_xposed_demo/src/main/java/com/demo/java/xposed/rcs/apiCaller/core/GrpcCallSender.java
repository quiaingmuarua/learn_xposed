package com.demo.java.xposed.rcs.apiCaller.core;

import com.demo.java.xposed.base.BaseAppHook;
import com.demo.java.xposed.utils.LogUtils;
import com.example.sekiro.messages.cache.XposedClassCacher;
import com.example.sekiro.messages.shared.CommandException;
import com.example.sekiro.messages.shared.ErrorCode;

import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * 帮助主动发起 gRPC 请求
 */
public class GrpcCallSender  extends BaseAppHook {

    private static final int RESPONSE_TIMEOUT_SECONDS = 10; // 默认超时时间

    // 方法名管理（防止混淆变更时难以维护）
    private static final String METHOD_ON_CLOSE = "a";
    private static final String METHOD_ON_HEADERS = "b";
    private static final String METHOD_ON_MESSAGE = "c";



    private static final  String METHOD_ON_START="a";
    private static final  String METHOD_ON_SENDMESSAGE="f";
    private static final  String METHOD_ON_HALFCLOSE="d";
    private static final  String METHOD_ON_REQUEST="e";

    private static final Map<Object, BlockingQueue<Object>> listenerQueueMap = new ConcurrentHashMap<>();

    private static final AtomicBoolean listenerHooked = new AtomicBoolean(false);


    //初始化需要hook init
    public static void run(XC_LoadPackage.LoadPackageParam loadPackageParam) {
        if (!listenerHooked.compareAndSet(false, true)) return;
        hookListener();
    }


    /**
     * 主动发起 gRPC 请求（统一封装）
     *
     * @param classLoader          ClassLoader
     * @param internalGrpcChannel  InternalGrpcChannel实例（hook缓存）
     * @param methodDescriptor     MethodDescriptor实例（hook缓存）
     * @param requestMessage       请求对象（protobuf）
     * @return 响应对象（protobuf Response），超时返回 null
     */
     static Object sendGrpc(ClassLoader classLoader,
                                  Object internalGrpcChannel,
                                  Object methodDescriptor,
                                  Object requestMessage, Object metadata) throws Exception {
            // 构建 CallOptions
            Object callOptions = buildBlockingCallOptions();

            // newCall() 新建 ClientCallImpl
            Object clientCall = XposedHelpers.callMethod(
                    internalGrpcChannel,
                    "a",  // newCall 方法名
                    methodDescriptor,
                    callOptions
            );

            // 构建动态监听器
            BlockingQueue<Object> queue = new ArrayBlockingQueue<>(1);
            Object listener = buildClientCallListener(classLoader, queue);

            // start + send
            XposedHelpers.callMethod(clientCall, METHOD_ON_START, listener, metadata);
            XposedHelpers.callMethod(clientCall, METHOD_ON_SENDMESSAGE, requestMessage);
            XposedHelpers.callMethod(clientCall, METHOD_ON_HALFCLOSE);
            XposedHelpers.callMethod(clientCall, METHOD_ON_REQUEST, 1);

            // 同步等待响应
            Object response = queue.poll(RESPONSE_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            LogUtils.show("sendGrpc response = " + response );
            listenerQueueMap.remove(listener);
            if (response == null) {
                LogUtils.show("sendGrpc Timeout waiting for gRPC response");
                throw new CommandException(ErrorCode.TIMEOUT_ERROR,"Timeout waiting for gRPC response");
            }
            return response;

    }

    /**
     * 构造 CallOptions
     */
    private static Object buildBlockingCallOptions() {
        try {
            Class<?> callOptionsClass = XposedClassCacher.callOptionsClass;
            Object callOptions = XposedHelpers.getStaticObjectField(callOptionsClass, "a");

            Class<?> deadlineClass = XposedClassCacher.deadlineClass;
            Object deadline = XposedHelpers.callStaticMethod(deadlineClass, "c", (long) RESPONSE_TIMEOUT_SECONDS, TimeUnit.SECONDS);

            XposedHelpers.setObjectField(callOptions, "b", deadline);
            XposedHelpers.setObjectField(callOptions, "e", "gzip"); // 设置压缩器 gzip
            return callOptions;
        } catch (Throwable e) {
            throw new RuntimeException("Failed to build CallOptions", e);
        }
    }


    /**
     * 构建 ClientCallListener 动态代理
     */

    private static Object buildClientCallListener(ClassLoader classLoader, BlockingQueue<Object> queue) {
        try {
            Class<?> clientCallListenerClass = XposedClassCacher.clientCallListenerClass;
            Object listener = XposedHelpers.newInstance(clientCallListenerClass);
            listenerQueueMap.put(listener, queue);
            return listener;
        } catch (Throwable e) {
            throw new RuntimeException("Failed to build ClientCallListener", e);
        }
    }




    private static  void hookListener(){
        Class<?> clientCallListenerClass = XposedClassCacher.clientCallListenerClass;
        Class<?> MetadataGrpc = XposedClassCacher.MetadataGrpcClass;
        XposedHelpers.findAndHookMethod(clientCallListenerClass, METHOD_ON_MESSAGE, java.lang.Object.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);
                Object msg = param.args[0];
                Object listenerThis = param.thisObject;
                BlockingQueue<Object> queue = listenerQueueMap.get(listenerThis);
                if (queue != null) {
                    LogUtils.show("buildClientCallListener onMessage → " + msg);
                    queue.offer(msg);
                } else {
                    LogUtils.show("buildClientCallListener onMessage → no queue found for listener = " + listenerThis);
                }
            }
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
            }
        });

        XposedHelpers.findAndHookMethod(clientCallListenerClass, METHOD_ON_HEADERS,MetadataGrpc, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);
                LogUtils.show("buildClientCallListener METHOD_ON_HEADERS " + param.args[0]);
            }
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
            }
        });
    }

}

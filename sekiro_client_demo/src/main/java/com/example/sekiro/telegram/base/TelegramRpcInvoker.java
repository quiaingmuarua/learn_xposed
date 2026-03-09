package com.example.sekiro.telegram.base;



import com.example.sekiro.telegram.TelegramEnv;
import com.example.sekiro.util.SimpleLogUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class TelegramRpcInvoker {

    private static final ConcurrentMap<String, PendingRequest> PENDING_MAP = new ConcurrentHashMap<>();

    private final TelegramEnv env;
    private final TelegramResponseSerializer serializer;

    public TelegramRpcInvoker(TelegramEnv env, TelegramResponseSerializer serializer) {
        this.env = env;
        this.serializer = serializer;
    }

    public String sendRequestSync(Object tlRequest, long timeoutMs) throws Exception {
        String requestId = UUID.randomUUID().toString();
        PendingRequest pending = new PendingRequest(requestId);
        PENDING_MAP.put(requestId, pending);

        try {
            Object connectionsManager = getConnectionsManager();
            Method sendRequestMethod = findSendRequestMethod();

            Class<?> requestDelegateClz = env.loadClass("org.telegram.tgnet.RequestDelegate");

            Object delegate = Proxy.newProxyInstance(
                    env.getClassLoader(),
                    new Class[]{requestDelegateClz},
                    (proxy, method, args) -> {
                        String methodName = method.getName();

                        // 过滤 Object 基础方法
                        if (method.getDeclaringClass() == Object.class) {
                            switch (methodName) {
                                case "toString":
                                    return "TelegramRequestDelegateProxy@" +
                                            Integer.toHexString(System.identityHashCode(proxy));
                                case "hashCode":
                                    return System.identityHashCode(proxy);
                                case "equals":
                                    return proxy == args[0];
                                default:
                                    return null;
                            }
                        }

                        // 只处理 Telegram 回调 run(response, error)
                        if (!"run".equals(methodName)) {
                            SimpleLogUtils.show("[TelegramRpcInvoker] ignore delegate method: " + methodName);
                            return null;
                        }

                        handleDelegateCallback(requestId, args);
                        return null;
                    }
            );

            sendRequestMethod.invoke(connectionsManager, tlRequest, delegate);

            boolean ok = pending.await(timeoutMs);
            if (!ok) {
                throw new RuntimeException("Telegram request timeout, requestId=" + requestId);
            }

            if (!pending.isSuccess()) {
                throw new RuntimeException("Telegram request failed, requestId=" + requestId
                        + ", error=" + pending.getErrorMsg());
            }

            return pending.getResultJson();

        } finally {
            PENDING_MAP.remove(requestId);
        }
    }

    private void handleDelegateCallback(String requestId, Object[] args) {
        PendingRequest pending = PENDING_MAP.get(requestId);
        if (pending == null) {
            SimpleLogUtils.show("[TelegramRpcInvoker] pending request missing, requestId=" + requestId);
            return;
        }

        try {
            Object response = (args != null && args.length > 0) ? args[0] : null;
            Object error = (args != null && args.length > 1) ? args[1] : null;

            if (error != null) {
                pending.fail(extractTelegramError(error));
                return;
            }

            if (response == null) {
                pending.fail("Telegram callback both response and error are null");
                return;
            }

            String json = serializer.toJson(response);
            pending.success(json);

        } catch (Throwable t) {
            pending.fail("delegate callback parse error: " + t.getMessage());
        }
    }

    private Object getConnectionsManager() throws Exception {
        Class<?> cmCls = env.loadClass("org.telegram.tgnet.ConnectionsManager");
        Method getInstance = cmCls.getMethod("getInstance", int.class);
        return getInstance.invoke(null, env.getCurrentAccount());
    }

    private Method findSendRequestMethod() throws Exception {
        Class<?> cmCls = env.loadClass("org.telegram.tgnet.ConnectionsManager");
        Class<?> tlObjectClz = env.loadClass("org.telegram.tgnet.TLObject");
        Class<?> requestDelegateClz = env.loadClass("org.telegram.tgnet.RequestDelegate");

        return cmCls.getMethod("sendRequest", tlObjectClz, requestDelegateClz);
    }

    private String extractTelegramError(Object errorObj) {
        if (errorObj == null) {
            return "unknown telegram error";
        }

        try {
            Class<?> errorClass = errorObj.getClass();

            String text = null;
            Integer code = null;

            try {
                java.lang.reflect.Field textField = errorClass.getDeclaredField("text");
                textField.setAccessible(true);
                Object val = textField.get(errorObj);
                if (val != null) {
                    text = String.valueOf(val);
                }
            } catch (Throwable ignore) {
            }

            try {
                java.lang.reflect.Field codeField = errorClass.getDeclaredField("code");
                codeField.setAccessible(true);
                Object val = codeField.get(errorObj);
                if (val instanceof Integer) {
                    code = (Integer) val;
                }
            } catch (Throwable ignore) {
            }

            if (code != null && text != null) {
                return "code=" + code + ", text=" + text;
            }
            if (text != null) {
                return text;
            }
            return String.valueOf(errorObj);

        } catch (Throwable t) {
            return "parse telegram error failed: " + t.getMessage();
        }
    }
}
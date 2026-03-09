package com.example.sekiro.util;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import cn.iinti.sekiro3.business.api.SekiroClient;
import cn.iinti.sekiro3.business.api.interfaze.ActionHandler;
import cn.iinti.sekiro3.business.api.interfaze.HandlerRegistry;
import cn.iinti.sekiro3.business.api.interfaze.SekiroRequest;
import cn.iinti.sekiro3.business.api.interfaze.SekiroResponse;

public class SekiroUtil {

    private static final String SERVER_HOST = "1.12.219.17";
    private static final int SERVER_PORT = 5612;
    private static final String PREFS_NAME = "AppCachePreferences";
    private static final String KEY_UUID_PREFIX = "uuid_key_";

    /**
     * 每个 group 对应一个 client
     */
    private static final Map<String, SekiroClient> CLIENT_MAP = new ConcurrentHashMap<>();

    /**
     * 每个 group 对应一个 clientId 缓存
     */
    private static final Map<String, String> CLIENT_ID_CACHE = new ConcurrentHashMap<>();

    @FunctionalInterface
    public interface SekiroLambda {
        void handle(SekiroRequest req, SekiroResponse resp) throws Throwable;
    }

    public static synchronized void init(String group, Context context, List<ActionHandler> requestHandlers) {
        if (group == null || group.trim().isEmpty()) {
            throw new IllegalArgumentException("group can not be null or empty");
        }

        if (CLIENT_MAP.containsKey(group)) {
            return;
        }

        try {
            SekiroClient client = new SekiroClient(
                    group,
                    getOrCacheClientId(context, group),
                    SERVER_HOST,
                    SERVER_PORT
            );

            client.setupSekiroRequestInitializer((sekiroRequest, handlerRegistry) -> {
                registerHandlers(handlerRegistry, buildAllHandlers(requestHandlers));
            }).start();

            CLIENT_MAP.put(group, client);
        } catch (Exception e) {
            throw new RuntimeException("Sekiro init failed, group=" + group, e);
        }
    }

    public static boolean isStarted(String group) {
        return group != null && CLIENT_MAP.containsKey(group);
    }

    public static SekiroClient getClient(String group) {
        if (group == null) {
            return null;
        }
        return CLIENT_MAP.get(group);
    }

    public static List<String> getStartedGroups() {
        return new ArrayList<>(CLIENT_MAP.keySet());
    }

    public static ActionHandler action(String action, SekiroLambda lambda) {
        return new ActionHandler() {
            @Override
            public String action() {
                return action;
            }

            @Override
            public void handleRequest(SekiroRequest req, SekiroResponse resp) {
                try {
                    lambda.handle(req, resp);
                } catch (Throwable t) {
                    String errorMsg = t.getMessage() == null ? String.valueOf(t) : t.getMessage();
                    resp.failed(errorMsg);
                }
            }
        };
    }

    public static synchronized String getOrCacheClientId(Context context, String group) {
        if (group == null || group.trim().isEmpty()) {
            throw new IllegalArgumentException("group can not be null or empty");
        }

        String cachedClientId = CLIENT_ID_CACHE.get(group);
        if (cachedClientId != null) {
            return cachedClientId;
        }

        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String key = KEY_UUID_PREFIX + group;
        String clientId = prefs.getString(key, null);

        if (clientId == null) {
            clientId = UUID.randomUUID().toString();
            prefs.edit().putString(key, clientId).apply();
        }

        CLIENT_ID_CACHE.put(group, clientId);
        return clientId;
    }

    private static List<ActionHandler> buildAllHandlers(List<ActionHandler> externalHandlers) {
        List<ActionHandler> handlers = new ArrayList<>();
        handlers.addAll(buildDefaultHandlers());

        if (externalHandlers != null && !externalHandlers.isEmpty()) {
            handlers.addAll(externalHandlers);
        }

        return handlers;
    }

    private static void registerHandlers(HandlerRegistry registry, List<ActionHandler> handlers) {
        if (handlers == null || handlers.isEmpty()) {
            return;
        }

        for (ActionHandler handler : handlers) {
            if (handler != null) {
                registry.registerSekiroHandler(handler);
            }
        }
    }

    private static List<ActionHandler> buildDefaultHandlers() {
        List<ActionHandler> handlers = new ArrayList<>();

        handlers.add(action("version", (req, resp) -> {
            resp.success("1.0");
        }));

        handlers.add(action("add", (req, resp) -> {
            int a = req.getIntValue("a");
            int b = req.getIntValue("b");
            resp.success(a + b);
        }));

        return handlers;
    }
}
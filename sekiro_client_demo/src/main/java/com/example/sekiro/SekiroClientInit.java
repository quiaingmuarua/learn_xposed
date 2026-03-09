package com.example.sekiro;

import static com.example.sekiro.SekiroLambda.action;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.command.util.SimpleLogUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import cn.iinti.sekiro3.business.api.SekiroClient;
import cn.iinti.sekiro3.business.api.interfaze.ActionHandler;
import cn.iinti.sekiro3.business.api.interfaze.HandlerRegistry;

public class SekiroClientInit {
    private static volatile SekiroClient client;
    private static boolean started = false;

    private static final String SERVER_HOST = "1.12.219.17";
    private static final int SERVER_PORT = 5612;
    private static final String PREFS_NAME = "AppCachePreferences";
    private static final String KEY_UUID = "uuid_key";
    private static volatile String cachedClientId;


    public static synchronized void init(String group, Context context, List<ActionHandler> requestHandlers) {
        if (started) {
            return;
        }

        try {
            client = new SekiroClient(group, getOrCacheClientId(context), SERVER_HOST, SERVER_PORT);
            client.setupSekiroRequestInitializer((sekiroRequest, handlerRegistry) -> registerHandlers(handlerRegistry, buildAllHandlers(requestHandlers))).start();
            started = true;
            SimpleLogUtils.show("Sekiro init success");
        } catch (Exception e) {
            throw new RuntimeException("Sekiro init failed", e);
        }
    }


    public static synchronized String getOrCacheClientId(Context context) {
        if (cachedClientId != null) {
            return cachedClientId;
        }

        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String clientId = prefs.getString(KEY_UUID, null);

        if (clientId == null) {
            clientId = UUID.randomUUID().toString();
            prefs.edit().putString(KEY_UUID, clientId).apply();
        }

        cachedClientId = clientId;
        return clientId;
    }

    private static List<ActionHandler> buildAllHandlers(List<ActionHandler> externalHandlers) {
        List<ActionHandler> handlers = new ArrayList<>(buildDefaultHandlers());

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
            resp.success("4.0");
        }));

        handlers.add(action("info", (req, resp) -> {
            resp.success("info");
        }));

        handlers.add(action("add", (req, resp) -> {
            int a = req.getIntValue("a");
            int b = req.getIntValue("b");
            resp.success(a + b);
        }));

        return handlers;
    }
}
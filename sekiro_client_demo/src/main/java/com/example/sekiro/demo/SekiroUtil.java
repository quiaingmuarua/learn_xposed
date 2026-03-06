package com.example.sekiro.demo;

import java.util.List;
import java.util.UUID;

import android.content.Context;
import android.content.SharedPreferences;

import cn.iinti.sekiro3.business.api.SekiroClient;
import cn.iinti.sekiro3.business.api.interfaze.ActionHandler;
import cn.iinti.sekiro3.business.api.interfaze.HandlerRegistry;
import cn.iinti.sekiro3.business.api.interfaze.SekiroRequest;
import cn.iinti.sekiro3.business.api.interfaze.SekiroResponse;


public class SekiroUtil {
    private static volatile SekiroClient client;
    private static boolean started = false;
    private static final String serverHost = "1.12.219.17";
    private static final int serverPort = 5612;
    private static final String PREFS_NAME = "AppCachePreferences";
    private static final String KEY_UUID = "uuid_key";
    private static volatile String cachedClientId;

    public static synchronized void init(String group, Context context, List<ActionHandler> requestHandlers) {
        if (started) return;

        try {
            client = new SekiroClient(group, getOrCacheClientId(context), serverHost, serverPort);
            client.setupSekiroRequestInitializer((sekiroRequest, handlerRegistry) -> {
                        registerDefaultHandlers(handlerRegistry);
                        // 注册外部传入 handler
                        if (requestHandlers != null) {
                            for (ActionHandler handler : requestHandlers) {
                                handlerRegistry.registerSekiroHandler(handler);
                            }
                        }
                    })
                    .start();
            started = true; // 只有成功后才标记
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

    /**
     * 所有 handler 在这里集中注册
     */
    private static void registerDefaultHandlers(HandlerRegistry registry) {
        registry.registerSekiroHandler(new ActionHandler() {
            @Override
            public String action() {
                return "version";
            }

            @Override
            public void handleRequest(SekiroRequest req, SekiroResponse resp) {
                resp.success("1.0");
            }
        });

        // 示例2：带参数
        registry.registerSekiroHandler(new ActionHandler() {
            @Override
            public String action() {
                return "add";
            }

            @Override
            public void handleRequest(SekiroRequest req, SekiroResponse resp) {
                int a = req.getIntValue("a");
                int b = req.getIntValue("b");
                resp.success(a + b);
            }
        });
    }
}
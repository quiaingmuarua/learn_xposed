package com.example.sekiro.messages.shared;

import android.annotation.SuppressLint;
import android.content.Context;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CommandContext {

    @SuppressLint("StaticFieldLeak")
    private static final CommandContext INSTANCE = new CommandContext();

    private Context context;
    private final Map<Class<?>, Object> services = new ConcurrentHashMap<>();

    public static CommandContext getInstance() {
        return INSTANCE;
    }

    public static void init(Context context) {
        if (context == null) {
            throw new IllegalArgumentException("context is null");
        }
        INSTANCE.context = context.getApplicationContext();
    }

    public Context getContext() {
        if (context == null) {
            throw new IllegalStateException("CommandContext not initialized");
        }
        return context;
    }

    public ClassLoader getClassLoader() {
        return getContext().getClassLoader();
    }

    public Class<?> loadClass(String name) throws ClassNotFoundException {
        return getClassLoader().loadClass(name);
    }

    public <T> void register(Class<T> type, T service) {
        if (type == null || service == null) {
            throw new IllegalArgumentException("type or service is null");
        }
        services.put(type, service);
    }

    public <T> T require(Class<T> type) {
        Object service = services.get(type);
        if (service == null) {
            throw new IllegalStateException("service not registered: " + type.getName());
        }
        return type.cast(service);
    }

    public <T> T get(Class<T> type) {
        Object service = services.get(type);
        return service == null ? null : type.cast(service);
    }
}
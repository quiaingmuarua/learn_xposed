package com.example.sekiro.messages.core;

import com.example.sekiro.messages.shared.ApiResponse;
import com.example.sekiro.messages.shared.CommandException;
import com.example.sekiro.messages.shared.CommandHandler;
import com.example.sekiro.messages.shared.ErrorCode;
import com.example.sekiro.messages.shared.RegisteredHandler;
import com.example.sekiro.util.SimpleLogUtils;

import org.json.JSONObject;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

public class CommandRouter {

    private static final Map<String, RegisteredHandler<?, ?>> handlers = new ConcurrentHashMap<>();
    private static final Map<Class<?>, Function<JSONObject, ?>> defaultResolvers = new ConcurrentHashMap<>();

    private CommandRouter() {
    }

    public static <T> void registerDefaultResolver(Class<T> clazz, Function<JSONObject, T> resolver) {
        defaultResolvers.put(clazz, resolver);
    }

    public static <T, V> void register(String event,
                                       CommandHandler<T, V> handler,
                                       Function<JSONObject, T> resolver) {
        if (event == null || event.trim().isEmpty()) {
            throw new IllegalArgumentException("event is empty");
        }
        if (handler == null) {
            throw new IllegalArgumentException("handler is null");
        }
        if (resolver == null) {
            throw new IllegalArgumentException("resolver is null");
        }
        handlers.put(event, new RegisteredHandler<>(handler, resolver));
    }

    public static Set<String> getRegisterMethods() {
        return handlers.keySet();
    }

    public static ApiResponse<?> dispatch(JSONObject json) {
        String event = json.optString("event", "");
        RegisteredHandler<?, ?> registeredHandler = handlers.get(event);
        ApiResponse<?> result;
        long startTime = System.currentTimeMillis();

        try {
            if (registeredHandler == null) {
                result = new ApiResponse<>(ErrorCode.UNKNOWN_EVENT.code, buildUnknownEventMessage(event));
            } else {
                result = registeredHandler.invoke(json);
            }
        } catch (Exception e) {
            result = ApiResponse.fromException(e);
        }

        long endTime = System.currentTimeMillis();
        result.setDuration(endTime - startTime);
        SimpleLogUtils.show("[CommandRouter] dispatch result=" + resultToLogString(event, result));
        return result;
    }

    private static String resultToLogString(String event, ApiResponse<?> result) {
        return "event=" + event
                + ", status=" + result.status
                + ", code=" + result.code
                + ", message=" + result.message
                + ", duration=" + result.duration;
    }

    private static String buildUnknownEventMessage(String event) {
        String suggestions = handlers.keySet().stream()
                .filter(k -> k.toLowerCase().contains(event.toLowerCase()))
                .collect(Collectors.joining(", "));

        return suggestions.isEmpty()
                ? "未知事件: " + event
                : "未知事件: " + event + "，你是想输入: " + suggestions + "？";
    }

    public static String extractParam(JSONObject json, String... keys) {
        for (String key : keys) {
            String value = json.optString(key, "");
            if (!value.isEmpty()) {
                return value;
            }
        }
        throw new CommandException(ErrorCode.PARSE_ERROR, "缺少必填参数：" + Arrays.toString(keys));
    }

    public static long extractLong(JSONObject json, long defaultValue, String... keys) {
        for (String key : keys) {
            if (!json.has(key)) {
                continue;
            }
            Object value = json.opt(key);
            if (value == null) {
                continue;
            }
            try {
                if (value instanceof Number) {
                    return ((Number) value).longValue();
                }
                String s = String.valueOf(value).trim();
                if (!s.isEmpty()) {
                    return Long.parseLong(s);
                }
            } catch (Throwable ignore) {
            }
        }
        return defaultValue;
    }
}
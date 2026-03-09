package com.example.sekiro.shared;


import com.example.sekiro.util.SimpleLogUtils;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class CommandRouter  {

    private static final Map<String, RegisteredHandler<?, ?>> handlers = new HashMap<>();

    private static final Map<Class<?>, Function<JSONObject, ?>> defaultResolvers = new HashMap<>();

    // 在 CommandRouter 中增加：
    public static <T> void registerDefaultResolver(Class<T> clazz, Function<JSONObject, T> resolver) {
        defaultResolvers.put(clazz, resolver);
    }

    // 显式传递构造器
    protected static <T, V> void innerRegister(String event, CommandHandler<T, V> handler, Function<JSONObject, T> resolver) {
        handlers.put(event, new RegisteredHandler<>(handler, resolver));
    }

    public static Set<String> getRegisterMethods() {
        return handlers.keySet();
    }

    public static ApiResponse<?> dispatch(JSONObject json) {
        String event = json.optString("event", "");
        RegisteredHandler<?,?> registeredHandler = handlers.get(event);
        ApiResponse<?> result;
        // 记录请求开始时间
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
        // 记录请求结束时间
        long endTime = System.currentTimeMillis();
        // 计算请求时间
        result.setDuration( endTime - startTime);
        SimpleLogUtils.show("[CommandRouter] dispatch result=" + result);
        return result;
    }


    /**
     * 构建未识别事件的错误提示
     */
    private static String buildUnknownEventMessage(String event) {
        String suggestions = handlers.keySet().stream()
                .filter(k -> k.toLowerCase().contains(event.toLowerCase()))
                .collect(Collectors.joining(", "));

        return suggestions.isEmpty()
                ? "未知事件: " + event
                : "未知事件: " + event + "，你是想输入: " + suggestions + "？";
    }



    /**
     * 提取参数值，可设置多个 fallback key
     */
   protected static String extractParam(JSONObject json, String... keys) {
        for (String key : keys) {
            String value = json.optString(key, "");
            if (!value.isEmpty()) return value;
        }
        throw new CommandException(ErrorCode.PARSE_ERROR, "缺少必填参数：" + Arrays.toString(keys));
    }

}

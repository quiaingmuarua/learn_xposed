package com.example.command.core;

import com.example.command.model.ApiResponse;

import org.json.JSONObject;

import java.util.function.Function;

public class RegisteredHandler<T, V> {
    private final CommandHandler<T, V> handler;
    private final Function<JSONObject, T> paramResolver;

    public RegisteredHandler(CommandHandler<T, V> handler, Function<JSONObject, T> resolver) {
        this.handler = handler;
        this.paramResolver = resolver;
    }

    public ApiResponse<?> invoke(JSONObject json) throws Exception {
        T request = paramResolver.apply(json);
        CommandContext ctx = CommandContext.getInstance();
        V result = handler.handle(request, ctx);
        return ApiResponse.success(result);
    }
}
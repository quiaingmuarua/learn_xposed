package com.example.sekiro;

import android.content.Context;

import com.example.command.core.CommandContext;
import com.example.telegram.TelegramCommandRegistry;
import com.example.telegram.TelegramRequestFactory;
import com.example.telegram.base.TelegramResponseSerializer;
import com.example.telegram.base.TelegramRpcExecutor;
import com.example.telegram.base.TelegramRpcInvoker;

public class SekiroClientManager {

    public static void initClient(Context context, String targetApp) {
        if ("telegram".equals(targetApp)) {
            initTelegram(context);
        }
    }

    private static void initTelegram(Context context) {
        CommandContext.init(context);

        CommandContext ctx = CommandContext.getInstance();

        TelegramResponseSerializer serializer = new TelegramResponseSerializer();
        TelegramRpcInvoker rpcInvoker = new TelegramRpcInvoker(ctx, serializer);
        TelegramRpcExecutor rpcExecutor = new TelegramRpcExecutor(rpcInvoker);
        TelegramRequestFactory requestFactory = new TelegramRequestFactory(ctx);

        ctx.register(TelegramResponseSerializer.class, serializer);
        ctx.register(TelegramRpcInvoker.class, rpcInvoker);
        ctx.register(TelegramRpcExecutor.class, rpcExecutor);
        ctx.register(TelegramRequestFactory.class, requestFactory);

        TelegramCommandRegistry.registerAll();

        SekiroClientInit.init(
                "telegram",
                context,
                TelegramSekiroActions.createHandlers()
        );
    }
}
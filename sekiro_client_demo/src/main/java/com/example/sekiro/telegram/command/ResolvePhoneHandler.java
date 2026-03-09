package com.example.sekiro.telegram.command;



import com.example.sekiro.messages.shared.CommandContext;
import com.example.sekiro.messages.shared.CommandHandler;
import com.example.sekiro.telegram.base.TelegramRequestFactory;
import com.example.sekiro.telegram.base.TelegramRpcExecutor;

public class ResolvePhoneHandler implements CommandHandler<ResolvePhoneRequest, String> {

    @Override
    public String handle(ResolvePhoneRequest request, CommandContext context) throws Exception {
        TelegramRequestFactory requestFactory = context.require(TelegramRequestFactory.class);
        TelegramRpcExecutor rpcExecutor = context.require(TelegramRpcExecutor.class);

        return rpcExecutor.executeSync(
                "resolvePhone",
                "phone=" + request.getPhone(),
                request.getTimeoutMs(),
                () -> requestFactory.createResolvePhoneRequest(request.getPhone())
        );
    }
}
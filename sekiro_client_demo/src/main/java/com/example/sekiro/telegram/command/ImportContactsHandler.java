package com.example.sekiro.telegram.command;

import com.example.sekiro.messages.shared.CommandContext;
import com.example.sekiro.messages.shared.CommandHandler;
import com.example.sekiro.telegram.base.TelegramRequestFactory;
import com.example.sekiro.telegram.base.TelegramRpcExecutor;

public class ImportContactsHandler implements CommandHandler<ImportContactsRequest, String> {

    @Override
    public String handle(ImportContactsRequest request, CommandContext context) throws Exception {
        TelegramRequestFactory requestFactory = context.require(TelegramRequestFactory.class);
        TelegramRpcExecutor rpcExecutor = context.require(TelegramRpcExecutor.class);

        return rpcExecutor.executeSync(
                "importContacts",
                "size=" + request.getContacts().size(),
                request.getTimeoutMs(),
                () -> requestFactory.createImportContactsRequest(request.getContacts())
        );
    }
}
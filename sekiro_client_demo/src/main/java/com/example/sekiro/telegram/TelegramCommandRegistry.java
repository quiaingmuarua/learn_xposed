package com.example.sekiro.telegram;

import com.example.sekiro.messages.core.CommandRouter;
import com.example.sekiro.messages.shared.CommandException;
import com.example.sekiro.messages.shared.ErrorCode;
import com.example.sekiro.telegram.base.TelegramRequestFactory;
import com.example.sekiro.telegram.base.TelegramRequestParams;
import com.example.sekiro.telegram.base.TelegramRpcExecutor;
import com.example.sekiro.telegram.command.ImportContactsRequest;
import com.example.sekiro.telegram.command.ResolvePhoneRequest;

import com.example.sekiro.telegram.command.TelegramCommandRequest;
import com.example.sekiro.telegram.model.ImportContactItem;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public final class TelegramCommandRegistry {

    private static final long DEFAULT_RESOLVE_TIMEOUT_MS = 8000L;
    private static final long DEFAULT_IMPORT_TIMEOUT_MS = 10000L;
    private static final Gson GSON = new Gson();

    private TelegramCommandRegistry() {
    }

    @FunctionalInterface
    private interface JsonResolver<T> {
        T resolve(JSONObject json);
    }

    @FunctionalInterface
    private interface TelegramApiRequestBuilder<T> {
        Object build(T request, TelegramRequestFactory requestFactory) throws Exception;
    }

    public static void registerAll() {
        registerTelegram(
                "resolvePhone",
                TelegramCommandRegistry::resolvePhoneResolver,
                request -> "phone=" + request.getPhone(),
                (request, factory) -> factory.createResolvePhoneRequest(request.getPhone())
        );

        registerTelegram(
                "importContacts",
                TelegramCommandRegistry::importContactsResolver,
                request -> "size=" + request.getContacts().size(),
                (request, factory) -> factory.createImportContactsRequest(request.getContacts())
        );
    }

    private static <T extends TelegramCommandRequest> void registerTelegram(
            String actionName,
            JsonResolver<T> resolver,
            Function<T, String> logSuffixBuilder,
            TelegramApiRequestBuilder<T> apiRequestBuilder
    ) {
        CommandRouter.register(
                actionName,
                (request, context) -> {
                    TelegramRequestFactory requestFactory = context.require(TelegramRequestFactory.class);
                    TelegramRpcExecutor rpcExecutor = context.require(TelegramRpcExecutor.class);

                    TelegramRequestParams params = new TelegramRequestParams(
                            actionName,
                            logSuffixBuilder.apply(request),
                            request.getTimeoutMs()
                    );

                    return rpcExecutor.executeSync(
                            params,
                            () -> apiRequestBuilder.build(request, requestFactory)
                    );
                },
                resolver::resolve
        );
    }

    private static ResolvePhoneRequest resolvePhoneResolver(JSONObject json) {
        String phone = CommandRouter.extractParam(json, "phone");
        long timeoutMs = parseTimeout(json, DEFAULT_RESOLVE_TIMEOUT_MS);
        return new ResolvePhoneRequest(phone, timeoutMs);
    }

    private static ImportContactsRequest importContactsResolver(JSONObject json) {
        long timeoutMs = parseTimeout(json, DEFAULT_IMPORT_TIMEOUT_MS);
        List<ImportContactItem> items = parseContacts(json);

        if (items.isEmpty()) {
            throw new CommandException(ErrorCode.PARSE_ERROR, "missing contacts");
        }

        return new ImportContactsRequest(items, timeoutMs);
    }

    private static long parseTimeout(JSONObject json, long defaultValue) {
        long value = CommandRouter.extractLong(json, defaultValue, "timeout");
        return value > 0 ? value : defaultValue;
    }

    private static List<ImportContactItem> parseContacts(JSONObject json) {
        String contactsJson = json.optString("contacts", null);
        if (contactsJson != null && !contactsJson.trim().isEmpty()) {
            Type type = new TypeToken<List<ImportContactItem>>() {}.getType();
            List<ImportContactItem> items = GSON.fromJson(contactsJson, type);
            return items != null ? items : new ArrayList<>();
        }

        String phonesJson = json.optString("phones", null);
        if (phonesJson != null && !phonesJson.trim().isEmpty()) {
            Type type = new TypeToken<List<String>>() {}.getType();
            List<String> phones = GSON.fromJson(phonesJson, type);
            return fromPhones(phones);
        }

        String singlePhone = json.optString("phone", null);
        if (singlePhone != null && !singlePhone.trim().isEmpty()) {
            List<ImportContactItem> list = new ArrayList<>();
            list.add(new ImportContactItem(0L, singlePhone, "", ""));
            return list;
        }

        return new ArrayList<>();
    }

    private static List<ImportContactItem> fromPhones(List<String> phones) {
        List<ImportContactItem> list = new ArrayList<>();
        if (phones == null) {
            return list;
        }

        for (String phone : phones) {
            if (phone == null || phone.trim().isEmpty()) {
                continue;
            }
            list.add(new ImportContactItem(0L, phone, "", ""));
        }
        return list;
    }
}
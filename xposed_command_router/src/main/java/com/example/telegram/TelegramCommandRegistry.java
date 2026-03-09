package com.example.telegram;

import com.example.command.core.CommandRouter;
import com.example.command.core.CommandContext;
import com.example.command.model.CommandException;
import com.example.command.model.ErrorCode;
import com.example.telegram.base.TelegramRequestParams;
import com.example.telegram.base.TelegramRpcExecutor;
import com.example.telegram.model.ImportContactItem;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

public final class TelegramCommandRegistry {

    private static final long DEFAULT_RESOLVE_TIMEOUT_MS = 8000L;
    private static final long DEFAULT_IMPORT_TIMEOUT_MS = 10000L;

    private TelegramCommandRegistry() {
    }

    @FunctionalInterface
    private interface TelegramRequestBuilder {
        Object build(TelegramRequestFactory factory) throws Exception;
    }

    public static void registerAll() {
        CommandRouter.registerRaw("resolvePhone", TelegramCommandRegistry::handleResolvePhone);
        CommandRouter.registerRaw("importContacts", TelegramCommandRegistry::handleImportContacts);
    }
    private static String handleResolvePhone(JSONObject json, CommandContext context) throws Exception {
        String phone = CommandRouter.extractParam(json, "phone");
        long timeoutMs = parseTimeout(json, DEFAULT_RESOLVE_TIMEOUT_MS);

        return executeTelegram(
                context,
                new TelegramRequestParams("resolvePhone", "phone=" + phone, timeoutMs),
                factory -> factory.createResolvePhoneRequest(phone)
        );
    }

    private static String handleImportContacts(JSONObject json, CommandContext context) throws Exception {
        long timeoutMs = parseTimeout(json, DEFAULT_IMPORT_TIMEOUT_MS);
        List<ImportContactItem> contacts = parseContacts(json);

        if (contacts.isEmpty()) {
            throw new CommandException(ErrorCode.PARSE_ERROR, "missing contacts");
        }

        return executeTelegram(
                context,
                new TelegramRequestParams("importContacts", "size=" + contacts.size(), timeoutMs),
                factory -> factory.createImportContactsRequest(contacts)
        );
    }

    private static String executeTelegram(
            CommandContext context,
            TelegramRequestParams params,
            TelegramRequestBuilder requestBuilder
    ) throws Exception {
        TelegramRequestFactory requestFactory = context.require(TelegramRequestFactory.class);
        TelegramRpcExecutor rpcExecutor = context.require(TelegramRpcExecutor.class);

        return rpcExecutor.executeSync(
                params,
                () -> requestBuilder.build(requestFactory)
        );
    }

    private static long parseTimeout(JSONObject json, long defaultValue) {
        long value = CommandRouter.extractLong(json, defaultValue, "timeout");
        return value > 0 ? value : defaultValue;
    }

    private static List<ImportContactItem> parseContacts(JSONObject json) {
        List<ImportContactItem> result = new ArrayList<>();

        String contactsJson = json.optString("contacts", null);
        if (contactsJson != null && !contactsJson.trim().isEmpty()) {
            try {
                JSONArray array = new JSONArray(contactsJson);
                for (int i = 0; i < array.length(); i++) {
                    JSONObject itemObj = array.optJSONObject(i);
                    if (itemObj == null) {
                        continue;
                    }

                    long id = itemObj.optLong("id", 0L);
                    String phone = itemObj.optString("phone", "");
                    String name = itemObj.optString("name", "");
                    String avatar = itemObj.optString("avatar", "");

                    result.add(new ImportContactItem(id, phone, name, avatar));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return result;
        }

        String phonesJson = json.optString("phones", null);
        if (phonesJson != null && !phonesJson.trim().isEmpty()) {
            try {
                JSONArray array = new JSONArray(phonesJson);
                List<String> phones = new ArrayList<>();
                for (int i = 0; i < array.length(); i++) {
                    String phone = array.optString(i, "");
                    if (!phone.trim().isEmpty()) {
                        phones.add(phone);
                    }
                }
                return fromPhones(phones);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        String singlePhone = json.optString("phone", null);
        if (singlePhone != null && !singlePhone.trim().isEmpty()) {
            result.add(new ImportContactItem(0L, singlePhone, "", ""));
            return result;
        }

        return result;
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
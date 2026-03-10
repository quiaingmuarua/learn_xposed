package com.example.telegram;

import com.example.command.core.CommandContext;
import com.example.command.core.CommandRouter;
import com.example.command.model.CommandException;
import com.example.command.model.ErrorCode;
import com.example.telegram.base.TelegramRequestParams;
import com.example.telegram.base.TelegramRpcExecutor;
import com.example.telegram.model.TelegramContact;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public final class TelegramCommandRegistry {

    private static final long DEFAULT_RESOLVE_TIMEOUT_MS = 8000L;
    private static final long DEFAULT_IMPORT_TIMEOUT_MS = 10000L;
    private static final long DEFAULT_DELETE_TIMEOUT_MS = 10000L;
    private static final long DEFAULT_IMPORT_AND_DELETE_TIMEOUT_MS = 15000L;

    private TelegramCommandRegistry() {
    }

    @FunctionalInterface
    private interface TelegramRequestBuilder {
        Object build(TelegramRequestFactory factory) throws Exception;
    }

    public static void registerAll() {
        CommandRouter.registerRaw("resolvePhone", TelegramCommandRegistry::handleResolvePhone);
        CommandRouter.registerRaw("importContacts", TelegramCommandRegistry::handleImportContacts);
        CommandRouter.registerRaw("deleteContacts", TelegramCommandRegistry::handleDeleteContacts);
        CommandRouter.registerRaw("importContactsAndDelete", TelegramCommandRegistry::handleImportContactsAndDelete);
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
        List<TelegramContact> contacts = parseImportContacts(json);

        if (contacts.isEmpty()) {
            throw new CommandException(ErrorCode.PARSE_ERROR, "missing contacts");
        }

        return executeTelegram(
                context,
                new TelegramRequestParams("importContacts", "size=" + contacts.size(), timeoutMs),
                factory -> factory.createImportContactsRequest(contacts)
        );
    }

    private static String handleDeleteContacts(JSONObject json, CommandContext context) throws Exception {
        long timeoutMs = parseTimeout(json, DEFAULT_DELETE_TIMEOUT_MS);
        List<TelegramContact> contacts = parseDeleteContacts(json);

        if (contacts.isEmpty()) {
            throw new CommandException(ErrorCode.PARSE_ERROR, "missing delete contacts");
        }

        return executeTelegram(
                context,
                new TelegramRequestParams("deleteContacts", "size=" + contacts.size(), timeoutMs),
                factory -> factory.createDeleteContactsRequest(contacts)
        );
    }

    private static String handleImportContactsAndDelete(JSONObject json, CommandContext context) throws Exception {
        long workflowTimeoutMs = parseTimeout(json, DEFAULT_IMPORT_AND_DELETE_TIMEOUT_MS);

        List<TelegramContact> importContacts = parseImportContacts(json);
        if (importContacts.isEmpty()) {
            throw new CommandException(ErrorCode.PARSE_ERROR, "missing contacts");
        }

        long importTimeoutMs = extractStageTimeout(json, "importTimeout", Math.min(workflowTimeoutMs, 10000L));
        long deleteTimeoutMs = extractStageTimeout(json, "deleteTimeout", Math.min(workflowTimeoutMs, 10000L));

        String importResult = doImportContacts(context, importContacts, importTimeoutMs);

        List<TelegramContact> deleteContacts = extractDeleteContactsFromImportResult(importResult);
        if (deleteContacts.isEmpty()) {
            return buildDeleteSkippedResult(importResult);
        }

        String deleteResult = doDeleteContacts(context, deleteContacts, deleteTimeoutMs);
        return buildImportAndDeleteResult(importResult, deleteResult, deleteContacts.size());
    }

    private static String doImportContacts(
            CommandContext context,
            List<TelegramContact> contacts,
            long timeoutMs
    ) throws Exception {
        return executeTelegram(
                context,
                new TelegramRequestParams(
                        "importContactsAndDelete#import",
                        "size=" + contacts.size(),
                        timeoutMs
                ),
                factory -> factory.createImportContactsRequest(contacts)
        );
    }

    private static String doDeleteContacts(
            CommandContext context,
            List<TelegramContact> contacts,
            long timeoutMs
    ) throws Exception {
        return executeTelegram(
                context,
                new TelegramRequestParams(
                        "importContactsAndDelete#delete",
                        "size=" + contacts.size(),
                        timeoutMs
                ),
                factory -> factory.createDeleteContactsRequest(contacts)
        );
    }

    private static String buildDeleteSkippedResult(String importResult) throws JSONException {
        JSONObject result = new JSONObject();
        result.put("ok", true);
        result.put("importResult", safeToJsonObject(importResult));
        result.put("deleteSkipped", true);
        result.put("deleteReason", "no users with id/access_hash extracted from import result");
        return result.toString();
    }

    private static String buildImportAndDeleteResult(
            String importResult,
            String deleteResult,
            int deleteCount
    ) throws JSONException {
        JSONObject result = new JSONObject();
        result.put("ok", true);
        result.put("importResult", safeToJsonObject(importResult));
        result.put("deleteResult", safeToJsonObject(deleteResult));
        result.put("deleteCount", deleteCount);
        return result.toString();
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

    private static long extractStageTimeout(JSONObject json, String key, long defaultValue) {
        long value = json.optLong(key, defaultValue);
        return value > 0 ? value : defaultValue;
    }

    private static List<TelegramContact> parseImportContacts(JSONObject json) {
        List<TelegramContact> result = new ArrayList<>();

        String contactsJson = json.optString("contacts", null);
        if (contactsJson != null && !contactsJson.trim().isEmpty()) {
            try {
                JSONArray array = new JSONArray(contactsJson);
                for (int i = 0; i < array.length(); i++) {
                    JSONObject itemObj = array.optJSONObject(i);
                    if (itemObj == null) {
                        continue;
                    }

                    TelegramContact item = new TelegramContact();
                    item.setClientId(itemObj.optLong("clientId", itemObj.optLong("id", 0L)));
                    item.setPhone(itemObj.optString("phone", ""));
                    item.setFirstName(itemObj.optString("firstName", ""));
                    item.setLastName(itemObj.optString("lastName", ""));

                    if (item.canImport()) {
                        result.add(item);
                    }
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
                for (int i = 0; i < array.length(); i++) {
                    String phone = array.optString(i, "");
                    if (phone == null || phone.trim().isEmpty()) {
                        continue;
                    }

                    TelegramContact item = new TelegramContact();
                    item.setPhone(phone);
                    item.setFirstName("");
                    item.setLastName("");
                    result.add(item);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return result;
        }

        String singlePhone = json.optString("phone", null);
        if (singlePhone != null && !singlePhone.trim().isEmpty()) {
            TelegramContact item = new TelegramContact();
            item.setPhone(singlePhone);
            item.setFirstName("");
            item.setLastName("");
            result.add(item);
        }

        return result;
    }

    private static List<TelegramContact> parseDeleteContacts(JSONObject json) {
        List<TelegramContact> result = new ArrayList<>();

        String contactsJson = json.optString("contacts", null);
        if (contactsJson != null && !contactsJson.trim().isEmpty()) {
            try {
                JSONArray array = new JSONArray(contactsJson);
                for (int i = 0; i < array.length(); i++) {
                    JSONObject itemObj = array.optJSONObject(i);
                    if (itemObj == null) {
                        continue;
                    }

                    TelegramContact item = new TelegramContact();
                    item.setUserId(itemObj.optLong("userId", itemObj.optLong("id", 0L)));
                    item.setAccessHash(itemObj.optLong("accessHash", itemObj.optLong("access_hash", 0L)));

                    if (item.canDelete()) {
                        result.add(item);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return result;
        }

        String usersJson = json.optString("users", null);
        if (usersJson != null && !usersJson.trim().isEmpty()) {
            try {
                JSONArray array = new JSONArray(usersJson);
                for (int i = 0; i < array.length(); i++) {
                    JSONObject itemObj = array.optJSONObject(i);
                    if (itemObj == null) {
                        continue;
                    }

                    TelegramContact item = new TelegramContact();
                    item.setUserId(itemObj.optLong("id", 0L));
                    item.setAccessHash(itemObj.optLong("access_hash", 0L));

                    if (item.canDelete()) {
                        result.add(item);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return result;
        }

        TelegramContact single = new TelegramContact();
        single.setUserId(json.optLong("userId", 0L));
        single.setAccessHash(json.optLong("accessHash", 0L));
        if (single.canDelete()) {
            result.add(single);
        }

        return result;
    }

    private static List<TelegramContact> extractDeleteContactsFromImportResult(String importResult) {
        List<TelegramContact> result = new ArrayList<>();
        if (importResult == null || importResult.trim().isEmpty()) {
            return result;
        }

        try {
            JSONObject root = new JSONObject(importResult);
            JSONArray users = root.optJSONArray("users");
            if (users == null) {
                return result;
            }

            for (int i = 0; i < users.length(); i++) {
                JSONObject userObj = users.optJSONObject(i);
                if (userObj == null) {
                    continue;
                }

                TelegramContact contact = new TelegramContact();
                contact.setUserId(userObj.optLong("id", 0L));
                contact.setAccessHash(userObj.optLong("access_hash", 0L));

                if (contact.canDelete()) {
                    result.add(contact);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    private static Object safeToJsonObject(String jsonText) {
        if (jsonText == null) {
            return JSONObject.NULL;
        }

        try {
            return new JSONObject(jsonText);
        } catch (Exception ignore) {
        }

        try {
            return new JSONArray(jsonText);
        } catch (Exception ignore) {
        }

        return jsonText;
    }
}
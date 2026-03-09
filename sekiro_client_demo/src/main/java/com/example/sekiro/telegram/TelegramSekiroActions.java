package com.example.sekiro.telegram;

import static com.example.sekiro.telegram.base.SekiroLambda.action;

import android.content.Context;

import com.example.sekiro.telegram.base.TelegramEnv;
import com.example.sekiro.telegram.base.TelegramRequestFactory;
import com.example.sekiro.telegram.base.TelegramResponseSerializer;
import com.example.sekiro.telegram.base.TelegramRpcInvoker;
import com.example.sekiro.telegram.model.ImportContactItem;
import com.example.sekiro.util.SimpleLogUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import cn.iinti.sekiro3.business.api.interfaze.ActionHandler;
import cn.iinti.sekiro3.business.api.interfaze.SekiroRequest;
import cn.iinti.sekiro3.business.api.interfaze.SekiroResponse;

public final class TelegramSekiroActions {

    private static final long DEFAULT_RESOLVE_TIMEOUT_MS = 8000L;
    private static final long DEFAULT_IMPORT_TIMEOUT_MS = 10000L;
    private static final Gson GSON = new Gson();

    private TelegramSekiroActions() {
    }

    public static List<ActionHandler> createHandlers(Context context, ClassLoader classLoader) {
        TelegramEnv env = new TelegramEnv(classLoader);
        TelegramResponseSerializer serializer = new TelegramResponseSerializer();
        TelegramRequestFactory requestFactory = new TelegramRequestFactory(env);
        TelegramRpcInvoker rpcInvoker = new TelegramRpcInvoker(env, serializer);

        return Arrays.asList(
                action("resolvePhone", (req, resp) ->
                        handleResolvePhone(context, requestFactory, rpcInvoker, req, resp)
                ),
                action("importContacts", (req, resp) ->
                        handleImportContacts(requestFactory, rpcInvoker, req, resp)
                )
        );
    }

    private static void handleResolvePhone(
            Context context,
            TelegramRequestFactory requestFactory,
            TelegramRpcInvoker rpcInvoker,
            SekiroRequest req,
            SekiroResponse resp
    ) throws Exception {
        String phone = req.getString("phone");
        long timeoutMs = parseTimeout(req.getString("timeout"), DEFAULT_RESOLVE_TIMEOUT_MS);

        if (phone == null || phone.trim().isEmpty()) {
            resp.failed("missing parameter: phone");
            return;
        }

        SimpleLogUtils.show("[resolvePhone] start, phone=" + phone);

        Object request = requestFactory.createResolvePhoneRequest(phone);
        String resultJson = rpcInvoker.sendRequestSync(request, timeoutMs);

        SimpleLogUtils.show("[resolvePhone] success, phone=" + phone);
        resp.success(resultJson);
    }

    private static void handleImportContacts(
            TelegramRequestFactory requestFactory,
            TelegramRpcInvoker rpcInvoker,
            SekiroRequest req,
            SekiroResponse resp
    ) throws Exception {
        long timeoutMs = parseTimeout(req.getString("timeout"), DEFAULT_IMPORT_TIMEOUT_MS);

        List<ImportContactItem> items = parseContacts(req);
        if (items.isEmpty()) {
            resp.failed("missing contacts");
            return;
        }

        SimpleLogUtils.show("[importContacts] start, size=" + items.size());

        Object request = requestFactory.createImportContactsRequest(items);
        String resultJson = rpcInvoker.sendRequestSync(request, timeoutMs);

        SimpleLogUtils.show("[importContacts] success, size=" + items.size());
        resp.success(resultJson);
    }

    private static List<ImportContactItem> parseContacts(SekiroRequest request) {
        String contactsJson = request.getString("contacts");
        if (contactsJson != null && !contactsJson.trim().isEmpty()) {
            Type type = new TypeToken<List<ImportContactItem>>() {}.getType();
            List<ImportContactItem> items = GSON.fromJson(contactsJson, type);
            return items != null ? items : new ArrayList<>();
        }

        String phonesJson = request.getString("phones");
        if (phonesJson != null && !phonesJson.trim().isEmpty()) {
            Type type = new TypeToken<List<String>>() {}.getType();
            List<String> phones = GSON.fromJson(phonesJson, type);
            return fromPhones(phones);
        }

        String singlePhone = request.getString("phone");
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

    private static long parseTimeout(String timeoutStr, long defaultValue) {
        if (timeoutStr == null || timeoutStr.trim().isEmpty()) {
            return defaultValue;
        }
        try {
            long value = Long.parseLong(timeoutStr);
            return value > 0 ? value : defaultValue;
        } catch (Throwable ignore) {
            return defaultValue;
        }
    }
}
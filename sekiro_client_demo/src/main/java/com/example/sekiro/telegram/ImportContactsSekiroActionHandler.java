package com.example.sekiro.telegram;

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
import java.util.List;

import cn.iinti.sekiro3.business.api.interfaze.ActionHandler;
import cn.iinti.sekiro3.business.api.interfaze.SekiroRequest;
import cn.iinti.sekiro3.business.api.interfaze.SekiroResponse;

public class ImportContactsSekiroActionHandler implements ActionHandler {

    private static final long DEFAULT_TIMEOUT_MS = 10000L;
    private static final Gson GSON = new Gson();

    private final TelegramRequestFactory requestFactory;
    private final TelegramRpcInvoker rpcInvoker;

    public ImportContactsSekiroActionHandler(Context context, ClassLoader classLoader) {
        TelegramEnv env = new TelegramEnv(classLoader);
        TelegramResponseSerializer serializer = new TelegramResponseSerializer();
        this.requestFactory = new TelegramRequestFactory(env);
        this.rpcInvoker = new TelegramRpcInvoker(env, serializer);
    }

    @Override
    public String action() {
        return "importContacts";
    }

    @Override
    public void handleRequest(SekiroRequest sekiroRequest, SekiroResponse sekiroResponse) {
        try {
            long timeoutMs = parseTimeout(sekiroRequest.getString("timeout"), DEFAULT_TIMEOUT_MS);

            List<ImportContactItem> items = parseContacts(sekiroRequest);
            if (items.isEmpty()) {
                sekiroResponse.failed("missing contacts");
                return;
            }

            SimpleLogUtils.show("[ImportContacts] start, size=" + items.size());

            Object req = requestFactory.createImportContactsRequest(items);
            String resultJson = rpcInvoker.sendRequestSync(req, timeoutMs);

            SimpleLogUtils.show("[ImportContacts] success, size=" + items.size());
            sekiroResponse.success(resultJson);

        } catch (Throwable t) {
            SimpleLogUtils.show("[ImportContacts] failed: " + t);
            sekiroResponse.failed(t.getMessage() == null ? String.valueOf(t) : t.getMessage());
        }
    }

    private List<ImportContactItem> parseContacts(SekiroRequest request) {
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

    private List<ImportContactItem> fromPhones(List<String> phones) {
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

    private long parseTimeout(String timeoutStr, long defaultValue) {
        if (timeoutStr == null || timeoutStr.trim().isEmpty()) {
            return defaultValue;
        }
        try {
            long v = Long.parseLong(timeoutStr);
            return v > 0 ? v : defaultValue;
        } catch (Throwable ignore) {
            return defaultValue;
        }
    }
}
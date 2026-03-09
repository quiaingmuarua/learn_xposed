package com.example.sekiro.telegram.handler;



import android.content.Context;

import com.example.sekiro.telegram.TelegramEnv;
import com.example.sekiro.telegram.TelegramRequestFactory;
import com.example.sekiro.telegram.base.TelegramResponseSerializer;
import com.example.sekiro.telegram.base.TelegramRpcInvoker;
import com.example.sekiro.util.SimpleLogUtils;

import cn.iinti.sekiro3.business.api.interfaze.ActionHandler;
import cn.iinti.sekiro3.business.api.interfaze.SekiroRequest;
import cn.iinti.sekiro3.business.api.interfaze.SekiroResponse;

public class ResolvePhoneActionHandler implements ActionHandler {

    private static final long DEFAULT_TIMEOUT_MS = 8000L;

    private final Context context;
    private final TelegramEnv env;
    private final TelegramRequestFactory requestFactory;
    private final TelegramRpcInvoker rpcInvoker;

    public ResolvePhoneActionHandler(Context context, ClassLoader classLoader) {
        this.context = context;

        TelegramEnv env = new TelegramEnv(classLoader);
        TelegramResponseSerializer serializer = new TelegramResponseSerializer();

        this.env = env;
        this.requestFactory = new TelegramRequestFactory(env);
        this.rpcInvoker = new TelegramRpcInvoker(env, serializer);
    }

    @Override
    public String action() {
        return "resolvePhone";
    }

    @Override
    public void handleRequest(SekiroRequest sekiroRequest, SekiroResponse sekiroResponse) {
        String phone = sekiroRequest.getString("phone");
        long timeoutMs = parseTimeout(sekiroRequest.getString("timeout"), DEFAULT_TIMEOUT_MS);

        if (phone == null || phone.trim().isEmpty()) {
            sekiroResponse.failed("missing parameter: phone");
            return;
        }

        try {
            SimpleLogUtils.show("[ResolvePhoneActionHandler] resolvePhone start, phone=" + phone);

            Object request = requestFactory.createResolvePhoneRequest(phone);
            String resultJson = rpcInvoker.sendRequestSync(request, timeoutMs);

            SimpleLogUtils.show("[ResolvePhoneActionHandler] resolvePhone success, phone=" + phone);
            sekiroResponse.success(resultJson);

        } catch (Throwable t) {
            SimpleLogUtils.show("[ResolvePhoneActionHandler] resolvePhone failed: " + t);
            sekiroResponse.failed(t.getMessage() == null ? String.valueOf(t) : t.getMessage());
        }
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
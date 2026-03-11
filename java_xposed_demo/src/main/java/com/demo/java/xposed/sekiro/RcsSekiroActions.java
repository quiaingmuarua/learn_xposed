package com.demo.java.xposed.sekiro;

import static com.demo.java.xposed.sekiro.SekiroLambda.action;


import com.demo.java.xposed.utils.LogUtils;
import com.example.command.model.ApiResponse;
import com.example.command.core.CommandRouter;
import com.example.command.util.SimpleLogUtils;
import com.example.messages.XposedCommandRouter;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import cn.iinti.sekiro3.business.api.interfaze.ActionHandler;
import cn.iinti.sekiro3.business.api.interfaze.SekiroRequest;
import cn.iinti.sekiro3.business.api.interfaze.SekiroResponse;

public final class RcsSekiroActions {

    private RcsSekiroActions() {
    }

    public static List<ActionHandler> createHandlers() {
        List<ActionHandler> handlers = new ArrayList<>();

        for (String event : XposedCommandRouter.getRegisterMethods()) {
            handlers.add(action(event, (req, resp) -> dispatch(event, req, resp)));
        }
        LogUtils.show("RcsSekiroActions  handlers_size"+ handlers.size());
        return handlers;
    }

    private static void dispatch(String event, SekiroRequest req, SekiroResponse resp) {
        try {
            JSONObject json = toJson(event, req);
            ApiResponse<?> result = XposedCommandRouter.dispatch(json);

            if (result.isSuccess()) {
                resp.success(result);
            } else {
                resp.failed(result.message != null ? result.message : ("error code=" + result.code));
            }
        } catch (Throwable t) {
            SimpleLogUtils.show("[TelegramSekiroActions] dispatch error: " + t.getMessage());
            resp.failed("dispatch error: " + t.getMessage());
        }
    }

    private static JSONObject toJson(String event, SekiroRequest req) throws Exception {
        JSONObject json = new JSONObject();
        json.put("event", event);

        copyIfPresent(req, json, "phone");
        copyIfPresent(req, json, "phones");
        copyIfPresent(req, json, "contacts");
        copyIfPresent(req, json, "timeout");

        return json;
    }

    private static void copyIfPresent(SekiroRequest req, JSONObject json, String key) throws Exception {
        String value = req.getString(key);
        if (value != null && !value.trim().isEmpty()) {
            json.put(key, value);
        }
    }
}
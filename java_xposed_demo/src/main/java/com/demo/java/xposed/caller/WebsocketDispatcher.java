package com.demo.java.xposed.caller;

import com.demo.java.xposed.rcs.shared.ApiResponse;
import com.demo.java.xposed.rcs.apiCaller.core.XposedCommandRouter;
import com.demo.java.xposed.utils.LogUtils;
import com.google.gson.Gson;

import org.json.JSONObject;

public class WebsocketDispatcher {

    public static String handle(String msg) {
        if (msg == null || msg.trim().isEmpty()) {
            LogUtils.show("WS 收到空消息");
            return null;
        }
        msg = msg.trim();
        if ("ping".equalsIgnoreCase(msg) || "pong".equalsIgnoreCase(msg)) {
            LogUtils.show("WS 收到心跳: " + msg);
            return null;
        }
        try {
            JSONObject json = new JSONObject(msg);
            ApiResponse<?> response = XposedCommandRouter.dispatch(json);
            LogUtils.show("WS 响应: " + new Gson().toJson(response));
            return new Gson().toJson(response);

        } catch (Exception e) {
            LogUtils.printStackErrInfo("WS JSON 解析失败: ", e);
        }

        return null;
    }


}

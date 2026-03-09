package com.demo.java.xposed.caller;


import com.demo.java.xposed.rcs.apiCaller.core.XposedCommandRouter;
import com.example.sekiro.messages.shared.ApiResponse;
import com.example.sekiro.messages.shared.ErrorCode;
import com.demo.java.xposed.device.PluginInit;
import com.demo.java.xposed.utils.LogUtils;
import com.google.gson.Gson;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import fi.iki.elonen.NanoHTTPD;

public class XposedHttpServer extends NanoHTTPD {

    private final Gson gson = new Gson();
    private final Map<String, Function<IHTTPSession, ApiResponse<?>>> routeMap = new HashMap<>();

    public XposedHttpServer(int port) {
        super(port);
        initCustomRoutes();
        initRoutes();
        LogUtils.show("XposedHttpServer initRoutes ");
    }

    @Override
    public Response serve(IHTTPSession session) {
        String uri = session.getUri();
        ApiResponse<?> responseObj = null;
        Function<IHTTPSession, ApiResponse<?>> handler = routeMap.get(uri);
        if (handler != null) {
            responseObj = handler.apply(session);

        }else {
            responseObj = new ApiResponse<>(ErrorCode.UNKNOWN_EVENT.code, "routeMap keySet=" + routeMap.keySet());
            LogUtils.show("XposedHttpServer routeMap keySet=" + routeMap.keySet());
        }

        String json = gson.toJson(responseObj);
        return newFixedLengthResponse(Response.Status.OK, "application/json", json);

    }



    protected void initCustomRoutes() {
        routeMap.put("/version", session -> new ApiResponse<>("ok", PluginInit.version));
        routeMap.put("/info", session -> new ApiResponse<>("ok", PluginInit.gsonInfo()));
        routeMap.put("/all", session -> new ApiResponse<>("ok", routeMap.keySet()));
    }


    private void initRoutes() {
        // 动态注册所有已注册的 event -> HTTP 路由
        for (String event : XposedCommandRouter.getRegisterMethods()) {
            String path = "/" + event;
            routeMap.put(path, session -> autoHandle(path, session));
        }
    }
    // curl  "http://127.0.0.1:18899/lookup?phones=13185811903,15125024635,17202858096,15597207412,19407042393,16197230358,13174092946,17025881500,17864889108,18323028144,14692794486,19543280832,19564542748,19252095506,17708469408,17042313733,18167167545,17864395214,18643146236,18175648948,13057906064,17753545006,19897081841,12144554519,14247728051,18173337769,19373676462,14439911129,19166066748,19512560844,18285076419,17347313749,15203130283,17574397049,16509338692,13053230772,17739989370,18182991415,17739645054,12159924512,16617470344,13056101813,18313198839,18122399584,18506022132,18064331497,13057994373,17042992086,17606709549,12145435173"

    public static ApiResponse<?> autoHandle(String path, NanoHTTPD.IHTTPSession session) {
        try {
            JSONObject json = new JSONObject();
            json.put("event", path.replaceFirst("^/", ""));

            Map<String, List<String>> params = session.getParameters();  // ✅ 推荐方式
            for (Map.Entry<String, List<String>> entry : params.entrySet()) {
                if (!entry.getValue().isEmpty()) {
                    json.put(entry.getKey(), entry.getValue().get(0));
                }
            }

            return XposedCommandRouter.dispatch(json);

        } catch (Exception e) {
            return new ApiResponse<>(ErrorCode.PARSE_ERROR.code, "参数解析失败: " + e.getMessage());
        }
    }

}

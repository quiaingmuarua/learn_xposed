package com.example.sekiro.client;

import com.example.sekiro.util.SimpleLogUtils;

import cn.iinti.sekiro3.business.api.interfaze.ActionHandler;
import cn.iinti.sekiro3.business.api.interfaze.SekiroRequest;
import cn.iinti.sekiro3.business.api.interfaze.SekiroResponse;

@FunctionalInterface
public interface SekiroLambda {
    void handle(SekiroRequest req, SekiroResponse resp) throws Exception;


    public static ActionHandler action(String action, SekiroLambda lambda) {
        return new ActionHandler() {
            @Override
            public String action() {
                return action;
            }

            @Override
            public void handleRequest(SekiroRequest req, SekiroResponse resp) {
                try {
                    lambda.handle(req, resp);
                } catch (Throwable t) {
                    SimpleLogUtils.show("[" + action + "] failed: " + t);
                    resp.failed(t.getMessage() == null ? String.valueOf(t) : t.getMessage());
                }
            }
        };
    }
}
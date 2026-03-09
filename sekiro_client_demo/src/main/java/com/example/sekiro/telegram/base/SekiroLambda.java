package com.example.sekiro.telegram.base;

import cn.iinti.sekiro3.business.api.interfaze.SekiroRequest;
import cn.iinti.sekiro3.business.api.interfaze.SekiroResponse;

@FunctionalInterface
public interface SekiroLambda {
    void handle(SekiroRequest req, SekiroResponse resp);
}
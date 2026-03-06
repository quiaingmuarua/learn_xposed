package com.demo.java.xposed.app.instagram;

import android.content.Context;
import com.demo.java.xposed.utils.LogUtils;

import org.json.JSONObject;

import cn.iinti.sekiro3.business.api.interfaze.ActionHandler;
import cn.iinti.sekiro3.business.api.interfaze.SekiroRequest;
import cn.iinti.sekiro3.business.api.interfaze.SekiroResponse;

public class InsSekiroActionHandler implements ActionHandler {

    private Context context;

    public InsSekiroActionHandler(Context context) {
        this.context = context;
    }

    @Override
    public String action() {
        return "action";
    }

    @Override
    public void handleRequest(SekiroRequest sekiroRequest, SekiroResponse sekiroResponse) {
        JSONObject result = InsTokenPlus.getInsData(context);
        LogUtils.show("InsSekiroActionHandler handleRequest"+result);
        sekiroResponse.success(result.toString());
    }



}

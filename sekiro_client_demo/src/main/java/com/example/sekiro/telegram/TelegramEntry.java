package com.example.sekiro.telegram;

import android.content.Context;

import com.example.sekiro.demo.SekiroUtil;

import java.util.Arrays;
import java.util.List;

import cn.iinti.sekiro3.business.api.interfaze.ActionHandler;

public class TelegramEntry {

    public static void init(Context mContext){
        SekiroUtil.init("telegram", mContext, TelegramEntry.getAllActions(mContext));
    }


    private static List<ActionHandler> getAllActions(Context mContext){
        ClassLoader mLoader=mContext.getClassLoader();
        return Arrays.asList(new TelegramSekiroActionHandler(mContext, mLoader), new ImportContactsSekiroActionHandler(mContext, mLoader));
    }
}

package com.example.sekiro;

import android.content.Context;

import com.example.sekiro.telegram.ImportContactsSekiroActionHandler;
import com.example.sekiro.telegram.ResolvePhoneActionHandler;
import com.example.sekiro.util.SekiroUtil;

import java.util.Arrays;

public class SekiroClientManager {


    public static void initClient(Context mContext,String targetApp) {
        ClassLoader mLoader = mContext.getClassLoader();
        if ("telegram".equals(targetApp)) {
            SekiroUtil.init("telegram", mContext, Arrays.asList(new ResolvePhoneActionHandler(mContext, mLoader), new ImportContactsSekiroActionHandler(mContext, mLoader)));

        }

    }

}

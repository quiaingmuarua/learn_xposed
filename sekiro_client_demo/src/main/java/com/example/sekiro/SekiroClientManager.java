package com.example.sekiro;

import android.content.Context;

import com.example.sekiro.telegram.TelegramSekiroActions;
import com.example.sekiro.telegram.client.SekiroUtil;

public class SekiroClientManager {

    public static void initClient(Context context, String targetApp) {
        ClassLoader classLoader = context.getClassLoader();

        if ("telegram".equals(targetApp)) {
            SekiroUtil.init(
                    "telegram",
                    context,
                    TelegramSekiroActions.createHandlers(context, classLoader)
            );
        }
    }
}
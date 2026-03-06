package com.demo.java.xposed;

import com.demo.java.xposed.app.demo.geekrun.DetectAppEnvSample;
import com.demo.java.xposed.app.demo.geekrun.ProtectAppEnvSample;
import com.demo.java.xposed.app.instagram.InstagramHook;
import com.demo.java.xposed.app.line.NaverLineHook;
import com.demo.java.xposed.app.linkedin.LinkedinHook;
import com.demo.java.xposed.app.telegram.TelegramHook;
import com.demo.java.xposed.app.twitter.TwitterHook;
import com.demo.java.xposed.app.zalo.ZaloHook;
import com.demo.java.xposed.rcs.RcsHook;
import com.demo.java.xposed.utils.LogUtils;
import com.demo.java.xposed.whatsapp.WhatsappHook;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class HookMain implements IXposedHookLoadPackage {

    private static final Map<String, Consumer<XC_LoadPackage.LoadPackageParam>> HOOK_MAP =
            new HashMap<>();

    static {
        // LinkedIn
        HOOK_MAP.put(
                "com.linkedin.android",
                LinkedinHook::handleLoadPackage
        );

        // 示例 App
        HOOK_MAP.put(
                "com.example.protect_app_env_sample",
                ProtectAppEnvSample::handleLoadPackage
        );
        HOOK_MAP.put(
                "com.example.detect.app.sample",
                DetectAppEnvSample::handleLoadPackage
        );

        // RCS（多个包 → 同一个 handler）
        Consumer<XC_LoadPackage.LoadPackageParam> rcsHandler =
                RcsHook::handleLoadPackage;
        HOOK_MAP.put("com.google.android.apps.messaging", rcsHandler);
        HOOK_MAP.put("com.google.android.gms", rcsHandler);

        // IM Apps
        HOOK_MAP.put(
                "org.telegram.messenger",
                TelegramHook::handleLoadPackage
        );
        HOOK_MAP.put(
                "com.twitter.android",
                TwitterHook::handleLoadPackage
        );
        HOOK_MAP.put(
                "com.whatsapp",
                WhatsappHook::handleLoadPackage
        );
        HOOK_MAP.put(
                "jp.naver.line.android",
                NaverLineHook::handleLoadPackage
        );
        HOOK_MAP.put(
                "com.zing.zalo",
                ZaloHook::handleLoadPackage
        );
        HOOK_MAP.put(
                "com.instagram.android",
                InstagramHook::handleLoadPackage
        );
    }
    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        LogUtils.show("handleLoadPackage init");
        LogUtils.show("found ====>" + lpparam.packageName);

        Consumer<XC_LoadPackage.LoadPackageParam> handler =
                HOOK_MAP.get(lpparam.packageName);

        if (handler != null) {
            try {
                handler.accept(lpparam);
            } catch (Throwable t) {
                LogUtils.show("hook error: " + lpparam.packageName + " -> " + t);
            }
        } else {
            LogUtils.show("not_found suitable handle!!!!!!! " + lpparam.packageName);
        }
    }
}


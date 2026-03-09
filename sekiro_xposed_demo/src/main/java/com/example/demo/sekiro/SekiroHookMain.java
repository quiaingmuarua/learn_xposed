package com.example.demo.sekiro;
import com.example.command.util.SimpleLogUtils;
import com.example.demo.sekiro.hook.TelegramSekiroHook;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class SekiroHookMain implements IXposedHookLoadPackage {

    private static final Map<String, Consumer<XC_LoadPackage.LoadPackageParam>> HOOK_MAP =
            new HashMap<>();
    static {
        // IM Apps
        HOOK_MAP.put(
                "org.telegram.messenger",
                TelegramSekiroHook::handleLoadPackage
        );
    }



    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        SimpleLogUtils.show("handleLoadPackage init");
        SimpleLogUtils.show("found ====>" + lpparam.packageName);

        Consumer<XC_LoadPackage.LoadPackageParam> handler =
                HOOK_MAP.get(lpparam.packageName);

        if (handler != null) {
            try {
                handler.accept(lpparam);
            } catch (Throwable t) {
                SimpleLogUtils.show("hook error: " + lpparam.packageName + " -> " + t);
            }
        } else {
            SimpleLogUtils.show("not_found suitable handle!!!!!!! " + lpparam.packageName);
        }
    }
}

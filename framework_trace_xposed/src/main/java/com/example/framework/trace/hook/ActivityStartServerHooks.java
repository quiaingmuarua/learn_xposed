package com.example.framework.trace.hook;

import android.content.Intent;
import android.os.Binder;

import com.example.framework.trace.base.HookContext;
import com.example.framework.trace.base.HookUnit;
import com.example.framework.trace.tools.LogUtil;

import java.lang.reflect.Method;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

public final class ActivityStartServerHooks implements HookUnit {

    public static final String EXTRA_TRACE = "__ft_trace";

    @Override public String name() { return "ActivityStartServerHooks"; }
    @Override public int priority() { return 200; }
    @Override public boolean isEnabled(HookContext ctx) { return true; }

    @Override
    public boolean appliesTo(HookContext ctx) {
        return ctx.isSystemServer;
    }

    @Override
    public void hook(HookContext ctx) {
        LogUtil.event("hook/entry", null, "sys", "ActivityStartServerHooks install...");
        hookATMS(ctx);
        LogUtil.event("hook/installed", null, "sys", "ActivityStartServerHooks installed");
    }

    private void hookATMS(HookContext ctx) {
        try {
            Class<?> atms = XposedHelpers.findClass(
                    "com.android.server.wm.ActivityTaskManagerService",
                    ctx.classLoader
            );

            hookAllOverloadsWithIntent(atms, "startActivityAsUser");
            hookAllOverloadsWithIntent(atms, "startActivityAsCaller");
            hookAllOverloadsWithIntent(atms, "startActivity");

        } catch (Throwable t) {
            LogUtil.error("hook/atms_fail", "ATMS", t);
        }
    }

    private void hookAllOverloadsWithIntent(Class<?> clz, String methodName) {
        try {
            final String mn = methodName;

            for (Method m : clz.getDeclaredMethods()) {
                if (!mn.equals(m.getName())) continue;

                final int intentIdx = findIntentIndex(m.getParameterTypes());
                if (intentIdx < 0) continue;

                XposedBridge.hookMethod(m, new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) {
                        Intent intent = null;
                        try { intent = (Intent) param.args[intentIdx]; } catch (Throwable ignored) {}

                        String comp = (intent != null && intent.getComponent() != null)
                                ? intent.getComponent().flattenToShortString()
                                : "null";

                        int callingUid = Binder.getCallingUid();
                        int callingPid = Binder.getCallingPid();

                        // ✅ TRACE: server 只读，不生成不注入
                        String trace = null;
                        if (intent != null) {
                            try { trace = intent.getStringExtra(EXTRA_TRACE); } catch (Throwable ignored) {}
                        }

                        LogUtil.Attrs attrs = LogUtil.attrs()
                                .put("method", mn)
                                .put("uid", callingUid)
                                .put("pid", callingPid)
                                .put("comp", comp)
                                .put("flags", intent == null ? null : intent.getFlags());

                        // ✅ stack：强烈建议通过 LogUtil.ENABLE_STACK 控制
                        if (LogUtil.ENABLE_STACK) {
                            // 用 Throwable 但别 getStackTraceString（太长）；你 LogUtil 已经会 topN
                            Throwable st = new Throwable("ATMS " + mn);
                            attrs.put("stackTop", stackTop(st, LogUtil.STACK_TOP_N));
                        }

                        // point 用语义稳定
                        LogUtil.event("atms/" + mn, trace, "sys/start", attrs, null);
                    }
                });
            }
        } catch (Throwable t) {
            LogUtil.error("hook/atms_method_fail", methodName, t);
        }
    }

    private static int findIntentIndex(Class<?>[] p) {
        if (p == null) return -1;
        for (int i = 0; i < p.length; i++) {
            if (Intent.class.equals(p[i])) return i;
        }
        return -1;
    }

    // 本地 helper：只取 topN 行（避免爆炸）
    private static String stackTop(Throwable t, int n) {
        try {
            StackTraceElement[] arr = t.getStackTrace();
            if (arr == null) return "";
            int lim = Math.min(n, arr.length);
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < lim; i++) {
                if (i > 0) sb.append("\\n");
                sb.append(arr[i].toString());
            }
            return sb.toString();
        } catch (Throwable ignored) {
            return "";
        }
    }
}
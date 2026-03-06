package com.example.framework.trace.hook;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;


import com.example.framework.trace.HookerConfig;
import com.example.framework.trace.base.HookContext;
import com.example.framework.trace.base.HookUnit;
import com.example.framework.trace.tools.LogUtil;

import java.lang.reflect.Method;
import java.util.stream.IntStream;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

/**
 * Activity launch chain trace hooks.
 *
 * Goals:
 *  - Observe startActivity origin (execStartActivity)
 *  - Observe launch execution (LaunchActivityItem.execute OR handleLaunchActivity)
 *  - Observe lifecycle entry (callActivityOnCreate)
 *
 * Notes:
 *  - Works best in app process main process.
 *  - You can later extend to system_server hooks separately (AMS side).
 */
public final class ActivityHooks implements HookUnit {

    @Override
    public String name() {
        return "ActivityHooks";
    }

    @Override
    public int priority() {
        return 100; // earlier than binder hooks if you like
    }

    @Override
    public boolean isEnabled(HookContext ctx) {
        return true; // later: prefs/whitelist
    }

    @Override
    public boolean appliesTo(HookContext ctx) {
        if (ctx.isSystemServer) return false;

        if (!ctx.isMainProcess) return false;

        // 只追目标 app
        return HookerConfig.targetPackages.contains(ctx.packageName);
    }

    @Override
    public void hook(HookContext ctx) throws Throwable {
        hookInstrumentationExecStartActivity(ctx);
        hookInstrumentationCallActivityOnCreate(ctx);
        hookLaunchTransaction(ctx);          // Android 9+ common
        hookActivityThreadHandleLaunch(ctx); // legacy / some ROM paths
        LogUtil.simple( "ActivityHooks installed");
    }

    // ------------------------------------------------------------
    // 1) Instrumentation.execStartActivity (origin)
    // ------------------------------------------------------------

    private void hookInstrumentationExecStartActivity(HookContext ctx) {
        try {
            Class<?> instClz = XposedHelpers.findClass("android.app.Instrumentation", ctx.classLoader);

            // Try common signatures first (best quality: get Intent param)
            // Signature variants across versions/ROMs:
            // execStartActivity(Context who, IBinder contextThread, IBinder token, Activity target, Intent intent, int requestCode, Bundle options)
            // execStartActivity(Context who, IBinder contextThread, IBinder token, String target, Intent intent, int requestCode, Bundle options)
            // + sometimes more params (UserHandle, etc.)
            boolean hooked = false;

            hooked |= tryHookExecStartActivityByIntentIndex(instClz, ctx, "act/execStartActivity");

            if (!hooked) {
                // Fallback: hook all methods named execStartActivity
                XposedBridge.hookAllMethods(instClz, "execStartActivity", new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) {
                        Intent intent = findFirstIntent(param.args);

                        // ✅ trace：发起侧生成并注入
                        String trace = ensureTrace(intent);

                        String comp = (intent != null && intent.getComponent() != null)
                                ? intent.getComponent().flattenToShortString()
                                : "null";

                        Integer flags = null;
                        try { flags = (intent == null) ? null : intent.getFlags(); } catch (Throwable ignored) {}

                        int argsLen = (param.args == null) ? 0 : param.args.length;

                        // 可选：真实方法来源（调试用，不要当 point）
                        String origin = null;
                        try {
                            origin = (param.thisObject == null ? "null" : param.thisObject.getClass().getName())
                                    + "#" + (param.method == null ? "null" : param.method.getName());
                        } catch (Throwable ignored) {}

                        LogUtil.event(
                                "act/execStartActivity",
                                trace,
                                "app/start",
                                LogUtil.attrs()
                                        .put("comp", comp)
                                        .put("flags", flags)
                                        .put("argsLen", argsLen)
                                        .put("origin", origin),
                                null
                        );
                    }
                });
            }
        } catch (Throwable t) {
            LogUtil.error("hook/execStartActivity_fail", "Instrumentation", t);
        }
    }

    /**
     * Try to hook execStartActivity with a heuristic: locate Intent param in args.
     * If we can locate intent reliably, we log richer data.
     */
    private boolean tryHookExecStartActivityByIntentIndex(Class<?> instClz,HookContext ctx, String point) {
        boolean hookedAny = false;
        try {
            Method[] methods = instClz.getDeclaredMethods();
            for (Method m : methods) {
                if (!"execStartActivity".equals(m.getName())) continue;

                Class<?>[] p = m.getParameterTypes();
                int intentIdx = IntStream.range(0, p.length).filter(i -> Intent.class.equals(p[i])).findFirst().orElse(-1);
                if (intentIdx < 0) continue;

                // Hook this overload
                XposedBridge.hookMethod(m, new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) {
                        Intent intent = (Intent) param.args[intentIdx];

                        String trace = ensureTrace(intent); // ⭐ 注入 trace
                        String comp = (intent != null && intent.getComponent() != null)
                                ? intent.getComponent().flattenToShortString()
                                : "null";
                        int requestCode = findFirstInt(param.args, -999);
                        Bundle opts = findFirstBundle(param.args);

                        LogUtil.event("act/execStartActivity", trace, "app/start",
                                LogUtil.attrs()
                                        .put("comp", comp)
                                        .put("req", requestCode)
                                        .put("opts", opts == null ? 0 : 1)
                                        .put("flags",  intent.getFlags()),
                                null
                        );
                    }
                });

                hookedAny = true;
            }
        } catch (Throwable ignored) {
        }
        return hookedAny;
    }

    // ------------------------------------------------------------
    // 2) Instrumentation.callActivityOnCreate (lifecycle entry)
    // ------------------------------------------------------------

    private void hookInstrumentationCallActivityOnCreate(HookContext ctx) {
        try {
            Class<?> instClz = XposedHelpers.findClass("android.app.Instrumentation", ctx.classLoader);

            // Hook all overloads (Activity, Bundle) and (Activity, Bundle, PersistableBundle...)
            XposedBridge.hookAllMethods(instClz, "callActivityOnCreate", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) {
                    Activity a = null;
                    if (param.args != null && param.args.length > 0 && param.args[0] instanceof Activity) {
                        a = (Activity) param.args[0];
                    }
                    String an = (a == null) ? "null" : a.getClass().getName();

                    Intent it = null;
                    try { it = a.getIntent(); } catch (Throwable ignored) {}
                    String trace = readTrace(it);

                    LogUtil.event("inst/callActivityOnCreate",
                            trace, "app/lifecycle",
                            LogUtil.attrs().put("activity", an),
                            null);
                }
            });
        } catch (Throwable t) {
            LogUtil.error("hook/callActivityOnCreate_fail", "Instrumentation", t);
        }
    }

    // ------------------------------------------------------------
    // 3) LaunchActivityItem.execute (Android 9+ / modern path)
    // ------------------------------------------------------------

    private void hookLaunchTransaction(HookContext ctx) {
        try {
            Class<?> itemClz = XposedHelpers.findClass(
                    "android.app.servertransaction.LaunchActivityItem",
                    ctx.classLoader
            );

            // execute(ClientTransactionHandler client, IBinder token, PendingTransactionActions pendingActions)
            XposedBridge.hookAllMethods(itemClz, "execute", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) {
                    // LaunchActivityItem has fields: mIntent, mInfo, mCurConfig, etc.
                    Intent intent = null;
                    try {
                        intent = (Intent) XposedHelpers.getObjectField(param.thisObject, "mIntent");
                    } catch (Throwable ignored) {}

                    String comp = (intent != null && intent.getComponent() != null)
                            ? intent.getComponent().flattenToShortString()
                            : "null";

                    String trace = readTrace(intent);
                    LogUtil.event("actthread/handleLaunchActivity",
                            trace, "app/launch",
                            LogUtil.attrs().put("comp", comp),
                            null);
                }
            });
        } catch (Throwable t) {
            // Some very old devices don't have servertransaction package
            LogUtil.event("hook/LaunchActivityItem_skip", null, null, t.getClass().getSimpleName());
        }
    }

    // ------------------------------------------------------------
    // 4) ActivityThread.handleLaunchActivity (legacy path)
    // ------------------------------------------------------------

    private void hookActivityThreadHandleLaunch(HookContext ctx) {
        try {
            Class<?> atClz = XposedHelpers.findClass("android.app.ActivityThread", ctx.classLoader);

            XposedBridge.hookAllMethods(atClz, "handleLaunchActivity", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) {
                    // First arg often ActivityClientRecord
                    Object r = (param.args != null && param.args.length > 0) ? param.args[0] : null;
                    Intent intent = null;
                    try {
                        if (r != null) {
                            intent = (Intent) XposedHelpers.getObjectField(r, "intent");
                        }
                    } catch (Throwable ignored) {}

                    String comp = (intent != null && intent.getComponent() != null)
                            ? intent.getComponent().flattenToShortString()
                            : "null";

                    String trace = readTrace(intent);
                    LogUtil.event("actthread/handleLaunchActivity",
                            trace, "app/launch",
                            LogUtil.attrs().put("comp", comp),
                            null);
                }
            });
        } catch (Throwable t) {
            LogUtil.error("hook/handleLaunchActivity_fail", "ActivityThread", t);
        }
    }

    // ------------------------------------------------------------
    // Small helpers
    // ------------------------------------------------------------

    private static Intent findFirstIntent(Object[] args) {
        if (args == null) return null;
        for (Object a : args) {
            if (a instanceof Intent) return (Intent) a;
        }
        return null;
    }

    private static Bundle findFirstBundle(Object[] args) {
        if (args == null) return null;
        for (Object a : args) {
            if (a instanceof Bundle) return (Bundle) a;
        }
        return null;
    }

    private static int findFirstInt(Object[] args, int def) {
        if (args == null) return def;
        for (Object a : args) {
            if (a instanceof Integer) return (Integer) a;
        }
        return def;
    }




    private static final String FT_TRACE_KEY = "__ft_trace";
    private static final String FT_SPAN_KEY  = "__ft_span"; // 可选

    private static String ensureTrace(Intent intent) {
        if (intent == null) return null;
        try {
            String t = intent.getStringExtra(FT_TRACE_KEY);
            if (t == null || t.isEmpty()) {
                t = LogUtil.newTraceId();
                intent.putExtra(FT_TRACE_KEY, t);
            }
            return t;
        } catch (Throwable ignored) {
            return null;
        }
    }

    private static String readTrace(Intent intent) {
        if (intent == null) return null;
        try {
            return intent.getStringExtra(FT_TRACE_KEY);
        } catch (Throwable ignored) {
            return null;
        }
    }
}
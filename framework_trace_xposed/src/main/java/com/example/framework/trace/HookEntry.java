package com.example.framework.trace;




import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

import com.example.framework.trace.base.HookContext;
import com.example.framework.trace.base.HookUnit;
import com.example.framework.trace.base.HookRegistry;
import com.example.framework.trace.tools.LogUtil;


/**
 * Pluggable HookEntry for LSPosed/Xposed (FrameworkTrace style).
 *
 * Responsibilities:
 *  - Build HookContext
 *  - Select applicable HookUnit(s)
 *  - Execute in priority order
 *
 * Add new hooks:
 *  - implement HookUnit
 *  - register in HookRegistry.defaultUnits()
 */

import android.os.SystemClock;

import com.example.framework.trace.base.HookContext;
import com.example.framework.trace.base.HookRegistry;
import com.example.framework.trace.base.HookUnit;
import com.example.framework.trace.tools.LogUtil;

import java.util.ArrayList;
import java.util.List;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public final class HookEntry implements IXposedHookLoadPackage, IXposedHookZygoteInit {

    private static volatile String sModulePath;

    @Override
    public void initZygote(StartupParam startupParam) {
        sModulePath = startupParam.modulePath;
        LogUtil.simple("entry/initZygote modulePath=" + sModulePath);
    }

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) {
        if (!isValid(lpparam)) {
            LogUtil.simple("entry/skip invalid lpparam");
            return;
        }

        HookContext ctx = HookContext.from(lpparam, sModulePath);
        LogUtil.simple("entry/seen pkg=" + ctx.packageName + " proc=" + ctx.processName);

        if (shouldSkipProcess(ctx)) return;

        List<HookUnit> all = loadUnits();
        if (all.isEmpty()) {
            LogUtil.simple("entry/skip no units");
            return;
        }

        List<HookUnit> enabled = filterUnits(all, ctx);
        if (enabled.isEmpty()) {
            LogUtil.simple("entry/skip enabled=0 (all filtered) pkg=" + ctx.packageName + " proc=" + ctx.processName);
            return;
        }

        sortUnits(enabled);
        logSummary(ctx, enabled);

        executeUnits(enabled, ctx);
    }

    // ----------------------------
    // Step 1: validate
    // ----------------------------

    private boolean isValid(XC_LoadPackage.LoadPackageParam lp) {
        return lp != null && lp.packageName != null && lp.processName != null;
    }

    // ----------------------------
    // Step 2: skip rules
    // ----------------------------

    private boolean shouldSkipProcess(HookContext ctx) {
        if (ctx.isIsolatedProcess) {
            LogUtil.simple("entry/skip isolated proc=" + ctx.processName);
            return true;
        }
        if (ctx.isChildZygote) {
            LogUtil.simple("entry/skip childZygote proc=" + ctx.processName);
            return true;
        }
        return false;
    }

    // ----------------------------
    // Step 3: registry
    // ----------------------------

    private List<HookUnit> loadUnits() {
        List<HookUnit> all = HookRegistry.defaultUnits();
        LogUtil.simple("entry/units registrySize=" + (all == null ? 0 : all.size()));
        return all == null ? new ArrayList<>() : all;
    }

    // ----------------------------
    // Step 4: filter
    // ----------------------------

    private List<HookUnit> filterUnits(List<HookUnit> all, HookContext ctx) {
        List<HookUnit> enabled = new ArrayList<>(all.size());

        for (HookUnit unit : all) {
            if (unit == null) {
                LogUtil.simple("entry/unitSkip nullUnit");
                continue;
            }

            String uname = safeName(unit);

            try {
                if (!unit.isEnabled(ctx)) {
                    LogUtil.simple("entry/unitSkip disabled name=" + uname);
                    continue;
                }

                if (!unit.appliesTo(ctx)) {
                    LogUtil.simple("entry/unitSkip notApply name=" + uname
                            + " pkg=" + ctx.packageName
                            + " proc=" + ctx.processName
                            + " isSys=" + ctx.isSystemServer
                            + " isAndroidPkg=" + ctx.isAndroidPackage
                            + " isMain=" + ctx.isMainProcess);
                    continue;
                }

                enabled.add(unit);
                LogUtil.simple("entry/unitEnable name=" + uname);

            } catch (Throwable t) {
                LogUtil.error("entry/unitFilterError", uname, t);
            }
        }

        LogUtil.simple("entry/units enabledSize=" + enabled.size());
        return enabled;
    }

    // ----------------------------
    // Step 5: sort + summary
    // ----------------------------

    private void sortUnits(List<HookUnit> enabled) {
        enabled.sort((a, b) -> {
            int pa = a.priority();
            int pb = b.priority();
            if (pa != pb) return Integer.compare(pb, pa);
            return safeName(a).compareTo(safeName(b));
        });
    }

    private void logSummary(HookContext ctx, List<HookUnit> enabled) {
        LogUtil.simple("entry/summary pkg=" + ctx.packageName
                + " proc=" + ctx.processName
                + " pid=" + ctx.pid
                + " uid=" + ctx.uid
                + " sdk=" + ctx.sdk
                + " enabled=" + enabled.size());

        // 可选：打印最终顺序
        for (int i = 0; i < enabled.size(); i++) {
            HookUnit u = enabled.get(i);
            LogUtil.simple("entry/summaryUnit[" + i + "] name=" + safeName(u) + " prio=" + u.priority());
        }

        // 保留你原结构化 event
        LogUtil.event("entry/loadPackage", null, null,
                "pkg=" + ctx.packageName
                        + " proc=" + ctx.processName
                        + " pid=" + ctx.pid
                        + " uid=" + ctx.uid
                        + " sdk=" + ctx.sdk
                        + " units=" + enabled.size()
        );
    }

    // ----------------------------
    // Step 6: execute
    // ----------------------------

    private void executeUnits(List<HookUnit> enabled, HookContext ctx) {
        for (HookUnit unit : enabled) {
            String uname = safeName(unit);
            long t0 = SystemClock.uptimeMillis();

            LogUtil.simple("entry/unitRun name=" + uname + " proc=" + ctx.processName);

            try {
                unit.hook(ctx);
                long dt = SystemClock.uptimeMillis() - t0;
                LogUtil.simple("entry/unitOk name=" + uname + " dt=" + dt + "ms");
                LogUtil.event("entry/unitOk", null, null, uname + " dt=" + dt + "ms");
            } catch (Throwable t) {
                long dt = SystemClock.uptimeMillis() - t0;
                LogUtil.simple("entry/unitFail name=" + uname + " dt=" + dt + "ms err=" + t.getClass().getSimpleName());
                LogUtil.error("entry/unitFail", uname + " dt=" + dt + "ms", t);
            }
        }
    }

    // ----------------------------
    // helpers
    // ----------------------------

    private static String safeName(HookUnit unit) {
        if (unit == null) return "<null>";
        String n = unit.name();
        return (n != null && !n.isEmpty()) ? n : unit.getClass().getName();
    }
}


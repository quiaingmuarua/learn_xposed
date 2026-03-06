package com.example.framework.trace.base;

import android.app.AndroidAppHelper;
import android.os.Build;
import android.os.Process;

import de.robv.android.xposed.callbacks.XC_LoadPackage;

public final class HookContext {
    public final XC_LoadPackage.LoadPackageParam lpparam;

    public final String packageName;
    public final String processName;
    public final ClassLoader classLoader;

    public final int pid;
    public final int uid;
    public final int sdk;

    public final boolean isAndroidPackage;
    public final boolean isSystemServer;
    public final boolean isMainProcess;
    public final boolean isIsolatedProcess;
    public final boolean isChildZygote;

    public final String modulePath;

    private HookContext(XC_LoadPackage.LoadPackageParam lp, String modulePath) {
        this.lpparam = lp;
        this.packageName = lp.packageName;
        this.processName = lp.processName;
        this.classLoader = lp.classLoader;

        this.pid = Process.myPid();
        this.sdk = Build.VERSION.SDK_INT;
        this.modulePath = modulePath;

        // --- uid best-effort (先算出来，后面判 system_server 要用) ---
        this.uid = resolveUidSafe();

        this.isAndroidPackage = "android".equals(this.packageName);

        // main process
        this.isMainProcess = this.packageName != null && this.packageName.equals(this.processName);

        // isolated process (best-effort)
        this.isIsolatedProcess = this.processName != null && this.processName.contains(":isolated");

        // child zygote / usap
        String pn = this.processName == null ? "" : this.processName;
        this.isChildZygote = pn.contains("zygote") || pn.contains("usap");

        // --- system_server detection (Android 13 / ROM / LSPosed 兼容) ---
        // AOSP 常见：processName="system"
        // 有些环境：processName="system_server"
        // 你当前设备：packageName="android", processName="android"，但 uid=1000（system uid）
        this.isSystemServer =
                "system".equals(this.processName)
                        || "system_server".equals(this.processName)
                        || ("android".equals(this.processName) && this.isAndroidPackage && this.uid == 1000)
                        || (this.isAndroidPackage && this.uid == 1000); // 最强兜底
    }

    private static int resolveUidSafe() {
        try {
            if (AndroidAppHelper.currentApplicationInfo() != null) {
                return AndroidAppHelper.currentApplicationInfo().uid;
            }
        } catch (Throwable ignored) {}

        try {
            return android.os.Process.myUid();
        } catch (Throwable ignored) {}

        return -1;
    }

    public static HookContext from(XC_LoadPackage.LoadPackageParam lp, String modulePath) {
        return new HookContext(lp, modulePath);
    }
}
package com.example.framework.trace.base;


public interface HookUnit {
    /** Human readable name */
    String name();

    /** Larger = earlier */
    int priority();

    /** Global on/off (prefs, whitelist, etc.) */
    boolean isEnabled(HookContext ctx);

    /** Decide whether this unit applies for current pkg/proc */
    boolean appliesTo(HookContext ctx);

    /** Install hooks */
    void hook(HookContext ctx) throws Throwable;
}

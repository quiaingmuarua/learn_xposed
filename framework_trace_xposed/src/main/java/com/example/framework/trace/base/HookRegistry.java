package com.example.framework.trace.base;

import com.example.framework.trace.hook.ActivityHooks;
import com.example.framework.trace.hook.ActivityStartServerHooks;

import java.util.ArrayList;
import java.util.List;

public final class HookRegistry {

    public static List<HookUnit> defaultUnits() {
        List<HookUnit> list = new ArrayList<>();

        // system_server 侧（ATMS）
        list.add(new ActivityStartServerHooks());

        // app 进程侧
        list.add(new ActivityHooks());

        return list;
    }
}
package com.demo.java.xposed.rcs.apiCaller.cache;

import com.demo.java.xposed.utils.LogUtils;

import java.lang.ref.WeakReference;

public class CacheMessageInfo {
    private static final CacheMessageInfo instance = new CacheMessageInfo();
    private static WeakReference<Object> adapterRef = new WeakReference<>(null);


    private Object databaseInterfaceImpl;

    public static CacheMessageInfo getInstance() {
        return instance;
    }

    public static void setAdapterRef(Object adapter) {
        adapterRef = new WeakReference<>(adapter);
    }

    public static Object getAdapter() {
        return adapterRef.get();
    }

    public Object getDatabaseInterfaceImpl() {
        return databaseInterfaceImpl;
    }

    public void setDatabaseInterfaceImpl(Object databaseInterfaceImpl) {
        this.databaseInterfaceImpl = databaseInterfaceImpl;
        LogUtils.show("setDatabaseInterfaceImpl databaseInterfaceImpl = " + databaseInterfaceImpl);
    }

}

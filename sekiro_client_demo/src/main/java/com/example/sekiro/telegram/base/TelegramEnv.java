package com.example.sekiro.telegram.base;



import de.robv.android.xposed.XposedHelpers;

public class TelegramEnv {

    private final ClassLoader classLoader;

    public TelegramEnv(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    public ClassLoader getClassLoader() {
        return classLoader;
    }

    public int getCurrentAccount() throws ClassNotFoundException {
        Class<?> userConfigClass = classLoader.loadClass("org.telegram.messenger.UserConfig");
        return XposedHelpers.getStaticIntField(userConfigClass, "selectedAccount");
    }

    public Class<?> loadClass(String name) throws ClassNotFoundException {
        return classLoader.loadClass(name);
    }
}
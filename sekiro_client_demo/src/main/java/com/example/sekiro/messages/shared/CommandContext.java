package com.example.sekiro.messages.shared;

import android.annotation.SuppressLint;
import android.content.Context;

public class CommandContext {

    //单例模式
    @SuppressLint("StaticFieldLeak")
    private static final CommandContext INSTANCE = new CommandContext();



    public static CommandContext getInstance() {
        return INSTANCE;
    }

    private  Context context;


    public static void init(Context context) {
        INSTANCE.context = context;
    }


    public ClassLoader getClassLoader() {
        return context.getClassLoader();
    }



    public Class<?> loadClass(String name) throws ClassNotFoundException {
        return context.getClassLoader().loadClass(name);
    }



    public  Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }
}

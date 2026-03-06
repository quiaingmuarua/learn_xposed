package com.demo.java.xposed.utils;

public class PrintStack {

    static boolean shouldPrintStack = false;


    public static void printStack(String name) {
        if (shouldPrintStack) {
            LogUtils.printStack4(name);
        }

    }


    public static void printStackErrInfo(String name, Exception e){
        // 打印开始标记
        LogUtils.show("Dump Stack: " + name + " ++++++++++++");
        // 打印开始标记
        LogUtils.show("Exception info: " + e.getMessage());

        // 打印每一个堆栈信息
        StackTraceElement[] stackTraceElements = e.getStackTrace();
        for (StackTraceElement element : stackTraceElements) {
            LogUtils.show("\tat " + element.toString());
        }

        // 打印结束标记
        LogUtils.show("End dump Stack: " + name + " ++++++++++++");

    }

    public static void printStackErrInfo(String name, Throwable e){
        // 打印开始标记
        LogUtils.show("Dump Stack: " + name + " ++++++++++++");
        // 打印开始标记
        LogUtils.show("Exception info: " + e.getMessage());

        // 打印每一个堆栈信息
        StackTraceElement[] stackTraceElements = e.getStackTrace();
        for (StackTraceElement element : stackTraceElements) {
            LogUtils.show("\tat " + element.toString());
        }

        // 打印结束标记
        LogUtils.show("End dump Stack: " + name + " ++++++++++++");

    }




}

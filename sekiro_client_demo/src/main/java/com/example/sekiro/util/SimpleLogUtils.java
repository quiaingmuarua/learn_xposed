package com.example.sekiro.util;

import android.util.Log;

import java.util.Arrays;
import java.util.List;

public class SimpleLogUtils {

    private static final String DEFAULT_TAG = "java_xposed_demo";
    private static final String NET_TAG = "XposedNet";
    private static final int LOG_SEGMENT_SIZE = 4000;

    // 命中这些关键字时打印堆栈（可按需改/删）
    private static final List<String> blackList = Arrays.asList(
            "Unrecognized VerificationStatus"
    );

    public static void show(String msg, boolean debugOnly) {
        if (debugOnly) show(DEFAULT_TAG, msg);
    }

    public static void simpleShow(String msg) {
        if (msg == null) return;
        if (msg.length() > 1000) show(msg.substring(0, 1000));
        else show(msg);
    }

    public static void show(String msg) {
        show(DEFAULT_TAG, msg);
    }

    public static void show(String tag, String msg) {
        if (msg == null) return;
        matchKeyword(msg, true);
        printLargeLog(tag, msg);
    }

    public static void e(String msg) {
        if (msg == null) return;
        printLargeLog(DEFAULT_TAG, msg);
    }

    public static void e(String tag, String msg) {
        if (msg == null) return;
        printLargeLog(tag, msg);
    }

    public static void NetLogger(String msg) {
        if (msg == null) return;
        printLargeLog(NET_TAG, msg);
    }

    private static void printLargeLog(String tag, String msg) {
        int msgLength = msg.length();
        int start = 0;
        int part = 0;

        while (start < msgLength) {
            int end = Math.min(start + LOG_SEGMENT_SIZE, msgLength);
            String segment = msg.substring(start, end);
            Log.e(tag + "_part" + part, segment);
            start = end;
            part++;
        }
    }

    public static void printParams(String tag, Object... params) {
        StringBuilder sb = new StringBuilder();
        for (Object param : params) {
            if (param == null) {
                sb.append("null");
            } else if (param instanceof Object[]) {
                sb.append(deepToString((Object[]) param));
            } else {
                sb.append(param);
            }
            sb.append(", ");
        }
        if (sb.length() > 2) sb.setLength(sb.length() - 2);
        show(tag, sb.toString());
    }

    public static String deepToString(Object[] objArr) {
        if (objArr == null) return "null";
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < objArr.length; i++) {
            Object o = objArr[i];
            if (o instanceof Object[]) sb.append(deepToString((Object[]) o));
            else sb.append(o);
            if (i < objArr.length - 1) sb.append(", ");
        }
        sb.append("]");
        return sb.toString();
    }

    private static void matchKeyword(String content, boolean printStack) {
        if (content == null) return;
        for (String keyword : blackList) {
            if (content.contains(keyword) && printStack) {
                printStack4(keyword);
            }
        }
    }

    public static void printStack4(String name) {
        RuntimeException e = new RuntimeException("<Start dump Stack !>");
        e.fillInStackTrace();
        printStackErrInfo(name, e);
    }

    public static void printStackErrInfo(String name, Exception e) {
        show(DEFAULT_TAG, "Dump Stack: " + name + " ++++++++++++");

        if (e == null) {
            show(DEFAULT_TAG, "Exception is null");
            show(DEFAULT_TAG, "End dump Stack: " + name + " ++++++++++++");
            return;
        }

        show(DEFAULT_TAG, "Exception Type: " + e.getClass().getName());
        show(DEFAULT_TAG, "Exception Info: " + e.getMessage());

        for (StackTraceElement element : e.getStackTrace()) {
            show(DEFAULT_TAG, "    at " + element);
        }

        Throwable cause = e.getCause();
        while (cause != null) {
            show(DEFAULT_TAG, "Caused by: " + cause.getClass().getName() + ": " + cause.getMessage());
            for (StackTraceElement element : cause.getStackTrace()) {
                show(DEFAULT_TAG, "    at " + element);
            }
            cause = cause.getCause();
        }

        show(DEFAULT_TAG, "End dump Stack: " + name + " ++++++++++++");
    }
}
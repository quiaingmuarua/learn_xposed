
package com.demo.java.xposed.utils;

import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class LogUtils {

    private static final String DEFAULT_TAG = "java_xposed_demo";
    private static final String NET_TAG = "XposedNet";
    private static final int LOG_SEGMENT_SIZE = 4000;

    private static final boolean ENABLE_FILE_LOG = false;
    private static final File LOG_DIR = new File("/data/local/tmp/xlog/");
    private static final int LOG_RETENTION_DAYS = 7;
    private static final int MAX_LOG_FILE_SIZE_MB = 10;

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
    private static final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss.SSS", Locale.US);

    private static final List<String> blackList = Arrays.asList("Unrecognized VerificationStatus");


    public static void show(String msg, boolean debugOnly) {
        if (debugOnly) {
            show(DEFAULT_TAG, msg);
        }
    }

    public static void simpleShow(String msg){
        if(msg.length()>1000){
            show(msg.substring(0,1000));
        }else {
            show(msg);
        }

    }

    public static void show(String msg) {
        show(DEFAULT_TAG, msg);
    }

    public static void show(String tag, String msg) {
        matchKeyword(msg, true);
        printLargeLog(tag, msg);
        writeLogToFile(tag, msg);
    }

    public static void e(String msg) {
        if (msg != null) {
            printLargeLog(DEFAULT_TAG, msg);
            writeLogToFile(DEFAULT_TAG, msg);
        }
    }

    public static void e(String tag, String msg) {
        if (msg != null) {
            printLargeLog(tag, msg);
            writeLogToFile(tag, msg);
        }
    }

    public static void NetLogger(String msg) {
        if (msg != null) {
            printLargeLog(NET_TAG, msg);
            writeLogToFile(NET_TAG, msg);
        }
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

    private static void writeLogToFile(String tag, String msg) {
        if (!ENABLE_FILE_LOG) return;

        String today = dateFormat.format(new Date());
        String now = timeFormat.format(new Date());

        try {
            File logFile = getLogFile(today);
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(logFile, true))) {
                writer.write("[" + now + "][" + tag + "]: " + msg);
                writer.newLine();
            }
        } catch (IOException e) {
            Log.e("LogUtils", "Write log failed: " + e.getMessage());
        }
    }

    private static File getLogFile(String date) throws IOException {
        int index = 1;
        while (true) {
            File file = new File(LOG_DIR, date + "_" + index + ".log");
            if (!file.exists()) {
                return file;
            }
            if (file.length() < MAX_LOG_FILE_SIZE_MB * 1024 * 1024) {
                return file;
            }
            index++;
        }
    }

    private static void cleanupOldLogs() {
        if (!LOG_DIR.exists() || !LOG_DIR.isDirectory()) return;

        File[] files = LOG_DIR.listFiles((dir, name) -> name.endsWith(".log"));
        if (files == null) return;

        long now = System.currentTimeMillis();
        long maxAge = LOG_RETENTION_DAYS * 24L * 60 * 60 * 1000;

        for (File file : files) {
            if (now - file.lastModified() > maxAge) {
                file.delete();
            }
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
        if (sb.length() > 2) {
            sb.setLength(sb.length() - 2);
        }
        show(tag + " = " + sb.toString());
    }

    public static String deepToString(Object[] objArr) {
        if (objArr == null) return "null";
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < objArr.length; i++) {
            if (objArr[i] instanceof Object[]) {
                sb.append(deepToString((Object[]) objArr[i]));
            } else {
                sb.append(objArr[i]);
            }
            if (i < objArr.length - 1) {
                sb.append(", ");
            }
        }
        sb.append("]");
        return sb.toString();
    }

    private static void matchKeyword(String content, boolean printStack) {
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
        show("Dump Stack: " + name + " ++++++++++++");

        if (e == null) {
            show("Exception is null");
            show("End dump Stack: " + name + " ++++++++++++");
            return;
        }

        show("Exception Type: " + e.getClass().getName());
        show("Exception Info: " + e.getMessage());

        // 检查是否是SSL相关异常
        if (isSslException(e)) {
            show("⚡ SSL Exception Detected! Please check certificates, network, or server configuration.");
        }

        // 打印主异常的堆栈
        for (StackTraceElement element : e.getStackTrace()) {
            show("    at " + element.toString());
        }

        // 递归打印cause链
        Throwable cause = e.getCause();
        while (cause != null) {
            show("Caused by: " + cause.getClass().getName() + ": " + cause.getMessage());
            for (StackTraceElement element : cause.getStackTrace()) {
                show("    at " + element.toString());
            }
            cause = cause.getCause();
        }

        show("End dump Stack: " + name + " ++++++++++++");
    }

    private static boolean isSslException(Throwable throwable) {
        Throwable cause = throwable;
        while (cause != null) {
            if (cause.getClass().getName().toLowerCase().contains("ssl")) {
                return true;
            }
            cause = cause.getCause();
        }
        return false;
    }

}

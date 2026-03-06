package com.example.framework.trace.tools;

import android.os.Process;
import android.os.SystemClock;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

public final class LogUtil {
    private LogUtil() {}

    public static final String TAG = "FrameHook";
    private static final int SEGMENT = 3800;
    private static final SimpleDateFormat TIME_FMT = new SimpleDateFormat("HH:mm:ss.SSS", Locale.US);

    // ---- switches ----
    public static volatile boolean ENABLE_STACK = false;
    public static volatile int STACK_TOP_N = 8;
    public static volatile boolean ENABLE_TOOL_LOG = false;

    // ---- dedup (trace only) ----
    public static volatile boolean ENABLE_DEDUP = true;
    public static volatile long DEDUP_WINDOW_MS = 30;

    private static final Object DEDUP_LOCK = new Object();
    private static long sLastUp = -1;
    private static String sLastKey = null;

    // ---- no-trace launch dedup ----
    public static volatile boolean ENABLE_LAUNCH_DEDUP_NO_TRACE = true;
    private static final Object LAUNCH_LOCK = new Object();
    private static long sLastLaunchUp = -1;
    private static String sLastLaunchComp = null;

    // =========================
    // Public API
    // =========================

    public static void simple(String msg) {
        if (ENABLE_TOOL_LOG) event("xposed_tool", null, "tool", msg, null);
    }

    public static void event(String point, Object data) {
        event(point, null, null, data, null);
    }

    /** spanId 在你当前用法里是 stage，这里沿用参数名但写入 JSON 为 stage */
    public static void event(String point, String traceId, String spanId, Object data) {
        event(point, traceId, spanId, data, null);
    }

    public static void event(String point, String traceId, String stage, Object data, Throwable t) {
        if (shouldDrop(traceId, point, stage, data)) return;
        if (shouldDropNoTraceLaunch(traceId, point, stage, data)) return;
        print(buildJson(point, traceId, stage, data, t));
    }

    public static void error(String point, Object data, Throwable t) {
        event(point, null, null, data, t);
    }

    // =========================
    // Attrs helper
    // =========================

    public static Attrs attrs() { return new Attrs(); }

    public static final class Attrs {
        private final LinkedHashMap<String, Object> m = new LinkedHashMap<>();
        public Attrs put(String k, Object v) { if (k != null) m.put(k, v); return this; }
        Map<String, Object> map() { return m; }
        @Override public String toString() { return m.toString(); }
    }

    // =========================
    // Trace helper
    // =========================

    public static String newTraceId() {
        long x = SystemClock.uptimeMillis()
                ^ ((long) Process.myPid() << 32)
                ^ (long) (Math.random() * 1_000_000);
        return toHex8(x);
    }

    // =========================
    // Dedup
    // =========================

    private static boolean shouldDrop(String traceId, String point, String stage, Object data) {
        if (!ENABLE_DEDUP) return false;
        if (traceId == null) return false;

        long up = SystemClock.uptimeMillis();
        String key = traceId + "|" + point + "|" + stage + "|" + dataKey(data);

        synchronized (DEDUP_LOCK) {
            if (key.equals(sLastKey) && (up - sLastUp) <= DEDUP_WINDOW_MS) return true;
            sLastKey = key;
            sLastUp = up;
            return false;
        }
    }

    private static boolean shouldDropNoTraceLaunch(String traceId, String point, String stage, Object data) {
        if (!ENABLE_LAUNCH_DEDUP_NO_TRACE) return false;
        if (traceId != null) return false;
        if (!"app/launch".equals(stage)) return false;
        if (!"actthread/handleLaunchActivity".equals(point)) return false;

        String comp = null;
        if (data instanceof Attrs) {
            Object c = ((Attrs) data).map().get("comp");
            if (c != null) comp = String.valueOf(c);
        }

        long up = SystemClock.uptimeMillis();
        synchronized (LAUNCH_LOCK) {
            if (comp != null && comp.equals(sLastLaunchComp) && (up - sLastLaunchUp) <= 50) return true;
            sLastLaunchComp = comp;
            sLastLaunchUp = up;
            return false;
        }
    }

    private static String dataKey(Object data) {
        if (data == null) return "";
        if (data instanceof Attrs) {
            Object comp = ((Attrs) data).map().get("comp");
            return comp == null ? "" : String.valueOf(comp);
        }
        String s = String.valueOf(data);
        return s.length() > 64 ? s.substring(0, 64) : s;
    }

    // =========================
    // JSON builder
    // =========================

    private static String buildJson(String point, String traceId, String stage, Object data, Throwable t) {
        long up = SystemClock.uptimeMillis();
        String ts = TIME_FMT.format(new Date());
        int pid = Process.myPid();
        Thread th = Thread.currentThread();

        StringBuilder sb = new StringBuilder(256);
        sb.append('{');
        kv(sb, "ts", ts); sb.append(',');
        kv(sb, "up", up); sb.append(',');
        kv(sb, "pid", pid); sb.append(',');
        kv(sb, "tid", th.getId()); sb.append(',');
        kv(sb, "tname", th.getName()); sb.append(',');
        kv(sb, "point", point);

        if (traceId != null) { sb.append(','); kv(sb, "trace", traceId); }
        if (stage != null)   { sb.append(','); kv(sb, "stage", stage); }

        if (data != null) {
            sb.append(',');
            if (data instanceof Attrs) {
                sb.append("\"attrs\":");
                appendObj(sb, ((Attrs) data).map());
            } else {
                kv(sb, "msg", String.valueOf(data));
            }
        }

        if (t != null) {
            sb.append(",\"err\":{");
            kv(sb, "type", t.getClass().getName()); sb.append(',');
            kv(sb, "msg", t.getMessage() == null ? "" : t.getMessage());
            if (ENABLE_STACK) { sb.append(','); kv(sb, "stack", stackTop(t, STACK_TOP_N)); }
            sb.append('}');
        }

        sb.append('}');
        return sb.toString();
    }

    private static void appendObj(StringBuilder sb, Map<String, Object> map) {
        sb.append('{');
        boolean first = true;
        for (Map.Entry<String, Object> e : map.entrySet()) {
            if (!first) sb.append(',');
            first = false;
            sb.append('\"').append(escape(e.getKey())).append("\":");
            appendVal(sb, e.getValue());
        }
        sb.append('}');
    }

    private static void appendVal(StringBuilder sb, Object v) {
        if (v == null) { sb.append("null"); return; }
        if (v instanceof Number || v instanceof Boolean) { sb.append(v); return; }
        String s = String.valueOf(v);
        if (s.length() > 800) s = s.substring(0, 800) + "...(trunc)";
        sb.append('\"').append(escape(s)).append('\"');
    }

    private static String stackTop(Throwable t, int n) {
        try {
            StackTraceElement[] arr = t.getStackTrace();
            int lim = Math.min(n, arr == null ? 0 : arr.length);
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < lim; i++) {
                if (i > 0) sb.append("\\n");
                sb.append(arr[i]);
            }
            return sb.toString();
        } catch (Throwable ignored) {
            return "";
        }
    }

    private static void kv(StringBuilder sb, String k, Object v) {
        sb.append('\"').append(escape(k)).append("\":");
        if (v == null) sb.append("null");
        else if (v instanceof Number || v instanceof Boolean) sb.append(v);
        else sb.append('\"').append(escape(String.valueOf(v))).append('\"');
    }

    private static String escape(String s) {
        if (s == null) return "";
        StringBuilder out = new StringBuilder(s.length() + 16);
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '\\': out.append("\\\\"); break;
                case '"':  out.append("\\\""); break;
                case '\n': out.append("\\n"); break;
                case '\r': out.append("\\r"); break;
                case '\t': out.append("\\t"); break;
                default:
                    if (c < 0x20) out.append(String.format("\\u%04x", (int) c));
                    else out.append(c);
            }
        }
        return out.toString();
    }

    // =========================
    // Logcat print
    // =========================

    private static void print(String msg) {
        if (msg == null) return;
        int len = msg.length();
        if (len <= SEGMENT) { Log.i(TAG, msg); return; }

        for (int start = 0, part = 0; start < len && part < 50; part++) {
            int end = Math.min(start + SEGMENT, len);
            Log.i(TAG, msg.substring(start, end));
            start = end;
        }
    }

    private static String toHex8(long x) {
        String h = Long.toHexString(x);
        return h.length() <= 8 ? h : h.substring(h.length() - 8);
    }
}
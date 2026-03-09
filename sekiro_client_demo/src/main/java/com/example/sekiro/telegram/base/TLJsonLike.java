package com.example.sekiro.telegram.base;

import java.lang.reflect.*;
import java.nio.ByteBuffer;
import java.util.*;

/**
 * TLObject -> Map (for observation/logging)
 * - Supports nested TLObject / Lists / arrays / Maps
 * - Handles byte[], ByteBuffer (direct/heap), and "NativeByteBuffer-like" objects by reflection if possible
 * - Cycle-safe, depth-limited
 */
public final class TLJsonLike {

    public static class Options {
        /** max recursion depth to avoid huge graphs */
        public int maxDepth = 8;
        /** max collection elements to dump */
        public int maxCollectionSize = 50;
        /** max bytes to dump (hex preview) */
        public int maxBytesPreview = 256;
        /** include null fields */
        public boolean includeNulls = false;
        /** include class tag field */
        public boolean includeTypeTag = true;
        /** field name used for type */
        public String typeTagName = "_";
        /** also include constructor id if found (field name "constructor" or method getConstructorId/constructorId) */
        public boolean includeConstructorId = true;

        /** skip static fields */
        public boolean skipStatic = true;
        /** skip synthetic fields */
        public boolean skipSynthetic = true;

        /** ignore these field names (common internals) */
        public Set<String> ignoreFieldNames = new HashSet<>(Arrays.asList(
                "cachedSize", "serializedSize", "networkType", "disableUpdates",
                "javaByteBuffer", "buffer", "_buffer", "ptr", "address", "nativePtr"
        ));

        /** ignore fields by prefix */
        public List<String> ignoreFieldNamePrefixes = Arrays.asList("this$");
    }

    private TLJsonLike() {}

    public static Map<String, Object> toMap(Object root) {
        return toMap(root, new Options());
    }

    public static Map<String, Object> toMap(Object root, Options opt) {
        IdentityHashMap<Object, Boolean> seen = new IdentityHashMap<>();
        Object v = toValue(root, opt, 0, seen);
        if (v instanceof Map) return castMap(v);
        Map<String, Object> wrap = new LinkedHashMap<>();
        wrap.put(opt.typeTagName, (root == null) ? "null" : root.getClass().getName());
        wrap.put("value", v);
        return wrap;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> castMap(Object o) { return (Map<String, Object>) o; }

    private static Object toValue(Object o, Options opt, int depth, IdentityHashMap<Object, Boolean> seen) {
        if (o == null) return null;
        if (depth > opt.maxDepth) return "<maxDepth:" + opt.maxDepth + ">";

        // primitives / boxed / string / enums
        if (o instanceof String || o instanceof Number || o instanceof Boolean || o instanceof Character) return o;
        if (o.getClass().isEnum()) return ((Enum<?>) o).name();

        // cycle guard (only for non-trivial objects)
        if (isCycleProne(o)) {
            if (seen.containsKey(o)) {
                return "<cycle@" + System.identityHashCode(o) + ":" + o.getClass().getSimpleName() + ">";
            }
            seen.put(o, Boolean.TRUE);
        }

        // byte[]
        if (o instanceof byte[]) return bytesInfo((byte[]) o, opt);

        // ByteBuffer (direct/heap)
        if (o instanceof ByteBuffer) return byteBufferInfo((ByteBuffer) o, opt);

        // arrays
        Class<?> c = o.getClass();
        if (c.isArray()) {
            int n = Array.getLength(o);
            int limit = Math.min(n, opt.maxCollectionSize);
            List<Object> arr = new ArrayList<>(limit);
            for (int i = 0; i < limit; i++) arr.add(toValue(Array.get(o, i), opt, depth + 1, seen));
            if (n > limit) arr.add("<truncated:" + (n - limit) + " more>");
            return arr;
        }

        // collections
        if (o instanceof Collection) {
            Collection<?> col = (Collection<?>) o;
            int limit = Math.min(col.size(), opt.maxCollectionSize);
            List<Object> out = new ArrayList<>(limit);
            int i = 0;
            for (Object e : col) {
                if (i++ >= limit) break;
                out.add(toValue(e, opt, depth + 1, seen));
            }
            if (col.size() > limit) out.add("<truncated:" + (col.size() - limit) + " more>");
            return out;
        }

        // maps
        if (o instanceof Map) {
            Map<?, ?> m = (Map<?, ?>) o;
            Map<String, Object> out = new LinkedHashMap<>();
            int i = 0;
            for (Map.Entry<?, ?> e : m.entrySet()) {
                if (i++ >= opt.maxCollectionSize) {
                    out.put("<truncated>", "<" + (m.size() - opt.maxCollectionSize) + " more>");
                    break;
                }
                String k = String.valueOf(e.getKey());
                out.put(k, toValue(e.getValue(), opt, depth + 1, seen));
            }
            return out;
        }

        // Try "NativeByteBuffer-like" objects: has getJavaByteBuffer()/toByteArray()/bytes
        Object maybeNbb = tryNativeByteBufferLike(o, opt);
        if (maybeNbb != null) return maybeNbb;

        // fallback: reflect fields -> map
        return reflectObject(o, opt, depth, seen);
    }

    private static boolean isCycleProne(Object o) {
        return !(o instanceof String || o instanceof Number || o instanceof Boolean || o instanceof Character);
    }

    private static Map<String, Object> reflectObject(Object o, Options opt, int depth, IdentityHashMap<Object, Boolean> seen) {
        Map<String, Object> out = new LinkedHashMap<>();

        if (opt.includeTypeTag) {
            out.put(opt.typeTagName, o.getClass().getSimpleName());
        }

        if (opt.includeConstructorId) {
            Object ctor = tryGetConstructorId(o);
            if (ctor != null) out.put("_id", ctor);
        }

        for (Field f : getAllFields(o.getClass())) {
            if (!shouldIncludeField(f, opt)) continue;
            String name = f.getName();
            if (opt.ignoreFieldNames.contains(name)) continue;
            if (startsWithAny(name, opt.ignoreFieldNamePrefixes)) continue;

            try {
                f.setAccessible(true);
                Object v = f.get(o);
                if (v == null && !opt.includeNulls) continue;
                out.put(name, toValue(v, opt, depth + 1, seen));
            } catch (Throwable t) {
                out.put(name, "<err:" + t.getClass().getSimpleName() + ">");
            }
        }
        return out;
    }

    private static boolean startsWithAny(String s, List<String> prefixes) {
        for (String p : prefixes) if (s.startsWith(p)) return true;
        return false;
    }

    private static boolean shouldIncludeField(Field f, Options opt) {
        int mod = f.getModifiers();
        if (opt.skipStatic && Modifier.isStatic(mod)) return false;
        if (opt.skipSynthetic && f.isSynthetic()) return false;
        return true;
    }

    private static List<Field> getAllFields(Class<?> cls) {
        List<Field> fs = new ArrayList<>();
        Class<?> c = cls;
        while (c != null && c != Object.class) {
            try {
                fs.addAll(Arrays.asList(c.getDeclaredFields()));
            } catch (Throwable ignored) {}
            c = c.getSuperclass();
        }
        return fs;
    }

    /** Try to extract a "constructor id" for TL-like objects (best-effort). */
    private static Object tryGetConstructorId(Object o) {
        // Common in Telegram TL classes: static int constructor
        try {
            Field f = findField(o.getClass(), "constructor");
            if (f != null && Modifier.isStatic(f.getModifiers())) {
                f.setAccessible(true);
                Object v = f.get(null);
                if (v instanceof Integer) return String.format("0x%08X", (Integer) v);
            }
        } catch (Throwable ignored) {}

        // Some have method: getConstructorId()/constructorId()
        for (String mname : new String[]{"getConstructorId", "constructorId", "getConstructor"}) {
            try {
                Method m = o.getClass().getMethod(mname);
                m.setAccessible(true);
                Object v = m.invoke(o);
                if (v instanceof Integer) return String.format("0x%08X", (Integer) v);
            } catch (Throwable ignored) {}
        }
        return null;
    }

    private static Field findField(Class<?> cls, String name) {
        Class<?> c = cls;
        while (c != null && c != Object.class) {
            try {
                return c.getDeclaredField(name);
            } catch (NoSuchFieldException ignored) {
                c = c.getSuperclass();
            }
        }
        return null;
    }

    /** bytes -> {len, hexPreview} */
    private static Map<String, Object> bytesInfo(byte[] b, Options opt) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("len", b.length);
        int n = Math.min(b.length, opt.maxBytesPreview);
        out.put("hex", toHex(b, 0, n) + (b.length > n ? "...(+"
                + (b.length - n) + " bytes)" : ""));
        return out;
    }

    /** ByteBuffer -> {direct, pos, lim, cap, hexPreview} (does not consume original position) */
    private static Map<String, Object> byteBufferInfo(ByteBuffer buf, Options opt) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("direct", buf.isDirect());
        out.put("position", buf.position());
        out.put("limit", buf.limit());
        out.put("capacity", buf.capacity());

        try {
            ByteBuffer dup = buf.duplicate();
            dup.clear(); // pos=0, lim=cap
            int n = Math.min(dup.remaining(), opt.maxBytesPreview);
            byte[] tmp = new byte[n];
            dup.get(tmp);
            out.put("hex", toHex(tmp, 0, tmp.length) + (dup.remaining() > 0 ? "...(+" + dup.remaining() + " bytes)" : ""));
        } catch (Throwable t) {
            out.put("hex", "<err:" + t.getClass().getSimpleName() + ">");
        }
        return out;
    }

    /** Best-effort for Telegram NativeByteBuffer-like: try methods/fields that expose ByteBuffer/byte[]. */
    private static Object tryNativeByteBufferLike(Object o, Options opt) {
        String cn = o.getClass().getName();
        // Heuristic: class name contains "NativeByteBuffer" or "ByteBuffer" (custom)
        boolean maybe = cn.contains("NativeByteBuffer") || cn.endsWith("ByteBuffer");
        if (!maybe) {
            // still try a couple of common accessors cheaply
            // (no-op)
        }

        // Try getJavaByteBuffer(): ByteBuffer
        try {
            Method m = o.getClass().getMethod("getJavaByteBuffer");
            m.setAccessible(true);
            Object r = m.invoke(o);
            if (r instanceof ByteBuffer) return byteBufferInfo((ByteBuffer) r, opt);
        } catch (Throwable ignored) {}

        // Try toByteArray(): byte[]
        try {
            Method m = o.getClass().getMethod("toByteArray");
            m.setAccessible(true);
            Object r = m.invoke(o);
            if (r instanceof byte[]) return bytesInfo((byte[]) r, opt);
        } catch (Throwable ignored) {}

        // Try field "buffer" as byte[] or ByteBuffer
        try {
            Field f = findField(o.getClass(), "buffer");
            if (f != null) {
                f.setAccessible(true);
                Object r = f.get(o);
                if (r instanceof byte[]) return bytesInfo((byte[]) r, opt);
                if (r instanceof ByteBuffer) return byteBufferInfo((ByteBuffer) r, opt);
            }
        } catch (Throwable ignored) {}

        return null;
    }

    private static String toHex(byte[] b, int off, int len) {
        char[] hex = "0123456789ABCDEF".toCharArray();
        StringBuilder sb = new StringBuilder(len * 2);
        for (int i = 0; i < len; i++) {
            int v = b[off + i] & 0xFF;
            sb.append(hex[v >>> 4]).append(hex[v & 0x0F]);
        }
        return sb.toString();
    }
}

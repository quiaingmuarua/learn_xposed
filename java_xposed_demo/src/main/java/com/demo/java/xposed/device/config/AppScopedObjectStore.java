package com.demo.java.xposed.device.config;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ConcurrentHashMap;

public final class AppScopedObjectStore {

    private static final Gson GSON_IO = new GsonBuilder()
            .serializeNulls()
            .create();

    /** 安全写：原子写入 ctx.getNoBackupFilesDir()/baseName.json */
    public static <T> String writeSafePkgJson(Context ctx, String baseName, T obj) {
        if (ctx == null || baseName == null || baseName.isEmpty()) return null;
        try {
            File dir = ctx.getNoBackupFilesDir();
            if (dir == null) return null;
            if (!dir.exists()) dir.mkdirs();

            File dst = new File(dir, baseName.endsWith(".json") ? baseName : (baseName + ".json"));
            File tmp = new File(dir, dst.getName() + ".tmp");

            String json = GSON_IO.toJson(obj);

            FileOutputStream out = null;
            try {
                out = new FileOutputStream(tmp, false);
                out.write(json.getBytes(StandardCharsets.UTF_8));
                out.getFD().sync();
            } finally {
                try { if (out != null) out.close(); } catch (Throwable ignored) {}
            }

            // atomic replace
            if (dst.exists()) dst.delete();
            boolean ok = tmp.renameTo(dst);
            if (!ok) {
                // fallback copy
                FileInputStream in = null;
                FileOutputStream o2 = null;
                try {
                    in = new FileInputStream(tmp);
                    o2 = new FileOutputStream(dst, false);
                    byte[] buf = new byte[16 * 1024];
                    int n;
                    while ((n = in.read(buf)) > 0) o2.write(buf, 0, n);
                    o2.getFD().sync();
                } finally {
                    try { if (in != null) in.close(); } catch (Throwable ignored) {}
                    try { if (o2 != null) o2.close(); } catch (Throwable ignored) {}
                }
            }

            tmp.delete();
            return dst.getAbsolutePath();
        } catch (Throwable ignored) {
            return ignored.getMessage();
        }
    }

    /** 安全读：从 ctx.getNoBackupFilesDir()/baseName.json 读出对象；不存在/失败返回 null */
    public static <T> T readSafePkgJson(Context ctx, String baseName, Class<T> clazz) {
        if (ctx == null || baseName == null || baseName.isEmpty() || clazz == null) return null;
        FileInputStream in = null;
        try {
            File dir = ctx.getNoBackupFilesDir();
            if (dir == null) return null;

            File file = new File(dir, baseName.endsWith(".json") ? baseName : (baseName + ".json"));
            if (!file.exists() || !file.isFile() || file.length() <= 0) return null;

            in = new FileInputStream(file);
            byte[] buf = new byte[(int) file.length()];
            int r = 0;
            while (r < buf.length) {
                int n = in.read(buf, r, buf.length - r);
                if (n <= 0) break;
                r += n;
            }
            String json = new String(buf, 0, r, StandardCharsets.UTF_8);
            return GSON_IO.fromJson(json, clazz);
        } catch (Throwable ignored) {
            return null;
        } finally {
            try { if (in != null) in.close(); } catch (Throwable ignored) {}
        }
    }

}

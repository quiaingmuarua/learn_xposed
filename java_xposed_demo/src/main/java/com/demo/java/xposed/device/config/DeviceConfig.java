package com.demo.java.xposed.device.config;

import com.demo.java.xposed.utils.FileUtils;
import com.demo.java.xposed.utils.LogUtils;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.demo.java.xposed.device.model.FakeDeviceInfo;

import java.io.FileReader;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public final class DeviceConfig {

    public static final String DEFAULT_PATH = "/data/local/tmp/fake_fingerprint.json";

    private static final Gson GSON = new GsonBuilder()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .create();

    private static final Type DEVICE_LIST_TYPE =
            new TypeToken<List<FakeDeviceInfo>>() {}.getType();

    private static final Type VERSION_LIST_TYPE =
            new TypeToken<List<FakeDeviceInfo.FakeAndroidVersionInfo>>() {}.getType();

    private static final class CacheEntry {
        volatile JsonObject root;
        volatile String lastModified;
    }

    private static final ConcurrentHashMap<String, CacheEntry> CACHE = new ConcurrentHashMap<>();

    private DeviceConfig() {}

    private static JsonObject getRoot(String filePath) {
        if (filePath == null || filePath.isEmpty()) filePath = DEFAULT_PATH;

        String lm = FileUtils.getFileLastModifiedTimestamp(filePath);
        CacheEntry entry = CACHE.computeIfAbsent(filePath, _k -> new CacheEntry());

        // 命中缓存
        if (entry.root != null && lm != null && lm.equals(entry.lastModified)) {
            return entry.root;
        }

        // 重新加载
        try (FileReader reader = new FileReader(filePath)) {
            JsonElement el = JsonParser.parseReader(reader);
            if (el != null && el.isJsonObject()) {
                entry.root = el.getAsJsonObject();
                entry.lastModified = lm;
                return entry.root;
            }
        } catch (Exception e) {
            LogUtils.show("DeviceConfig parse failed: " + filePath + " e=" + e);
        }

        entry.root = new JsonObject();
        entry.lastModified = lm;
        return entry.root;
    }

    private static JsonObject getObj(JsonObject obj, String key) {
        if (obj == null) return null;
        JsonElement el = obj.get(key);
        return (el != null && el.isJsonObject()) ? el.getAsJsonObject() : null;
    }

    private static JsonObject getAppObject(JsonObject root, String packageName) {
        if (root == null || packageName == null || packageName.isEmpty()) return null;
        JsonObject apps = getObj(root, "apps");
        if (apps == null) return null;
        JsonElement appEl = apps.get(packageName);
        return (appEl != null && appEl.isJsonObject()) ? appEl.getAsJsonObject() : null;
    }

    private static <T> List<T> readList(JsonObject container, String key, Type type) {
        if (container == null) return Collections.emptyList();
        JsonElement el = container.get(key);
        if (el == null || !el.isJsonArray()) return Collections.emptyList();
        try {
            List<T> list = GSON.fromJson(el, type);
            return list == null ? Collections.emptyList() : list;
        } catch (Exception ignored) {
            return Collections.emptyList();
        }
    }

    private static <T> List<T> getListSection(String filePath, String packageName, String key, Type type) {
        JsonObject root = getRoot(filePath);

        List<T> fromApp = readList(getAppObject(root, packageName), key, type);
        if (!fromApp.isEmpty()) return fromApp;

        List<T> fromDefault = readList(getObj(root, "default"), key, type);
        if (!fromDefault.isEmpty()) return fromDefault;

        List<T> fromRoot = readList(root, key, type);
        if (!fromRoot.isEmpty()) return fromRoot;

        return Collections.emptyList();
    }

    // ====== 你工程当前用到的两个方法签名：保留以确保能跑 ======

    public static List<FakeDeviceInfo> getDeviceProfiles(String filePath, String packageName) {
        return getListSection(filePath, packageName, "device_profiles", DEVICE_LIST_TYPE);
    }

    public static List<FakeDeviceInfo.FakeAndroidVersionInfo> getAndroidVersions(String filePath, String packageName) {
        return getListSection(filePath, packageName, "android_versions", VERSION_LIST_TYPE);
    }

    // 可选：清缓存
    public static void invalidate(String filePath) {
        if (filePath == null || filePath.isEmpty()) filePath = DEFAULT_PATH;
        CACHE.remove(filePath);
    }
}

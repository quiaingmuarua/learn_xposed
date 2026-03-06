package com.demo.java.xposed.device.config;

import com.demo.java.xposed.utils.FileUtils;
import com.demo.java.xposed.utils.LogUtils;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.FileReader;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class SimCardConfig {

    public static final String DEFAULT_PATH = "/data/local/tmp/fake_fingerprint.json";

    // 缓存：整个 JSON 根对象
    private static volatile JsonObject rootCache = null;
    private static volatile String lastModifiedCache = null;

    private static final Gson GSON = new Gson();

    /** 读取并缓存（文件有变化才会重新解析） */
    private static JsonObject getRoot(String filePath) {
        String lm = FileUtils.getFileLastModifiedTimestamp(filePath);

        // 命中缓存
        if (rootCache != null && lm.equals(lastModifiedCache)) {
            return rootCache;
        }

        // 重新加载
        try (FileReader reader = new FileReader(filePath)) {
            JsonElement el = JsonParser.parseReader(reader);
            if (el != null && el.isJsonObject()) {
                rootCache = el.getAsJsonObject();
                lastModifiedCache = lm;
                return rootCache;
            }
        } catch (Exception e) {
            LogUtils.show("Failed to parse config.json " + filePath + " e=" + e);
        }

        // 失败时：不要污染旧缓存（如果你希望失败后仍用旧缓存，可改这里逻辑）
        rootCache = new JsonObject();
        lastModifiedCache = lm;
        LogUtils.show("Failed to parse config.json " + filePath);
        return rootCache;
    }

    /** 按包名取某个 section：sim_info / real_sim_info / info */
    private static Map<String, String> getSection(String filePath, String packageName, String sectionKey) {
        JsonObject root = getRoot(filePath);
        if (root == null) return Collections.emptyMap();

        // 1) apps[packageName][sectionKey]
        JsonObject appObj = getAppObject(root, packageName);
        Map<String, String> fromApp = objToStringMap(appObj, sectionKey);
        if (!fromApp.isEmpty()) return fromApp;

        // 2) default[sectionKey]
        Map<String, String> fromDefault = objToStringMap(getObj(root, "default"), sectionKey);
        if (!fromDefault.isEmpty()) return fromDefault;

        // 3) 旧结构：root[sectionKey]
        Map<String, String> fromRoot = objToStringMap(root, sectionKey);
        if (!fromRoot.isEmpty()) return fromRoot;

        return Collections.emptyMap();
    }

    private static JsonObject getAppObject(JsonObject root, String packageName) {
        if (packageName == null || packageName.isEmpty()) return null;
        JsonObject apps = getObj(root, "apps");
        if (apps == null) return null;
        JsonElement appEl = apps.get(packageName);
        if (appEl != null && appEl.isJsonObject()) {
            return appEl.getAsJsonObject();
        }
        return null;
    }

    private static JsonObject getObj(JsonObject obj, String key) {
        if (obj == null) return null;
        JsonElement el = obj.get(key);
        if (el != null && el.isJsonObject()) return el.getAsJsonObject();
        return null;
    }

    private static Map<String, String> objToStringMap(JsonObject container, String key) {
        if (container == null) return Collections.emptyMap();
        JsonElement el = container.get(key);
        if (el == null || !el.isJsonObject()) return Collections.emptyMap();

        try {
            // JsonObject -> Map<String, String>
            Map<String, Object> tmp = GSON.fromJson(el, Map.class);
            if (tmp == null || tmp.isEmpty()) return Collections.emptyMap();

            Map<String, String> out = new HashMap<>();
            for (Map.Entry<String, Object> e : tmp.entrySet()) {
                out.put(e.getKey(), e.getValue() == null ? "" : String.valueOf(e.getValue()));
            }
            return out;
        } catch (Exception ex) {
            return Collections.emptyMap();
        }
    }

    // --------- 对外 API（兼容旧调用 + 新包名调用） ---------

    /** 旧代码兼容：不传包名时，默认走旧结构/或 default */
    public static Map<String, String> getSimInfo() {
        return getSimInfo(null);
    }

    public static Map<String, String> getSimInfo(String packageName) {
        return getSection(DEFAULT_PATH, packageName, "sim_info");
    }

    public static Map<String, String> getRealSimInfo() {
        return getRealSimInfo(null);
    }

    public static Map<String, String> getRealSimInfo(String packageName) {
        return getSection(DEFAULT_PATH, packageName, "real_sim_info");
    }

    public static Map<String, String> getVersionConfig() {
        return getVersionConfig(null);
    }

    public static Map<String, String> getVersionConfig(String packageName) {
        return getSection(DEFAULT_PATH, packageName, "info");
    }

    public static String getFakeFingerprintTime() {
        return FileUtils.getFileLastModifiedTimestamp(DEFAULT_PATH);
    }
}

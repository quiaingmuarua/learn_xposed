package com.demo.java.xposed.device.model;

import android.content.Context;

import com.demo.java.xposed.device.config.AppScopedObjectStore;
import com.demo.java.xposed.device.config.DeviceConfig;
import com.demo.java.xposed.utils.LogUtils;
import com.demo.java.xposed.utils.RandomUtils;

import com.google.gson.Gson;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

public class FakeDeviceInfo {

    // =================== 配置文件策略 ===================
    // 全局模板源（只在 app 私有缓存不存在时导入一次）
    private static final String GLOBAL_TEMPLATE_PATH = "/data/local/tmp/fake_fingerprint.json";
    // app 私有目录缓存文件名（pm clear 后会消失）
    private static final String APP_CACHE_FILE_NAME = "fake_fingerprint.json";

    // =================== 缓存：按 packageName 保存生成好的 FakeDeviceInfo ===================
    private static final ConcurrentHashMap<String, FakeDeviceInfo> INSTANCES = new ConcurrentHashMap<>();
    private volatile static FakeDeviceInfo instance; // 兼容旧无参 getInstance()

    // 深拷贝用（避免污染模板对象）
    private static final Gson COPY_GSON = new Gson();

    // =================== 原字段：保持不动 ===================
    private String product;
    private String fingerprint;
    private String fingerprintRaw;
    private String id;
    private String manufacturer;
    private String hardware;
    private String tags;
    private String brand;
    private String model;
    private String type;
    private String display;
    private String radio;

    private String androidId;
    private FakeAndroidVersionInfo fakeAndroidVersionInfo;

    // =================== getters：保持不动 ===================
    public String getAndroidId() { return androidId; }
    public String getProduct() { return product; }
    public String getFingerprint() { return fingerprint; }
    public String getFingerprintRaw() { return fingerprintRaw; }
    public String getId() { return id; }
    public String getManufacturer() { return manufacturer; }
    public String getRadio() { return radio; }
    public String getHardware() { return hardware; }
    public String getTags() { return tags; }
    public String getBrand() { return brand; }
    public String getModel() { return model; }
    public String getType() { return type; }
    public String getDisplay() { return display; }
    public FakeAndroidVersionInfo getFakeAndroidVersionInfo() { return fakeAndroidVersionInfo; }

    // =================== 新入口：按 app 私有目录读取（推荐使用） ===================
    private static FakeDeviceInfo getInstance(Context appContext, String packageName) {

        String pkg = (packageName == null || packageName.isEmpty()) ? appContext.getPackageName() : packageName;

        // 1) 确保私有缓存文件存在：不存在则从 /data/local/tmp 导入一次
        File appCacheFile = ensureAppCacheFile(appContext);

        // 2) 按包名缓存生成的 FakeDeviceInfo（只要私有文件没被清除，永远稳定）
        return INSTANCES.computeIfAbsent(pkg, _k -> createFromFile(appCacheFile.getAbsolutePath(), pkg));
    }

    public static  FakeDeviceInfo tryGetCachedInstance(Context appContext){
        FakeDeviceInfo fakeDeviceInfo=AppScopedObjectStore.readSafePkgJson(appContext,"device_finger",FakeDeviceInfo.class);
        LogUtils.show("tryGetCachedInstance readSafePkgJson"+fakeDeviceInfo);
        if(fakeDeviceInfo!=null){return fakeDeviceInfo;}
        fakeDeviceInfo= getInstance(appContext);
        String filePath=AppScopedObjectStore.writeSafePkgJson(appContext,"device_finger",fakeDeviceInfo);
        LogUtils.show("AppScopedObjectStore writeSafePkgJson "+filePath);
        return fakeDeviceInfo;


    }

    // 你也可以提供一个更简洁的重载
    private static FakeDeviceInfo getInstance(Context appContext) {
        if (appContext == null) return null;
        return getInstance(appContext, appContext.getPackageName());
    }

    // =================== 私有目录导入逻辑 ===================
    private static File ensureAppCacheFile(Context c) {
        LogUtils.show("ensureAppCacheFile ctx "+c);
        // no_backup：不容易被系统备份/恢复干扰；pm clear 会清掉
        File dir = c.getNoBackupFilesDir();
        File cacheFile = new File(dir, APP_CACHE_FILE_NAME);

        // 已存在且非空，直接用
        if (cacheFile.exists() && cacheFile.isFile() && cacheFile.length() > 0) {
            return cacheFile;
        }

        synchronized (FakeDeviceInfo.class) {
            // double-check
            if (cacheFile.exists() && cacheFile.isFile() && cacheFile.length() > 0) {
                return cacheFile;
            }

            File src = new File(GLOBAL_TEMPLATE_PATH);

            // 模板不存在 -> 写一个最小 JSON，保证后续读取不会崩
            if (!src.exists() || !src.isFile() || src.length() == 0) {
                writeTextAtomically(cacheFile, "{}");
                return cacheFile;
            }

            // 原子导入：先写 tmp，再 rename
            File tmp = new File(dir, APP_CACHE_FILE_NAME + ".tmp");
            copyFile(src, tmp);
            atomicRename(tmp, cacheFile);

            return cacheFile;
        }
    }

    private static void copyFile(File src, File dst) {
        FileInputStream in = null;
        FileOutputStream out = null;
        try {
            in = new FileInputStream(src);
            out = new FileOutputStream(dst, false);
            byte[] buf = new byte[16 * 1024];
            int n;
            while ((n = in.read(buf)) > 0) {
                out.write(buf, 0, n);
            }
            out.getFD().sync(); // 尽量落盘
        } catch (Exception ignored) {
        } finally {
            try { if (in != null) in.close(); } catch (Exception ignored) {}
            try { if (out != null) out.close(); } catch (Exception ignored) {}
        }
    }

    private static void atomicRename(File src, File dst) {
        try {
            if (dst.exists()) dst.delete();
            boolean ok = src.renameTo(dst);
            if (!ok) {
                // 兜底：rename 失败再 copy 一次
                copyFile(src, dst);
                src.delete();
            }
        } catch (Exception ignored) {
        }
    }

    private static void writeTextAtomically(File dst, String content) {
        File dir = dst.getParentFile();
        File tmp = new File(dir, dst.getName() + ".tmp");
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(tmp, false);
            out.write(content.getBytes(StandardCharsets.UTF_8));
            out.getFD().sync();
        } catch (Exception ignored) {
        } finally {
            try { if (out != null) out.close(); } catch (Exception ignored) {}
        }
        atomicRename(tmp, dst);
    }

    // =================== 核心：从指定 filePath 生成一份 FakeDeviceInfo ===================
    private static FakeDeviceInfo createFromFile(String filePath, String packageName) {
        List<FakeDeviceInfo> profiles = DeviceConfig.getDeviceProfiles(filePath, packageName);
        if (profiles.isEmpty()) {
            return new FakeDeviceInfo();
        }

        FakeDeviceInfo picked = RandomUtils.getRandomStringFromList(profiles);
        FakeDeviceInfo deviceInfo = deepCopyDevice(picked);

        deviceInfo.fakeAndroidVersionInfo = randomFakeAndroidVersionInfo(filePath, packageName);
        deviceInfo.androidId = genDeviceId();

        deviceInfo.fingerprintRaw = rewriteFingerprintRelease(
                deviceInfo.fingerprint,
                deviceInfo.fakeAndroidVersionInfo == null ? null : deviceInfo.fakeAndroidVersionInfo.release
        );

        deviceInfo.fingerprint = genFingerPrint(deviceInfo);
        return deviceInfo;
    }

    private static FakeDeviceInfo deepCopyDevice(FakeDeviceInfo src) {
        if (src == null) return new FakeDeviceInfo();
        return COPY_GSON.fromJson(COPY_GSON.toJson(src), FakeDeviceInfo.class);
    }

    private static FakeAndroidVersionInfo deepCopyVersion(FakeAndroidVersionInfo src) {
        if (src == null) return new FakeAndroidVersionInfo();
        return COPY_GSON.fromJson(COPY_GSON.toJson(src), FakeAndroidVersionInfo.class);
    }

    // =================== 生成/派生字段：保持你的逻辑（稍微加了防御） ===================
    public static String genDeviceId() {
        Random random = new Random();
        StringBuilder sb = new StringBuilder(16);

        char firstChar = (char) ('a' + random.nextInt(6)); // 'a' ~ 'f'
        sb.append(firstChar);

        String characters = "0123456789abcdef";
        for (int i = 1; i < 16; i++) {
            int index = random.nextInt(characters.length());
            sb.append(characters.charAt(index));
        }
        return sb.toString();
    }

    public static String genFingerPrint(FakeDeviceInfo fakeDeviceInfo) {
        if (fakeDeviceInfo == null || fakeDeviceInfo.getFakeAndroidVersionInfo() == null) {
            return fakeDeviceInfo == null ? "" : fakeDeviceInfo.fingerprint;
        }
        return fakeDeviceInfo.brand + '/' +
                fakeDeviceInfo.product + '/' +
                fakeDeviceInfo.product + ':' +
                fakeDeviceInfo.getFakeAndroidVersionInfo().release + '/' +
                fakeDeviceInfo.id + '/' +
                fakeDeviceInfo.getFakeAndroidVersionInfo().incremental + ':' +
                fakeDeviceInfo.type + '/' +
                fakeDeviceInfo.tags;
    }

    /**
     * 更稳的 release 替换：
     * brand/name/device:release/id/incremental:type/tags
     */
    private static String rewriteFingerprintRelease(String fingerprint, String newRelease) {
        if (fingerprint == null || newRelease == null) return fingerprint;

        int colon = fingerprint.indexOf(':');
        if (colon < 0) return fingerprint;

        String prefix = fingerprint.substring(0, colon + 1);
        String rest = fingerprint.substring(colon + 1); // release/id/...
        String[] parts = rest.split("/", 3);
        if (parts.length < 2) return fingerprint;

        return prefix + newRelease + "/" + parts[1] + (parts.length == 3 ? ("/" + parts[2]) : "");
    }

    // =================== 版本信息：从同一个 filePath 读取（替代内置 JSON） ===================
    public static FakeAndroidVersionInfo randomFakeAndroidVersionInfo(String filePath, String packageName) {
        List<FakeAndroidVersionInfo> versions = DeviceConfig.getAndroidVersions(filePath, packageName);
        if (versions == null || versions.isEmpty()) {
            return new FakeAndroidVersionInfo();
        }

        FakeAndroidVersionInfo picked = RandomUtils.getRandomStringFromList(versions);
        FakeAndroidVersionInfo versionInfo = deepCopyVersion(picked);

        // 你原来的扰动逻辑：保留
        versionInfo.incremental = CommonFakeInfo.addRandomNumber(versionInfo.incremental);
        versionInfo.securityPatch = CommonFakeInfo.addRandomDays(versionInfo.securityPatch);
        return versionInfo;
    }

    // =================== toString：保持不动 ===================
    @Override
    public String toString() {
        return "FakeDeviceInfo{" +
                "product='" + product + '\'' +
                ", fingerprint='" + fingerprint + '\'' +
                ", id='" + id + '\'' +
                ", hardware='" + hardware + '\'' +
                ", tags='" + tags + '\'' +
                ", brand='" + brand + '\'' +
                ", model='" + model + '\'' +
                ", type='" + type + '\'' +
                ", display='" + display + '\'' +
                ", fakeAndroidVersionInfo=" + fakeAndroidVersionInfo +
                '}';
    }

    // =================== Version class：保持你的字段/Getter ===================
    public static class FakeAndroidVersionInfo extends CommonFakeInfo {
        private int sdkInt;
        private String[] activeCodenames;
        private String previewSdkFingerprint;
        private int firstSdkInt;

        private int resourcesSdkInt;
        private String securityPatch;
        private String baseOs;
        private int previewSdkInt;
        private String release;
        private String sdk;
        private String codename;
        private String incremental;

        public int getSdkInt() { return sdkInt; }
        public String[] getActiveCodenames() { return activeCodenames; }
        public String getPreviewSdkFingerprint() { return previewSdkFingerprint; }
        public int getFirstSdkInt() { return firstSdkInt; }
        public int getResourcesSdkInt() { return resourcesSdkInt; }
        public String getSecurityPatch() { return securityPatch; }
        public String getBaseOs() { return baseOs; }
        public int getPreviewSdkInt() { return previewSdkInt; }
        public String getRelease() { return release; }
        public String getSdk() { return sdk; }
        public String getCodename() { return codename; }
        public String getIncremental() { return incremental; }

        @Override
        public String toString() {
            return "FakeAndroidVersionInfo{" +
                    "sdkInt=" + sdkInt +
                    ", activeCodenames=" + Arrays.toString(activeCodenames) +
                    ", previewSdkFingerprint='" + previewSdkFingerprint + '\'' +
                    ", firstSdkInt=" + firstSdkInt +
                    ", resourcesSdkInt=" + resourcesSdkInt +
                    ", securityPatch='" + securityPatch + '\'' +
                    ", baseOs='" + baseOs + '\'' +
                    ", previewSdkInt=" + previewSdkInt +
                    ", release='" + release + '\'' +
                    ", sdk='" + sdk + '\'' +
                    ", codename='" + codename + '\'' +
                    ", incremental='" + incremental + '\'' +
                    '}';
        }
    }
}

package com.demo.java.xposed.app.instagram;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.provider.Settings;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;
import com.demo.java.xposed.utils.FileUtils;
import com.demo.java.xposed.utils.LogUtils ;
import com.demo.java.xposed.utils.MD5 ;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Locale;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;



/**
 * 提取ins的所有token
 */
public class InsTokenPlus {

    private static final String TAG = "INS_TOKEN_PLUS";

    /**
     * 提取整个INS的设备信息.
     * @return
     */
    public static JSONObject getInsData(Context context) {
        InsDeviceData deviceData = new InsDeviceData();

        // ins的token
        JSONObject tokensAsJson = getTokensAsJson(context);
        if(tokensAsJson==null){
            return null;
        }
//        Log.i(TAG, "tokensAsJson-> " + tokensAsJson.toString());

//        // convertToSimpleFormat 替换成系统 JSONObject 解析
        try {
            JSONObject jsonObject = convertToSimpleFormatSystem(tokensAsJson.toString());
            Log.i(TAG, "jsonObject-> " + jsonObject.toString());
            deviceData.authorizationStr = jsonObject.optString("authorization");
            deviceData.xIgWwwClaim = jsonObject.optString("claim");
            deviceData.xMid = jsonObject.optString("machine_id");
            deviceData.uid = jsonObject.optString("user_id");

        } catch (Exception ignore) {
        }

        try {
            String androidId = getAndroidId(context);
            deviceData.androidId = androidId;
        } catch (Exception ignore) {
        }

        String locale = getLocale();
        deviceData.appLocale = locale;

        try {
            String familyDeviceId = getFamilyDeviceId(context);
            deviceData.xIgFamilyDeviceId = familyDeviceId;
        } catch (Exception ignore) {
        }

        try {
            String deviceId = getDeviceId(context);
            deviceData.xIgDeviceId = deviceId;
        } catch (Exception ignore) {
        }

        try {
            String userAgent = getUserAgent(context);
            deviceData.userAgentStr = userAgent;
        } catch (Exception ignore) {
        }

        try {
            String mqttStr = getMqttStr(context);
            deviceData.mqttIds = mqttStr;
        } catch (Exception ignore) {
        }

        // 使用系统 JSONObject 将 deviceData 转成 JSON 字符串
        JSONObject result = new JSONObject();
        try {
            result.put("access_token", deviceData.authorizationStr);
            result.put("mid", deviceData.xMid);
            result.put("uid", deviceData.uid);
            result.put("android_id", deviceData.androidId);
            result.put("appLocale", deviceData.appLocale);
            result.put("family_device_id", deviceData.xIgFamilyDeviceId);
            result.put("device_id", deviceData.xIgDeviceId);
            result.put("user_agent", deviceData.userAgentStr);
            result.put("mqttIds", deviceData.mqttIds);

            result = mergeJsonObjects(result, tokensAsJson);
        } catch (Exception ignore) {
        }
//        Log.i(TAG, "resultJson-> " + result.toString());
        return result;
    }

    public static JSONObject mergeJsonObjects(JSONObject target, JSONObject source) {
        try {
            Iterator<String> keys = source.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                target.put(key, source.get(key));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return target;
    }

    /**
     *
     * @param context
     * @return
     */
    private static String getMqttStr(Context context) {
        SharedPreferences analyticsprefs = context.getSharedPreferences("rti.mqtt.ids", Context.MODE_PRIVATE);
        String mqtt_device_secret = analyticsprefs.getString("/settings/mqtt/id/mqtt_device_secret", null);
        Long timestamp = analyticsprefs.getLong("/settings/mqtt/id/timestamp", 0L);
        String connection_key = analyticsprefs.getString("/settings/mqtt/id/connection_key", "");
        String mqtt_device_id = analyticsprefs.getString("/settings/mqtt/id/mqtt_device_id", "");
        String connection_secret = analyticsprefs.getString("/settings/mqtt/id/connection_secret", "");

        //
        SharedPreferences token_store = context.getSharedPreferences("rti.mqtt.token_store", Context.MODE_PRIVATE);
        String mqtt_connection_key = token_store.getString("token_key", "{}");

        SharedPreferences registrations = context.getSharedPreferences("rti.mqtt.registrations", Context.MODE_PRIVATE);
        String registrationsStr = registrations.getString("com.instagram.android", "{}");

        JSONObject jsonObject = new JSONObject();
        JSONObject ids_json = new JSONObject();
        JSONObject token_storeJson = new JSONObject();
        JSONObject registrationsJson = new JSONObject();

        try {
            // rti.mqtt.token_store
            token_storeJson.putOpt("token_key", mqtt_connection_key);

            ids_json.putOpt("/settings/mqtt/id/mqtt_device_secret", mqtt_device_secret);
            ids_json.putOpt("/settings/mqtt/id/timestamp", timestamp);
            ids_json.putOpt("/settings/mqtt/id/connection_key", connection_key);
            ids_json.putOpt("/settings/mqtt/id/mqtt_device_id", mqtt_device_id);
            ids_json.putOpt("/settings/mqtt/id/connection_secret", connection_secret);

            registrationsJson.putOpt("com.instagram.android", registrationsStr);

            jsonObject.putOpt("rti.mqtt.ids", ids_json);
            jsonObject.putOpt("rti.mqtt.token_store", token_storeJson);
            jsonObject.putOpt("rti.mqtt.registrations", registrationsJson);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        return jsonObject.toString();
    }

    /**
     * 获取x-ig-device-id
     *
     * @param context
     * @return
     */
    private static String getDeviceId(Context context) {        //
        File file = new File(context.getFilesDir(), "INSTALLATION");
        String s1 = FileUtils.readString(file, StandardCharsets.UTF_8);
        return s1;
    }

    private static String getBuildId(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("msys-preferences", Context.MODE_PRIVATE);

        for (String key : prefs.getAll().keySet()) {
            if (key.contains("app_build_number")) {
                return  prefs.getString(key,"");
            }
        }
        return "";
    }

    private static String getAppVersion(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("msys-preferences", Context.MODE_PRIVATE);

        for (String key : prefs.getAll().keySet()) {
            if (key.contains("db_app_version")) {
                return  prefs.getString(key,"");
            }
        }
        return "";
    }

    private static String getFamilyDeviceId(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("analyticsprefs", Context.MODE_PRIVATE);
        String string = prefs.getString("analytics_device_id", null);
        return string;
    }

    private static String getLocale() {
        return Locale.getDefault().toString();
    }

    @SuppressLint("HardwareIds")
    private static String getAndroidId(Context context) {
        return Settings.Secure.getString(context.getContentResolver(), "android_id");
    }

    public static JSONObject getTokensAsJson(Context context) {
        JSONObject jsonObject = new JSONObject();

        try {
            // 初始化 Token 实例
            Token authHeaderPrefsToken = Token.initToken(context, "AuthHeaderPrefs", "AuthHeaderPrefs_single");
//            LogUtil.d(TAG, "authHeaderPrefsToken-> " + authHeaderPrefsToken);
            String authHeaderPrefsTokenDecrypt = authHeaderPrefsToken.getDecrypt();
//            LogUtil.d(TAG, "authHeaderPrefsTokenDecrypt-> " + authHeaderPrefsTokenDecrypt);

            // 这个可以解析userId
            Token wwwClaimHeaderPrefs = Token.initToken(context, "WwwClaimHeaderPrefs", "WwwClaimHeaderPrefs_single");
//            LogUtil.d(TAG, "wwwClaimHeaderPrefs-> " + authHeaderPrefsToken);
            String wwwClaimHeaderPrefsDecrypt = wwwClaimHeaderPrefs.getDecrypt();
//            LogUtil.d(TAG, "wwwClaimHeaderPrefsDecrypt-> " + wwwClaimHeaderPrefsDecrypt);

            Token routingHeaderPrefs = null;
            JSONArray jsonArray = new JSONArray(wwwClaimHeaderPrefsDecrypt);
            if (jsonArray.length() > 0) {
                JSONObject jsonObject1 = jsonArray.getJSONObject(0);
                String n = jsonObject1.getString("n");
                routingHeaderPrefs = Token.initToken(context, "RoutingHeaderPrefs_" + n, "RoutingHeaderPrefs_" + n + "_single");
                String routingHeaderPrefsRes = routingHeaderPrefs.getDecrypt();
//                LogUtil.d(TAG, "InsUtils: routingHeaderPrefsRes result -> " + routingHeaderPrefsRes);
            }

            // 将解密结果放入 JSON 对象
            jsonObject.put("authHeaderPrefsToken", new JSONArray(authHeaderPrefsTokenDecrypt));
            jsonObject.put("wwwClaimHeaderPrefsDecrypt", new JSONArray(wwwClaimHeaderPrefsDecrypt));
            jsonObject.put("routingHeaderPrefsRes", routingHeaderPrefs != null ? new JSONArray(routingHeaderPrefs.getDecrypt()) : null);

            String md5 = MD5.encrypt(jsonObject.toString());
            jsonObject.put("md5",md5);

            return jsonObject;
        } catch (JSONException e) {
            LogUtils.e(e.getMessage());
        }catch (Exception e){
        }
        return null;
    }

    static class Token {
        private String filePath;
        private String name;

        private String keyStoreName;

        private String iv;

        private SharedPreferences sharedPreferences;

        public String getFilePath() {
            return filePath;
        }

        public void setFilePath(String filePath) {
            this.filePath = filePath;
        }

        public String getName() {
            return name;
        }

        public String getKeyStoreName() {
            return keyStoreName;
        }

        public void setKeyStoreName(String keyStoreName) {
            this.keyStoreName = keyStoreName;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getIv() {
            return iv;
        }

        public void setIv(String iv) {
            this.iv = iv;
        }

        public SharedPreferences getSharedPreferences() {
            return sharedPreferences;
        }

        public void setSharedPreferences(SharedPreferences sharedPreferences) {
            this.sharedPreferences = sharedPreferences;
        }

        @Override
        public String toString() {
            return "Token{" +
                    "filePath='" + filePath + '\'' +
                    ", name='" + name + '\'' +
                    ", keyStoreName='" + keyStoreName + '\'' +
                    ", iv='" + iv + '\'' +
                    ", sharedPreferences=" + sharedPreferences +
                    '}';
        }

        public static Token initToken(Context context, String tokenName, String keyStoreName) {
            Token token = new Token();
            token.setName(tokenName);
            token.setKeyStoreName(keyStoreName);
            token.setFilePath("/data/data/com.instagram.android/files/single/" + tokenName);
            token.setSharedPreferences(context.getSharedPreferences(tokenName, 0));
            token.setIv(token.getSharedPreferences().getString(token.getName(), null));
            return token;
        }

        private String getDecrypt() {
            GCMParameterSpec gCMParameterSpec;
            try {
                SecretKey secretKey = getSecretKey();
                gCMParameterSpec = new GCMParameterSpec(128, Base64.decode(iv, 0));
                Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
                cipher.init(2, secretKey, gCMParameterSpec);
                CipherInputStream cipherInputStream = new CipherInputStream(new BufferedInputStream(Files.newInputStream(Paths.get(filePath))), cipher);
                return convertInputStreamToString(cipherInputStream);
            } catch (Exception ex) {
                Log.e(TAG, "getDecrypt: error" + ex.getMessage(), ex);
            }
            return null;
        }

        private SecretKey getSecretKey() {
            try {
                KeyStore ks = KeyStore.getInstance("AndroidKeyStore");
                ks.load(null);

                // 尝试获取现有的密钥
                if (ks.containsAlias(keyStoreName)) {
                    KeyStore.Entry entry = ks.getEntry(keyStoreName, null);
                    if (entry instanceof KeyStore.SecretKeyEntry) {
                        return ((KeyStore.SecretKeyEntry) entry).getSecretKey();
                    }
                }

                // 如果密钥不存在，创建一个新的
                KeyGenerator keyGenerator = KeyGenerator.getInstance("AES", "AndroidKeyStore");
                KeyGenParameterSpec.Builder builder = new KeyGenParameterSpec.Builder(
                        keyStoreName,
                        KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                        .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                        .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                        .setKeySize(256)
                        .setRandomizedEncryptionRequired(true);

                keyGenerator.init(builder.build());
                SecretKey secretKey = keyGenerator.generateKey();
//                LogUtil.d(TAG, "Created new secret key for: " + keyStoreName);
                return secretKey;

            } catch (Exception ex) {
//                Log.e(TAG, "Error getting/creating secret key for " + keyStoreName, ex);
                return null;
            }
        }

    }

    public static String convertInputStreamToString(InputStream inputStream) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
        StringBuilder stringBuilder = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            stringBuilder.append(line).append("\n");
        }
        reader.close();
        return stringBuilder.toString();
    }


    public static final String getUserAgent(Context context) {
        String str;
        String res="";
        String A04 = A04(context);
        String screenInfo = getFullScreenInfo(context);
        String obj = Locale.getDefault().toString();
        try {
            try {
                String str2 = Build.MANUFACTURER;
                String str3 = Build.BRAND;
                if (!A0K(str2, str3)) {
                    str2 = A05("%s/%s", str2, str3);
                }
//                res = A05("%s/%s; %s; %s; %s; %s; %s; %s", Integer.valueOf(Build.VERSION.SDK_INT), Build.VERSION.RELEASE, A00, str2, Build.MODEL, Build.DEVICE, Build.HARDWARE, obj);
                res ="Instagram %s Android (%s/%s; %s; %s; %s; %s; %s; %s; %s)".formatted(getAppVersion(context), Integer.valueOf(Build.VERSION.SDK_INT), Build.VERSION.RELEASE, screenInfo, str2, Build.MODEL, Build.DEVICE, Build.HARDWARE, obj,getBuildId(context));
            } catch (Exception unused) {
                LogUtils.show(String.valueOf(unused));
            }
        } catch (Exception unused2) {
            res = "";
        }
//        return A05("%s %s Android %s", "Instagram", A04, str);
        return res;
    }



    public static final String A04(Context context) {
        try {
            String str2 = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
            return str2;
        } catch (PackageManager.NameNotFoundException e) {
            throw new RuntimeException(e);
        }
    }


    public static String getFullScreenInfo(Context context) {
        DisplayMetrics dm = new DisplayMetrics();

        WindowManager wm =
                (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);

        if (wm != null) {
            Display display = wm.getDefaultDisplay();
            display.getRealMetrics(dm);   // 关键：realMetrics
        } else {
            // 兜底方案（极少发生）
            dm = context.getResources().getDisplayMetrics();
        }

        int width = Math.min(dm.widthPixels, dm.heightPixels);
        int height = Math.max(dm.widthPixels, dm.heightPixels);

        return dm.densityDpi + "dpi; " + width + "x" + height;
    }


    public static final String A05(String str, Object... objArr) {
        Object[] copyOf = Arrays.copyOf(objArr, objArr.length);
        String format = String.format(null, str, Arrays.copyOf(copyOf, copyOf.length));
        return format;
    }

    public static boolean A0K(Object obj, Object obj2) {
        return obj == null ? obj2 == null : obj.equals(obj2);
    }


    public static class InsDeviceData {

        private String uid;

        //
        private String androidId;

        // x-ig-family-device-id
        private String xIgFamilyDeviceId;
        // x-ig-device-id
        private String xIgDeviceId;
        // Z_zZQgABAAGvUaU4pHHVY7ViFt1s
        private String xMid;
        // x-ig-www-claim, hmac.AR36xkvFkixSlvqG-ijvHv9IHTSgbYH7h9fWaFSimHGuyj1B
        private String xIgWwwClaim;
        private String authorizationStr;

        // 用户的useragent
        // (29/10; 550dpi; 1440x2729; samsung/Samsung; Galaxy Z Fold 4; galaxyzfold4; helio g95; lo_LA; 699226552)
        private String userAgentStr;

        // en_us
        private String appLocale;

        // 安卓设备的token
        private Long androidInfoId;

        // mqtt的具体信息
        // <map>
        //    <string name="/settings/mqtt/id/mqtt_device_secret">qo1sJYcezG9txcf5HHLiUTzhVRo=</string>
        //    <long name="/settings/mqtt/id/timestamp" value="1746518248222" />
        //    <string name="/settings/mqtt/id/connection_key">451364089443885</string>
        //    <string name="/settings/mqtt/id/connection_secret">JN+S1KY6SW6HSuOVzNtnUbSL0pI=</string>
        //    <string name="/settings/mqtt/id/mqtt_device_id">8bc4c3e0-2a95-4c49-aa80-7e36183a119a</string>
        //</map>
        private String mqttIds;


        //

        public InsDeviceData() {
        }
    }

    public static JSONObject convertToSimpleFormatSystem(String internalJson) {
        try {
            JSONObject internal = new JSONObject(internalJson);
            JSONObject result = new JSONObject();

            // 处理 authHeaderPrefsToken
            if (internal.has("authHeaderPrefsToken")) {
                JSONArray authArray = internal.optJSONArray("authHeaderPrefsToken");
                if (authArray != null) {
                    for (int i = 0; i < authArray.length(); i++) {
                        JSONObject obj = authArray.optJSONObject(i);
                        if (obj == null) continue;

                        String name = obj.optString("n");
                        String value = obj.optString("v");

                        if ("DEVICE_HEADER_ID".equals(name)) {
                            result.put("machine_id", value);
                        } else {
                            result.put("authorization", value);
                            result.put("user_id", name);
                        }
                    }
                }
            }

            // 处理 wwwClaimHeaderPrefs
            if (internal.has("wwwClaimHeaderPrefs")) {
                JSONArray claimArray = internal.optJSONArray("wwwClaimHeaderPrefs");
                if (claimArray != null && claimArray.length() > 0) {
                    JSONObject claim = claimArray.optJSONObject(0);
                    if (claim != null) {
                        result.put("claim", claim.optString("v"));
                    }
                }
            }

            return result;
        } catch (Exception e) {
            Log.e(TAG, "Error converting to simple format: " + e.getMessage(), e);
            return null;
        }
    }
}

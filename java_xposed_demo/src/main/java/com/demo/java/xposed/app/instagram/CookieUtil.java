package com.demo.java.xposed.app.instagram;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.Base64;
public class CookieUtil {

    public static List<Map<String, Object>> transferCookie(String rawCookieStr) throws JSONException {
        JSONObject jsonCookie = new JSONObject(rawCookieStr);

        if (!jsonCookie.has("access_token") || jsonCookie.isNull("access_token")) {
            System.out.println("cookie 格式不支持");
            return Collections.emptyList();
        }

        List<Map<String, Object>> cookies = new ArrayList<>();

        // ds_user_id
        Map<String, Object> dsUserId = new HashMap<>();
        dsUserId.put("domain", ".instagram.com");
        dsUserId.put("name", "ds_user_id");
        dsUserId.put("path", "/");
        dsUserId.put("sameSite", "no_restriction");
        dsUserId.put("value", jsonCookie.optString("uid"));
        cookies.add(dsUserId);

        // ig_did
        Map<String, Object> igDid = new HashMap<>();
        igDid.put("domain", ".instagram.com");
        igDid.put("name", "ig_did");
        igDid.put("value", jsonCookie.optString("device_id"));
        cookies.add(igDid);

        // mid
        Map<String, Object> mid = new HashMap<>();
        mid.put("domain", ".instagram.com");
        mid.put("name", "mid");
        mid.put("path", "/");
        mid.put("value", jsonCookie.optString("mid"));
        cookies.add(mid);

        // sessionid
        String accessToken = jsonCookie.optString("access_token");
        System.out.println("accessToken:" +accessToken);
        String sessionid = extractSessionIdFromAccessToken(accessToken);

        Map<String, Object> sessionId = new HashMap<>();
        sessionId.put("domain", ".instagram.com");
        sessionId.put("name", "sessionid");
        sessionId.put("path", "/");
        sessionId.put("value", sessionid);
        cookies.add(sessionId);

        return cookies;
    }


    private static String extractSessionIdFromAccessToken(String accessTokenRaw) throws JSONException {
        if (accessTokenRaw == null) return "";

        // 1) 去掉 Bearer 前缀 & trim
        String token = accessTokenRaw.trim();
        if (token.regionMatches(true, 0, "Bearer ", 0, "Bearer ".length())) {
            token = token.substring("Bearer ".length()).trim();
        }

        // 2) 取最后一段 base64（IGT:2:<payload>）
        String[] parts = token.split(":");
        String b64 = parts[parts.length - 1].trim();

        // 3) 自动补齐 padding（base64 长度必须是 4 的倍数）
        b64 = padBase64(b64);

        // 4) 先用标准 Base64 解；不行再用 URL-safe 解（兼容两种情况）
        byte[] decoded;
        try {
            decoded = Base64.getDecoder().decode(b64);
        } catch (IllegalArgumentException e) {
            decoded = Base64.getUrlDecoder().decode(b64);
        }

        String decodedJson = new String(decoded, StandardCharsets.UTF_8);
        JSONObject obj = new JSONObject(decodedJson);
        return obj.optString("sessionid", "");
    }

    private static String padBase64(String s) {
        // 去掉空白
        s = s.replaceAll("\\s+", "");
        int mod = s.length() % 4;
        if (mod == 0) return s;
        return s + "====".substring(mod); // mod=1补3个=，mod=2补2个=，mod=3补1个=
    }

    public static void main(String[] args) {
        try {
            List<Map<String, Object>> result= CookieUtil.transferCookie("{\"device_id\":\"50623114-6663-4b5e-a217-b67e42256375\",\"authHeaderPrefsToken\":[{\"t\":\"string\",\"v\":\"Bearer IGT:2:eyJkc191c2VyX2lkIjoiNzgxMzQ0MDM0MjAiLCJzZXNzaW9uaWQiOiI3ODEzNDQwMzQyMCUzQTBjRUhOQU5Xc2VtclR2JTNBMjklM0FBWWpIZnl5LThya1c3QXp4blQ0UlpBOGZTcUVpV3VsTmNLTmtWdW1aTEEifQ==\",\"n\":\"78134403420\"},{\"t\":\"string\",\"v\":\"aVVzpwABAAGQg857Qk7R71JgnXbQ\",\"n\":\"DEVICE_HEADER_ID\"}],\"mqttIds\":\"{\\\"rti.mqtt.ids\\\":{\\\"\\\\/settings\\\\/mqtt\\\\/id\\\\/mqtt_device_secret\\\":\\\"79QmOudkqpoBYjCepTlWJEhJpfI=\\\",\\\"\\\\/settings\\\\/mqtt\\\\/id\\\\/timestamp\\\":1767207849276,\\\"\\\\/settings\\\\/mqtt\\\\/id\\\\/connection_key\\\":\\\"772799119963571\\\",\\\"\\\\/settings\\\\/mqtt\\\\/id\\\\/mqtt_device_id\\\":\\\"4afa114b-33f6-4f09-a630-46ece0905db1\\\",\\\"\\\\/settings\\\\/mqtt\\\\/id\\\\/connection_secret\\\":\\\"w6\\\\/ffV2VUOwMb0DtkE0rFhttBBU=\\\"},\\\"rti.mqtt.token_store\\\":{\\\"token_key\\\":\\\"{\\\\\\\"k\\\\\\\":\\\\\\\"eyJjayI6Ijc3Mjc5OTExOTk2MzU3MSIsImFpIjo1NjczMTAyMDM0MTUwNTIsImRpIjoiNGFmYTExNGItMzNmNi00ZjA5LWE2MzAtNDZlY2UwOTA1ZGIxIiwicG4iOiJjb20uaW5zdGFncmFtLmFuZHJvaWQifQ==\\\\\\\",\\\\\\\"v\\\\\\\":0,\\\\\\\"t\\\\\\\":\\\\\\\"fbns-b64\\\\\\\"}\\\"},\\\"rti.mqtt.registrations\\\":{\\\"com.instagram.android\\\":\\\"{\\\\\\\"app_id\\\\\\\":\\\\\\\"567067343352427\\\\\\\",\\\\\\\"pkg_name\\\\\\\":\\\\\\\"com.instagram.android\\\\\\\",\\\\\\\"token\\\\\\\":\\\\\\\"{\\\\\\\\\\\\\\\"k\\\\\\\\\\\\\\\":\\\\\\\\\\\\\\\"eyJjayI6Ijc3Mjc5OTExOTk2MzU3MSIsImFpIjo1NjczMTAyMDM0MTUwNTIsImRpIjoiNGFmYTExNGItMzNmNi00ZjA5LWE2MzAtNDZlY2UwOTA1ZGIxIiwicG4iOiJjb20uaW5zdGFncmFtLmFuZHJvaWQifQ==\\\\\\\\\\\\\\\",\\\\\\\\\\\\\\\"v\\\\\\\\\\\\\\\":0,\\\\\\\\\\\\\\\"t\\\\\\\\\\\\\\\":\\\\\\\\\\\\\\\"fbns-b64\\\\\\\\\\\\\\\"}\\\\\\\",\\\\\\\"time\\\\\\\":1769161390775,\\\\\\\"invalid\\\\\\\":false}\\\"}}\",\"mid\":\"aVVzpwABAAGQg857Qk7R71JgnXbQ\",\"wwwClaimHeaderPrefsDecrypt\":[{\"t\":\"string\",\"v\":\"hmac.AR3p79Htk9foBbc43I3K0P3cS5wybM2tpFbsibFmquXqxl0I\",\"n\":\"78134403420\"}],\"access_token\":\"Bearer IGT:2:eyJkc191c2VyX2lkIjoiNzgxMzQ0MDM0MjAiLCJzZXNzaW9uaWQiOiI3ODEzNDQwMzQyMCUzQTBjRUhOQU5Xc2VtclR2JTNBMjklM0FBWWpIZnl5LThya1c3QXp4blQ0UlpBOGZTcUVpV3VsTmNLTmtWdW1aTEEifQ==\",\"uid\":\"78134403420\",\"family_device_id\":\"a882bf98-279f-497a-919d-a609ec230222\",\"android_id\":\"9828b23400bce64b\",\"routingHeaderPrefsRes\":[{\"t\":\"string\",\"v\":\"RVA\",\"n\":\"X-MSGR-Region\"},{\"t\":\"string\",\"v\":\"ATN,73327795761,1778288318:01f716e52dd6ad41545b4eecb74df95065b183a4afe275dd83c0617d14f09a53f0826b32\",\"n\":\"IG-U-IG-DIRECT-REGION-HINT\"},{\"t\":\"string\",\"v\":\"78134403420\",\"n\":\"IG-U-DS-USER-ID\"}],\"appLocale\":\"en_US\",\"user_agent\":\"Instagram 410.1.0.63.71 Android (33/13; 320dpi; 720x1600; KXD; 13C; 13C; mt6765; en_US; 846519237)\",\"md5\":\"190dac625e38667c21b874909733fbbe\"}");
            String jsonStr = new JSONArray(result).toString();
            System.out.println(jsonStr);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }
}

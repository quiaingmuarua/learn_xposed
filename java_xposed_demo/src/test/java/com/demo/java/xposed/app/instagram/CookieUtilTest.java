package com.demo.java.xposed.app.instagram;


import com.google.gson.Gson;

import org.json.JSONException;
import org.junit.Test;

import java.util.List;
import java.util.Map;

public class CookieUtilTest {

    @Test
    public void transferCookie() {

        try {
            List<Map<String, Object>> result= CookieUtil.transferCookie("{\"xIgDeviceId\":\"59dbac1d-d663-4fdf-ace6-28419477c4a7\",\"authHeaderPrefsToken\":[{\"t\":\"string\",\"v\":\"aKx3gwABAAHI-BrbX_yI-XOy_yMS\",\"n\":\"DEVICE_HEADER_ID\"},{\"t\":\"string\",\"v\":\"Bearer IGT:2:eyJkc191c2VyX2lkIjoiNzY1MjM3NTQ0OTMiLCJzZXNzaW9uaWQiOiI3NjUyMzc1NDQ5MyUzQUZhY05hYTFiSFphMW9iJTNBNiUzQUFZZFhwWU1YUFZXb0EwZGZZLU4zekk1aUpGZDZJOFRRX3JjVmtkZkV6USJ9\",\"n\":\"76523754493\"}],\"mqttIds\":\"{\\\"rti.mqtt.ids\\\":{\\\"\\\\/settings\\\\/mqtt\\\\/id\\\\/mqtt_device_secret\\\":\\\"2XI30aMptpuvpwfOEwQV4D8p7Zc=\\\",\\\"\\\\/settings\\\\/mqtt\\\\/id\\\\/timestamp\\\":1756225050869,\\\"\\\\/settings\\\\/mqtt\\\\/id\\\\/connection_key\\\":\\\"439340039050939\\\",\\\"\\\\/settings\\\\/mqtt\\\\/id\\\\/mqtt_device_id\\\":\\\"d5af2a2c-79ed-4abb-9a68-bbc213aca59d\\\",\\\"\\\\/settings\\\\/mqtt\\\\/id\\\\/connection_secret\\\":\\\"z3BQR4AVMrW+CHHo7dKqsXzD2Gs=\\\"},\\\"rti.mqtt.token_store\\\":{\\\"token_key\\\":\\\"{\\\\\\\"k\\\\\\\":\\\\\\\"eyJjayI6IjQzOTM0MDAzOTA1MDkzOSIsImFpIjo1NjczMTAyMDM0MTUwNTIsImRpIjoiZDVhZjJhMmMtNzllZC00YWJiLTlhNjgtYmJjMjEzYWNhNTlkIiwicG4iOiJjb20uaW5zdGFncmFtLmFuZHJvaWQifQ==\\\\\\\",\\\\\\\"v\\\\\\\":0,\\\\\\\"t\\\\\\\":\\\\\\\"fbns-b64\\\\\\\"}\\\"},\\\"rti.mqtt.registrations\\\":{\\\"com.instagram.android\\\":\\\"{\\\\\\\"app_id\\\\\\\":\\\\\\\"567067343352427\\\\\\\",\\\\\\\"pkg_name\\\\\\\":\\\\\\\"com.instagram.android\\\\\\\",\\\\\\\"token\\\\\\\":\\\\\\\"\\\\\\\",\\\\\\\"time\\\\\\\":1769585225436,\\\\\\\"invalid\\\\\\\":false}\\\"}}\",\"wwwClaimHeaderPrefsDecrypt\":[{\"t\":\"string\",\"v\":\"hmac.AR3Cp45WKcVauzl9IWx1HplzdiDUXgC7pojJCsTInZJ5BqjI\",\"n\":\"76523754493\"}],\"userAgentStr\":\"30/11; 320dpi; 1200x1904; MINDEO; MS8389; MS8389; mt6765; ar_YE_#u-nu-latn\",\"routingHeaderPrefsRes\":[{\"t\":\"string\",\"v\":\"NCG,76523754493,1787773057:01fe285d2a2cb51654cac7c78b15ac3308d84f6492aa546a4de8c7eaea9b647243747bed\",\"n\":\"IG-U-RUR\"},{\"t\":\"string\",\"v\":\"76523754493\",\"n\":\"IG-U-DS-USER-ID\"}],\"appLocale\":\"ar_YE_#u-nu-latn\",\"xIgFamilyDeviceId\":\"bb13f51d-ebbb-4b6e-9c9d-f3c1d43e4c5c\",\"androidId\":\"androidid-35c2a2c3ab9e76aa\",\"md5\":\"6fa6f732008cb964ae955363bdfe0d2d\"}");

            Gson gson=new Gson();
            System.out.printf( gson.toJson(result));
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

    }

}

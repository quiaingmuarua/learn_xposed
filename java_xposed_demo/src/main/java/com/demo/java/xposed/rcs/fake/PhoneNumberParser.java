package com.demo.java.xposed.rcs.fake;

import com.demo.java.xposed.utils.LogUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class PhoneNumberParser {

    private static OkHttpClient client = new OkHttpClient();
    private static final int MAX_RETRIES = 3;

    public static Map<String, String> getCarrierByPhoneNumber(String countryCode,String phoneNumber) throws IOException {
        LogUtils.show("getCarrierByPhoneNumber "+phoneNumber);
        String url = String.format("https://libphonenumber.appspot.com/phonenumberparser?number=%%2B%s%s", countryCode,phoneNumber);
        int attempts = 0;
        IOException lastException = null;

        while (attempts < MAX_RETRIES) {
            try {
                Request request = new Request.Builder()
                        .url(url)
                        .build();

                try (Response response = client.newCall(request).execute()) {
                    if (!response.isSuccessful()) {
                        throw new IOException("Unexpected code " + response);
                    }

                    String respText = response.body().string();

                    // 提取Carrier
                    String carrierName = extractData(respText, "Carrier</TH><TD>(.*?)<");

                    // 提取Region
                    String regionName = extractData(respText, "Phone Number region</TH><TD>(.*?)</TD>");

                    // 提取Country Code
//                    String countryCode = extractData(respText, "country_code</TH><TD>(.*?)</TD>");

                    // 创建并返回结果Map
                    Map<String, String> result = new HashMap<>();
                    result.put("carrier_name", carrierName);
                    result.put("country_code", countryCode);
                    result.put("region_name", regionName);

                    return result;
                }
            } catch (IOException e) {
                lastException = e;
                attempts++;
                if (attempts < MAX_RETRIES) {
                    System.out.println("Attempt " + attempts + " failed, retrying...");
                }
            }
        }

        // 如果所有重试都失败，则抛出最后一次的异常
        throw lastException;
    }

    private static String extractData(String text, String regex) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

}

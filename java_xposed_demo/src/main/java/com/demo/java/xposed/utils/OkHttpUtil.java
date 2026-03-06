package com.demo.java.xposed.utils;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.ConnectionPool;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class OkHttpUtil {

    // 不复用连接，避免复用脏连接导致 SSL 握手异常
    private static final OkHttpClient client = new OkHttpClient.Builder()
            .connectionPool(new ConnectionPool(0, 1, TimeUnit.NANOSECONDS)) // 禁止连接复用
            .hostnameVerifier((hostname, session) -> true) // 忽略主机名验证（注意：生产环境不推荐这么做）
            .connectTimeout(6, TimeUnit.SECONDS)
            .readTimeout(6, TimeUnit.SECONDS)
            .writeTimeout(6, TimeUnit.SECONDS)
            .build();

    /**
     * 发送同步POST请求
     * @param url 请求地址
     * @param json 请求体内容
     * @return 响应体字符串
     * @throws Exception 最终抛出最后一次异常
     */
    public static String sendPostRequestSync(String url, String json) throws Exception {
        LogUtils.show("sendPostRequestSync -> url: " + url);
        Exception lastException = new Exception("Unknown error during sendPostRequestSync");

        for (int attempt = 1; attempt <= 3; attempt++) {
            try {
                RequestBody body = RequestBody.create(json, MediaType.get("application/json; charset=utf-8"));
                Request request = new Request.Builder()
                        .url(url)
                        .post(body)
                        .header("Connection", "close") // 请求头上也明确关闭连接
                        .build();

                try (Response response = client.newCall(request).execute()) {
                    if (!response.isSuccessful()) {
                        throw new IOException("Unexpected HTTP code: " + response.code() + ", message: " + response.message());
                    }
                    String responseBody = response.body().string();
                    LogUtils.show("sendPostRequestSync -> success on attempt " + attempt);
                    return responseBody;
                }
            } catch (Exception e) {
                LogUtils.printStackErrInfo("sendPostRequestSync -> attempt " + attempt + " failed", e);
                lastException = e;
                Thread.sleep(1000); // 1秒再试
            }

        }

        throw lastException;
    }

    public static void main(String[] args) {
        String url = "https://api.example.com/data";
        String json = "{\"name\": \"John\", \"age\": 30}";

        try {
            String response = sendPostRequestSync(url, json);
            System.out.println("Response: " + response);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

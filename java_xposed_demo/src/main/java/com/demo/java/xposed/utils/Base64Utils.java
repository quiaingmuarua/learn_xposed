package com.demo.java.xposed.utils;

import android.util.Base64;

public class Base64Utils {

    /**
     * 安全解码 Base64 字符串，自动清理非法字符、补齐 padding，避免抛出异常
     * @param input 原始 Base64 字符串，可能包含非法字符或缺少 padding
     * @return 解码后的字节数组，失败则返回空数组
     */
    public static byte[] safeDecodeBase64(String input) {
        if (input == null || input.isEmpty()) {
            return new byte[0];
        }

        // 只保留合法字符：Base64 标准字符 + padding =
        String cleaned = input.replaceAll("[^A-Za-z0-9+/=]", "");

        // 补齐 padding，Base64 长度必须是4的倍数
        int paddingNeeded = (4 - (cleaned.length() % 4)) % 4;
        for (int i = 0; i < paddingNeeded; i++) {
            cleaned += "=";
        }

        try {
            return Base64.decode(cleaned, Base64.DEFAULT);
        } catch (IllegalArgumentException e) {
            // 如果还是异常，返回空数组
            return new byte[0];
        }
    }

    /**
     * 解码并转换为 UTF-8 字符串
     */
    public static String safeDecodeBase64ToString(String input) {
        byte[] decoded = safeDecodeBase64(input);
        return new String(decoded, java.nio.charset.StandardCharsets.UTF_8);
    }
}

package com.demo.java.xposed.utils;

public class Base62Util {

    private static final String BASE62_CHARS = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";

    /**
     * 将10进制数字编码为62进制字符串
     *
     * @param num 原始数字
     * @return 62进制字符串
     */
    public static String encodeBase62(long num) {
        if (num == 0) {
            return "0";
        }
        StringBuilder sb = new StringBuilder();
        while (num > 0) {
            int remainder = (int) (num % 62);
            sb.append(BASE62_CHARS.charAt(remainder));
            num /= 62;
        }
        return sb.reverse().toString();
    }

    /**
     * 将62进制字符串解码为10进制数字
     *
     * @param str 62进制字符串
     * @return 解码后的数字
     */
    public static long decodeBase62(String str) {
        long result = 0;
        for (int i = 0; i < str.length(); i++) {
            result = result * 62 + BASE62_CHARS.indexOf(str.charAt(i));
        }
        return result;
    }

    // 测试示例
    public static void main(String[] args) {
        String phone = "13580001234";
        long phoneNum = Long.parseLong(phone);


        // 编码
        String encoded = encodeBase62(phoneNum);
        System.out.println("62进制编码结果: " + encoded);

        // 解码
        long decodedNum = decodeBase62(encoded);
        System.out.println("解码后的手机号: " + decodedNum);
    }
}

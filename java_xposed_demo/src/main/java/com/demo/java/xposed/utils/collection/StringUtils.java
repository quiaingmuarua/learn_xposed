package com.demo.java.xposed.utils.collection;

public class StringUtils {

    /**
     * 将16进制字符串转换为byte[]
     *
     * @param hexString 16进制字符串
     * @return byte[]
     * @explain 16进制字符串不区分大小写，返回的数组相同
     */
    public static byte[] HexStringToBytes(String hexString) {
        if (hexString == null || hexString.trim().isEmpty()) {
            return new byte[0];
        }

        hexString = hexString.replaceAll(" ", "");

        if (hexString.length() % 2 != 0) {
            throw new IllegalArgumentException("Hex string must have even length");
        }

        int len = hexString.length();
        byte[] bytes = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            String hex = hexString.substring(i, i + 2);
            try {
                bytes[i / 2] = (byte) Integer.parseInt(hex, 16);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid hex character in input: " + hex, e);
            }
        }
        return bytes;
    }



    /**
     * 将byte[]转换为16进制字符串
     *
     * @param bytes byte[]
     * @return 16进制字符串
     */
    public static String bytesToHexString(byte[] bytes) {
        if (bytes == null || bytes.length == 0) return "";
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b & 0xFF)); // 小写
            // 如果你想要大写：%02X
        }
        return sb.toString();
    }



    public static String bytesToString(byte[] bytes){
        return new String(bytes);
    }

}

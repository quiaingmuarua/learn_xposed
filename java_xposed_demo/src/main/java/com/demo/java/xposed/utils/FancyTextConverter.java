package com.demo.java.xposed.utils;

public class FancyTextConverter {

    public static String toBoldSerif(String input) {
        StringBuilder sb = new StringBuilder();
        for (char ch : input.toCharArray()) {
            if (ch >= 'A' && ch <= 'Z') {
                // 大写字母粗体 Serif 从 U+1D400 开始
                sb.appendCodePoint(0x1D400 + (ch - 'A'));
            } else if (ch >= 'a' && ch <= 'z') {
                // 小写字母粗体 Serif 从 U+1D41A 开始
                sb.appendCodePoint(0x1D41A + (ch - 'a'));
            } else {
                // 非字母字符原样输出
                sb.append(ch);
            }
        }
        return sb.toString();
    }

    public static void main(String[] args) {
        String normal = "Your toll account balance is outstanding.";
        String fancy = toBoldSerif(normal);
        System.out.println(fancy);
    }
}

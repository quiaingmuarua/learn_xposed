package com.demo.java.xposed.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class ShellUtils {

    public static String getSystemProperty(String key, String default_value) {
        String value = default_value;
        try {
            // 执行 getprop 命令
            Process process = Runtime.getRuntime().exec("getprop " + key);
            BufferedReader bufferedReader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()));

            // 读取命令输出
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                value += line;
            }

            bufferedReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return value;
    }

    public static String getSystemProperty(String key){
        return getSystemProperty(key,"");

    }

    public static void main(String[] args) {
        // 获取自定义属性
        String myPropValue = getSystemProperty("my.custom.prop","");
        System.out.println("my.custom.prop: " + myPropValue);
    }
}

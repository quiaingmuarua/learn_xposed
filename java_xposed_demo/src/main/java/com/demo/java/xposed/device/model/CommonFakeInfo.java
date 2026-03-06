package com.demo.java.xposed.device.model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Random;

public class CommonFakeInfo {


    public static String addRandomDays(String dateString) {
        try {
            // 将日期字符串解析为 LocalDate 对象
            LocalDate originalDate = null;
            originalDate = LocalDate.parse(dateString, DateTimeFormatter.ISO_DATE);
            // 生成一个随机天数
            Random random = new Random();
            int randomDays = random.nextInt(100);  // 你可以根据需要调整范围

            // 将随机天数添加到原始日期
            LocalDate modifiedDate = null;
            modifiedDate = originalDate.plusDays(randomDays);

            // 将修改后的日期格式化为字符串
            return modifiedDate.format(DateTimeFormatter.ISO_DATE);

        } catch (Exception e) {
            // 处理解析异常
            System.err.println("无法解析日期字符串: " + e.getMessage());
            return dateString;  // 或者根据需要返回一个默认值
        }


    }


    public static String addRandomNumber(String numberString) {
        try {
            // 尝试将字符串数字转换为整数
            int originalNumber = Integer.parseInt(numberString);

            // 生成一个随机数
            Random random = new Random();
            int randomNumber = random.nextInt(1000);  // 你可以根据需要调整范围

            // 将随机数与原始数字相加
            return String.valueOf(originalNumber + randomNumber);
        } catch (NumberFormatException e) {
            // 处理转换异常
            System.err.println("无法将字符串转换为整数: " + e.getMessage());
            return numberString;  // 或者根据需要返回一个默认值
        }
    }
}

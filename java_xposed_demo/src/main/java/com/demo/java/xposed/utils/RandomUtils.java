package com.demo.java.xposed.utils;

import java.util.List;
import java.util.Random;

public class RandomUtils {

    // 获取当前进程ID作为随机种子
//    public static long targetProcessId = android.os.Process.myPid();

    public static <T> T getRandomStringFromList(List<T> stringList) {
        if (stringList == null || stringList.isEmpty()) {
            throw new IllegalArgumentException("字符串列表不能为空");
        }
        Random random = new Random();
        // 使用传入的 Random 对象生成随机索引
        int randomIndex = random.nextInt(stringList.size());
        // 返回随机选择的字符串
        return stringList.get(randomIndex);
    }



    private static int getRandomNumber(int min, int max) {
        Random random = new Random();
        return random.nextInt((max - min) + 1) + min;
    }


    public static String generateRandomNumber(Long seed, int length) {
        Random random = new Random(seed);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(random.nextInt(10)); // Append a random digit (0-9)
        }
        return sb.toString();
    }


    public static  String randomAlphabetic(int length){
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append((char) (random.nextInt(26) + 'a'));
        }
        return sb.toString();
    }
}

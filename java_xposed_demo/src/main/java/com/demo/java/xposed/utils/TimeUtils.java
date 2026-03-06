package com.demo.java.xposed.utils;

import android.os.SystemClock;

import java.util.Date;

public class TimeUtils {

    /**
     * 获取设备的开机时间
     *
     * @return Date对象，表示设备的开机时间
     */
    public static Date getBootTime() {
        // 获取当前时间的毫秒数
        long currentTime = System.currentTimeMillis();

        // 获取设备自开机以来的时间（毫秒）
        long elapsedTime = SystemClock.elapsedRealtime();

        // 计算开机时间
        long bootTime = currentTime - elapsedTime;

        // 将计算出的开机时间转换为 Date 对象
        return new Date(bootTime);
    }

    /**
     * 获取设备的开机时间的字符串表示形式
     *
     * @return String对象，表示设备的开机时间
     */
    public static String getBootTimeString() {
        Date bootTime = getBootTime();
        return bootTime.toString();
    }


    /**
     * 获取设备自开机以来的时间（秒）
     *
     * @return long值，表示设备自开机以来的秒数
     */
    public static long getUptimeSeconds() {
        // 获取设备自开机以来的时间（毫秒），并转换为秒
        return SystemClock.elapsedRealtime() / 1000;
    }
}

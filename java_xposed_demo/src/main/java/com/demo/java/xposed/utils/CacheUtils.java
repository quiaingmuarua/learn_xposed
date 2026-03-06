package com.demo.java.xposed.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.demo.java.xposed.rcs.model.MsgItem;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class CacheUtils {

    private static final String PREFS_NAME = "AppCachePreferences";
    private static final String KEY_LAST_LAUNCH_TIME = "LastLaunchTime";
    private static final String KEY_CACHE_CLEARED_TIME = "CacheClearedTime";
    private static final String KEY_MSG_ITEM_MAP = "msg_item_map";


    /**
     * 检查并记录应用的启动时间。
     * 如果发现启动时间戳丢失（可能是缓存被清除），记录当前时间为缓存清除时间。
     */
    public static void checkAndRecordLaunchTime(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        // 检查是否存在上次启动时间
        long lastLaunchTime = prefs.getLong(KEY_LAST_LAUNCH_TIME, -1);

        if (lastLaunchTime == -1) {
            // 如果上次启动时间不存在，说明缓存或数据被清除
            long currentTime = System.currentTimeMillis();

            // 记录缓存清除的时间
            recordCacheClearedTime(context, currentTime);
        }

        // 记录当前的启动时间
        prefs.edit().putLong(KEY_LAST_LAUNCH_TIME, System.currentTimeMillis()).apply();
    }

    /**
     * 记录缓存清除的时间
     */
    private static void recordCacheClearedTime(Context context, long time) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putLong(KEY_CACHE_CLEARED_TIME, time).apply();

        // 输出日志以显示缓存清除时间
        Log.d("CacheUtils", "App cache was cleared at: " + new Date(time).toString());
    }

    /**
     * 获取上次应用启动时间
     *
     * @param context 上下文对象
     * @return 上次应用启动时间的 Date 对象，如果没有记录则返回 null
     */
    public static String getLastLaunchTime(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        long lastLaunchTime = prefs.getLong(KEY_LAST_LAUNCH_TIME, -1);

        if (lastLaunchTime != -1) {
            return new Date(lastLaunchTime).getTime()/1000 + "";
        } else {
            return null;
        }
    }

    /**
     * 获取上次缓存清除时间
     *
     * @param context 上下文对象
     * @return 上次缓存清除时间的 Date 对象，如果没有记录则返回 null
     */
    public static String getLastCacheClearedTime(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        long cacheClearedTime = prefs.getLong(KEY_CACHE_CLEARED_TIME, -1);

        if (cacheClearedTime != -1) {
            return new Date(cacheClearedTime).getTime()/1000 + "";
        } else {
            return null;
        }
    }
}

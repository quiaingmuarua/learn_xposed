package com.demo.java.xposed.utils;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.annotation.SuppressLint;
import de.robv.android.xposed.XposedBridge;
import java.util.ArrayList;
import java.util.List;

public class SQLiteQueryHelper {

    /**
     * 执行通用的 SQL 查询
     *
     * @param dbPath 数据库文件路径
     * @param sql SQL 查询语句
     * @param bindArgs SQL 查询的参数
     * @param columnName 查询结果列名
     * @return 查询结果的列表
     */
    public static List<String> queryDatabase(String dbPath, String sql, String[] bindArgs, String columnName) {
        List<String> result = new ArrayList<>();
        SQLiteDatabase db = null;
        Cursor cursor = null;

        try {
            // 打开数据库
            db = SQLiteDatabase.openOrCreateDatabase(dbPath, null);

            // 执行 SQL 查询
            cursor = db.rawQuery(sql, bindArgs);

            if (cursor != null && cursor.moveToFirst()) {
                // 获取指定列的数据
                do {
                    @SuppressLint("Range")
                    String data = cursor.getString(cursor.getColumnIndex(columnName)); // 获取指定列名的数据
                    result.add(data);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            XposedBridge.log("Error querying database: " + e.getMessage());
        } finally {
            // 关闭数据库和游标
            if (cursor != null) cursor.close();
            if (db != null) db.close();
        }
        return result;
    }
}

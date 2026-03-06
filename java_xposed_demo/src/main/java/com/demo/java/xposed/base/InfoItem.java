package com.demo.java.xposed.base;


import android.content.Intent;

import java.util.function.Supplier;

// InfoItem.java
public class InfoItem {
    private String title;
    private String description;
    private Status status;

    //activity跳转
    private Supplier<Intent> intentSupplier;

    // item 结果
    private Supplier<String> resultSupplier; // New field for result supplier


    // 私有构造方法，防止直接实例化
    public InfoItem(String title, String description, Status status) {
        this.title = title;
        this.description = description;
        this.status = status;
    }

    // 静态工厂方法
    public static InfoItem withIntent(String title, String description, Status status, Supplier<Intent> intentSupplier) {
        InfoItem item = new InfoItem(title, description, status);
        item.intentSupplier = intentSupplier;
        return item;
    }

    public static InfoItem withResult(String title, String description, Status status, Supplier<String> resultSupplier) {
        InfoItem item = new InfoItem(title, description, status);
        item.resultSupplier = resultSupplier;
        return item;
    }

    public Supplier<Intent> getIntentSupplier() {
        return intentSupplier;
    }

    public Supplier<String> getResultSupplier() {
        return resultSupplier;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public Status getStatus() {
        return status;
    }
}

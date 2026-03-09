package com.example.sekiro.messages.model;

public enum DbActionEnum {
    DB_QUERY("dbQuery"),
    DELETE_CONVERSATION("deleteConversation"),
    DELETE_ALL_CONVERSATIONS("deleteAllConversations");

    private final String path;

    // 构造函数
    DbActionEnum(String path) {
        this.path = path;

    }

    public String getPath() {
        return path;
    }


    // 根据路径返回对应的枚举值
    public static DbActionEnum fromPath(String path) {
        for (DbActionEnum method : values()) {
            if (method.path.equalsIgnoreCase(path)) {
                return method;
            }
        }
        return null; // 或者抛出 IllegalArgumentException
    }
}

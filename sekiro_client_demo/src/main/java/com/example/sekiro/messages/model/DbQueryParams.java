package com.example.sekiro.messages.model;

import java.util.List;

public class DbQueryParams {

    private String table;
    private List<String> fieldsName;
    private String orderByField;
    private int cnt;

    private String conversationId;

    // 私有构造方法
    private DbQueryParams() {
    }

    // Getters
    public String getTable() {
        return table;
    }

    public List<String> getFieldsName() {
        return fieldsName;
    }

    public String getOrderByField() {
        return orderByField;
    }

    public int getCnt() {
        return cnt;
    }

    public String getConversationId() {
        return conversationId;
    }

    // Builder 静态类
    public static class Builder {
        private final DbQueryParams instance;

        public Builder() {
            instance = new DbQueryParams();
        }

        public Builder setTable(String table) {
            instance.table = table;
            return this;
        }

        public Builder setFieldsName(List<String> fieldsName) {
            instance.fieldsName = fieldsName;
            return this;
        }

        public Builder setOrderByField(String orderByField) {
            instance.orderByField = orderByField;
            return this;
        }

        public Builder setCnt(int cnt) {
            instance.cnt = cnt;
            return this;
        }

        public DbQueryParams build() {
            return instance;
        }

        public Builder setConversationId(String conversationId) {
            instance.conversationId = conversationId;
            return this;
        }
    }

    @Override
    public String toString() {
        return "DbQueryParams{" +
                "table='" + table + '\'' +
                ", fieldsName=" + fieldsName +
                ", orderByField='" + orderByField + '\'' +
                ", cnt=" + cnt +",conversationId="+conversationId+
                '}';
    }
}

package com.example.sekiro.messages.model;

public enum XpGrpcMethodEnum {

    LOOKUP("lookup" ), // 假设lookup需要phones
    KICKOFF("kickGroupUsers"),
    ADD_GROUP_USERS("addGroupUsers"), // phones和groupId是必需的
    AUTO_KICK_DELIVERED_USERS("autoKickDeliveredUsers"),
    AUTO_KICK_SENT_USERS("autoKickSentUsers"),


    GET_CURRENT_GROUP_INFO("getCurrentGroupInfo"),


    ADD_SYSTEM_CONTACT("addSystemContact"),
    DELETE_ALL_CONTACT("deleteAllContact"), //删除所有系统联系人




    RECEIVE_MESSAGES("receiveMessages");//暂时没用，有bug




    private final String path;


    // 构造函数
    XpGrpcMethodEnum(String path) {
        this.path = path;

    }

    public String getPath() {
        return path;
    }


    // 根据路径返回对应的枚举值
    public static XpGrpcMethodEnum fromPath(String path) {
        for (XpGrpcMethodEnum method : values()) {
            if (method.path.equalsIgnoreCase(path)) {
                return method;
            }
        }
        return null; // 或者抛出 IllegalArgumentException
    }

}

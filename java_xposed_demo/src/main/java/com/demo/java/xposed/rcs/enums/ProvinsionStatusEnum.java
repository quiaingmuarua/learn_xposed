package com.demo.java.xposed.rcs.enums;


public enum ProvinsionStatusEnum  {
    UNSET(0),
    PROVISIONED(1),
    NOT_PROVISIONED(2),
    NOT_PROVISIONED_BUT_INITIALIZED(3),
    UNRECOGNIZED(-1);


    /* renamed from: f */
    private final int value;

    ProvinsionStatusEnum(int i) {
        this.value = i;
    }


    public static ProvinsionStatusEnum fromValue(int value) {
        for (ProvinsionStatusEnum type : ProvinsionStatusEnum.values()) {
            if (type.value == value) {
                return type;
            }
        }
        return ProvinsionStatusEnum.UNRECOGNIZED;
    }


    public static GlobalRcsStatusEnum fromString(String name) {
        for (GlobalRcsStatusEnum type : GlobalRcsStatusEnum.values()) {
            if (type.name().equalsIgnoreCase(name)) {
                return type;
            }
        }
        return null; // 或者抛出异常
    }

}
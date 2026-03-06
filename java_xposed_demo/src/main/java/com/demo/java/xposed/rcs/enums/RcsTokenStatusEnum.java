package com.demo.java.xposed.rcs.enums;
public enum RcsTokenStatusEnum {
    JIBE_HAS_TOKEN(1),
    JIBE_NO_TOKEN(2),
    JIBE_NO_TOKEN_BLOCKED(3),
    THIRD_PARTY_HAS_TOKEN(4),
    THIRD_PARTY_NO_TOKEN(5),
    UNKNOWN_BACKEND_HAS_TOKEN(6),
    UNKNOWN_BACKEND_NO_TOKEN(7),
    UNKNOWN_BACKEND_NO_TOKEN_BLOCKED(8);


    /* renamed from: i */
    public final int value;

    RcsTokenStatusEnum(int i) {
        this.value = i;
    }


    public static RcsTokenStatusEnum fromValue(int value) {
        for (RcsTokenStatusEnum type : RcsTokenStatusEnum.values()) {
            if (type.value == value) {
                return type;
            }
        }
        return null;
    }
}
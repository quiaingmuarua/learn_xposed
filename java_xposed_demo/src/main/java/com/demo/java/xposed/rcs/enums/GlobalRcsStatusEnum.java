package com.demo.java.xposed.rcs.enums;
/*
10-20 : 注册前，环境是否准备好
20-30：注册中，状态
30-40： 注册后状态，
200:rcs可用

激活后： 可用、待可用、不可用

rcs 可用
EtouffeeStateMachine: onRegistrationUpdate from Tachyon, previousState: REGISTERED_WITH_PREKEYS, newState: REGISTERED_WITH_PREKEYS

 */
public enum GlobalRcsStatusEnum{
    //注册前环境准备
    DISABLED_VIA_GSERVICES(1), //rcs注册见面无法打开
    DISABLED_FROM_PREFERENCES(2), //rcs 设置开关关闭
    CARRIER_SETUP_PENDING(3), // rcs注册可用

    //开始注册--请求前
    MSG_TRANSITION_TO_CHECK_PRECONDITIONS(10),//开始注册
    MSG_SET_CONSENT_API_SUCCESS(11),//注册GMS服务ok
    MSG_TRANSITION_TO_REQUEST_WITH_TOKEN(12), //准备token复用
    MSG_TRANSITION_TO_REQUEST_WITH_IMSI(13),

    //开始注册 --请求后
    MSG_SEND_CONFIG_REQUEST(20),
    MSG_HTTP_RESPONSE(21),//发出了请求
    MSG_HTTP_200_OK(22), //请求200
    WaitingForOtpState(23),

    //激活中
    MSG_CONFIG_DOC(30), //准备激活成功



    //注册后
    AVAILABLE(100), //激活成功
    NOT_FOUND(101), //通常是复用不成功
    REGISTERED_WITH_PREKEYS(102),//准备可用，需要可用的gms签名
    UNIMPLEMENTED(103),
    PROVISIONED(200), //rcs可用
    PONG(201);

    public final int value;

    GlobalRcsStatusEnum(int i) {
        this.value = i;
    }


    public static GlobalRcsStatusEnum fromValue(int value) {
        for (GlobalRcsStatusEnum type : GlobalRcsStatusEnum.values()) {
            if (type.value == value) {
                return type;
            }
        }
        return null;
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

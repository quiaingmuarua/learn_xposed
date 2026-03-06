package com.demo.java.xposed.rcs.model;


import com.demo.java.xposed.utils.LogUtils;
import com.google.gson.Gson;

import java.util.Map;

public class KeyInfo {


    private static final String TAG = "Xposed_RcsKeyInfo";

    // 状态 200 成功
    private int machineState = 100;

    // 标签 send 、 register
    private int pluginStatus = 100;

    //类型 1、注册 2、发送
    private String tag;

    private Map<String, String> detailInfo;

    private String rawInfo;

    public KeyInfo(String tag, Map<String, String> detailInfo) {
        this.tag = tag;
        this.detailInfo = detailInfo;
    }

    public KeyInfo(String rawInfo, Map<String, String> detailInfo, String tag) {
        this.rawInfo = rawInfo;
        this.detailInfo = detailInfo;
        this.tag = tag;
    }

    public KeyInfo(String tag, String rawInfo) {
        this.rawInfo = rawInfo;
        this.tag = tag;
    }


    public static void printLog(KeyInfo keyInfo) {
        keyInfo.pluginStatus = KeyCommonInfo.Status.pluginStatus;
        keyInfo.machineState = KeyCommonInfo.Status.machineState;
        Gson gson = new Gson();
        LogUtils.show(TAG, gson.toJson(keyInfo));
    }

    /*
    MSG_TRANSITION_TO_CHECK_PRECONDITIONS
    MSG_TRANSITION_TO_SET_GOOGLE_TOS_CONSENT
    MSG_SET_CONSENT_COMPLETE
    MSG_TRANSITION_TO_VERIFY_MSISDN
    MSG_TRANSITION_TO_REQUEST_WITH_IMSI
    MSG_SEND_CONFIG_REQUEST
    MSG_HTTP_RESPONSE   //有发送请求，基本设备信息校验完成
    MSG_HTTP_200_OK:  //验证码请求发送完成，设备正常
    MSG_HTTP_400_BAD_REQUEST   //通常是设备信息校验不通过
    MSG_HTTP_403_FORBIDDEN  //代理问题
    RcsAvailability{mAvailability=AVAILABLE (RCS is setup)}   //rcs服务ok，注册完成

    层级：
    初始化异常：插件版本、 网络、自动脚本启动、初始化参数   （自检）
    设备异常：设备指纹风控
    账号异常： 无法发送rcs消息
    流程异常：ui

   异常类型：
    已知风控 ： 无法发送验证码 ，切换指纹
    非风控： ui 无法收到验证码 重试就好
    未知错误： 未被定义的错误，无法处理的风控 ，切换指纹还是无法发送验证码，
     */


}

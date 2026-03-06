package com.demo.java.xposed.rcs.apiCaller.core;

import com.demo.java.xposed.rcs.apiCaller.cache.CachedGroupInfo;
import com.demo.java.xposed.rcs.apiCaller.model.ChannelRequestParams;
import com.demo.java.xposed.rcs.apiCaller.model.DbQueryParams;
import com.demo.java.xposed.rcs.apiCaller.model.XpGrpcMethodEnum;
import com.example.sekiro.shared.CommandException;
import com.example.sekiro.shared.CommandHandler;
import com.example.sekiro.shared.CommandRouter;
import com.example.sekiro.shared.ErrorCode;

import org.json.JSONObject;

import java.util.Optional;

public class XposedCommandRouter  extends CommandRouter {


    static {
        registerDefaultResolver(ChannelRequestParams.class, XposedCommandRouter::buildDefaultChannelRequest);
        registerDefaultResolver(DbQueryParams.class, XposedCommandRouter::buildDbQueryParams);
    }


    // 默认注册（绑定默认构造器，无需 ClassLoader）
    public static <V> void registerChannel(String event, CommandHandler<ChannelRequestParams,V> handler) {
        innerRegister(event, handler, XposedCommandRouter::buildDefaultChannelRequest);
    }

    public static <V> void registerDb(String event, CommandHandler<DbQueryParams,V> handler) {
        innerRegister(event, handler, XposedCommandRouter::buildDbQueryParams);
    }



    private static DbQueryParams buildDbQueryParams(JSONObject jsonParams) {

        String conversations =  jsonParams.optString("conversations", "");


        return new DbQueryParams.Builder().setConversationId(conversations).build();
    }

    private static ChannelRequestParams buildDefaultChannelRequest(JSONObject jsonParams) {
        String event = extractParam(jsonParams, "event");

        // 通过 event 创建 XpGrpcMethodEnum
        XpGrpcMethodEnum methodEnum = Optional.ofNullable(XpGrpcMethodEnum.fromPath(event))
                .orElseThrow(() -> new CommandException(ErrorCode.PARSE_ERROR, "不支持的 event: " + event));

        // groupId 和 phoneNumberList 获取逻辑
        String groupId = jsonParams.has("groupId")
                ? jsonParams.optString("groupId", "")
                : CachedGroupInfo.getCurGroupId();

        String rawPhoneParam = jsonParams.optString("phones");

        String tokenHex = jsonParams.optString("tokenHex","");

        // 通过 Builder 创建 ChannelRequestParams
        return new ChannelRequestParams.Builder()
                .setXpGrpcMethodEnum(methodEnum)
                .setPhoneNumberList(rawPhoneParam)
                .setGroupId(groupId).setTokenHex(tokenHex)
                .autoSetParams() // 自动根据类型验证和设置参数
                .build();
    }


}

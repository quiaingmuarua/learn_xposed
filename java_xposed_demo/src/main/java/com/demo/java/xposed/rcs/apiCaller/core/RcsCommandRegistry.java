package com.demo.java.xposed.rcs.apiCaller.core;

import com.demo.java.xposed.rcs.apiCaller.manager.ActionHandlerManager;
import com.demo.java.xposed.rcs.apiCaller.manager.DbHandlerManager;
import com.demo.java.xposed.rcs.apiCaller.manager.InnerHandlerManager;
import com.example.messages.model.DbActionEnum;
import com.example.messages.model.XpGrpcMethodEnum;
import com.example.messages.cache.CachedGroupInfo;
import com.example.messages.XposedCommandRouter;

import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * 命令注册入口类。
 * 所有事件（如用户操作、DB操作）统一在这里注册。
 * 推荐在应用启动或 hook 初始化时调用 init()。
 */

public class RcsCommandRegistry {
    public static void init(ClassLoader classLoader) {

        initActionHandler(classLoader);
        initInnerAction(classLoader);
        initDbQuery(classLoader);

    }



    private static void initActionHandler(ClassLoader classLoader){

        XposedCommandRouter.registerChannel(XpGrpcMethodEnum.LOOKUP.getPath(), (channelRequestParams, commandContext) ->
                ActionHandlerManager.handleLookupRegistered(channelRequestParams, classLoader));
        XposedCommandRouter.registerChannel(XpGrpcMethodEnum.KICKOFF.getPath(), (channelRequestParams, commandContext) ->
              ActionHandlerManager.handleKickGroupUsers(channelRequestParams,  classLoader));

        XposedCommandRouter.registerChannel(XpGrpcMethodEnum.ADD_GROUP_USERS.getPath(), (channelRequestParams, commandContext) ->
               ActionHandlerManager.handleAddGroupUsers(channelRequestParams,  classLoader));

    }


    private static void initInnerAction(ClassLoader classLoader){
        XposedCommandRouter.registerChannel(XpGrpcMethodEnum.AUTO_KICK_DELIVERED_USERS.getPath(), (channelRequestParams,commandContext) ->
               InnerHandlerManager.handleAutoKickDeliverGroupUsers(CachedGroupInfo.getDeliveredGroupMembersMap(), classLoader));
        XposedCommandRouter.registerChannel(XpGrpcMethodEnum.AUTO_KICK_SENT_USERS.getPath(), (channelRequestParams,commandContext) ->
               InnerHandlerManager.handleAutoKickSentGroupUsers(CachedGroupInfo.getSentGroupMembersMap(),  classLoader));

        XposedCommandRouter.registerChannel(XpGrpcMethodEnum.ADD_SYSTEM_CONTACT.getPath(), (channelRequestParams,commandContext) ->
                InnerHandlerManager.handleAddSystemContact(commandContext.getContext(),channelRequestParams.getPhoneNumberList()));

        XposedCommandRouter.registerChannel(XpGrpcMethodEnum.DELETE_ALL_CONTACT.getPath(), (channelRequestParams,commandContext) ->
                InnerHandlerManager.handleDeleteAllContact(commandContext.getContext()));


        XposedCommandRouter.registerChannel(XpGrpcMethodEnum.RECEIVE_MESSAGES.getPath(), (channelRequestParams,commandContext) ->
                InnerHandlerManager.receiveMessages(channelRequestParams.getTokenHex(),classLoader));


    }



    private static void initDbQuery(ClassLoader classLoader){
        XposedCommandRouter.registerDb(DbActionEnum.DELETE_CONVERSATION.getPath(), (dbQueryParams, commandContext) ->
              DbHandlerManager.deleteConversation(dbQueryParams,classLoader));

        XposedCommandRouter.registerDb(DbActionEnum.DELETE_ALL_CONVERSATIONS.getPath(), (dbQueryParams, commandContext) ->
                DbHandlerManager.deleteAllConversations(dbQueryParams,classLoader));

    }








}

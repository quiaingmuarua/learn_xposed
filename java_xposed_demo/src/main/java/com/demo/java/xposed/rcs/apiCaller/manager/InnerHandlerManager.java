package com.demo.java.xposed.rcs.apiCaller.manager;

import android.content.Context;

import com.demo.java.xposed.base.BaseAppHook;
import com.demo.java.xposed.rcs.apiCaller.core.GrpcCallHelper;
import com.demo.java.xposed.utils.ContactUtils;
import com.demo.java.xposed.utils.LogUtils;
import com.example.sekiro.messages.ChannelRequestParams;
import com.example.sekiro.messages.model.XpGrpcMethodEnum;
import com.example.sekiro.messages.cache.CachedGroupInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class InnerHandlerManager  extends BaseAppHook {


    public static String  receiveMessages(String hexToken,ClassLoader classLoader) throws Exception {
        ChannelRequestParams.Builder builder = new ChannelRequestParams.Builder();
        builder.setXpGrpcMethodEnum(XpGrpcMethodEnum.RECEIVE_MESSAGES);
        builder.setTokenHex(hexToken);
        Object result= GrpcCallHelper.getChannelResult(builder.build(),classLoader);
        LogUtils.printParams("handleReceiveMessages " , protoObjToHex(result));
        return "ok";

    }


    public static List<String> handleAddSystemContact(Context context,List<String> numbers) throws Exception {

        for (String number : numbers) {
            ContactUtils.addContact(context,number);
        }

        return numbers;
    }

    public static String handleDeleteAllContact(Context context) throws Exception {

        ContactUtils.deleteAllContacts(context);
        return "OK";

    }


    public static List<String> handleAutoKickSentGroupUsers( Map<String, Set<String>> membersMap, ClassLoader classLoader) throws Exception {
        List<String> phones=new ArrayList<>();
        for (Map.Entry<String, Set<String>> entry : membersMap.entrySet()) {
            ChannelRequestParams params = genAutoClickParams(entry,classLoader);
            Object result= GrpcCallHelper.getChannelResult(params,classLoader);
            phones.addAll(entry.getValue());
            LogUtils.printParams("handleAutoKickSentGroupUsers " ,entry, result);
        }
        CachedGroupInfo.clearAllSentMembers();
        return phones;
    }


    public static  List<String> handleAutoKickDeliverGroupUsers( Map<String, Set<String>> membersMap, ClassLoader classLoader) throws Exception {
        List<String> phones=new ArrayList<>();
        for (Map.Entry<String, Set<String>> entry : membersMap.entrySet()) {
            ChannelRequestParams params = genAutoClickParams(entry,classLoader);
            Object result=GrpcCallHelper.getChannelResult(params,classLoader);
            phones.addAll(entry.getValue());
            LogUtils.printParams("handleAutoKickDeliverGroupUsers " ,entry, result);
        }
        CachedGroupInfo.clearAllDeliveredMembers();
        return phones;
    }




    private static ChannelRequestParams genAutoClickParams(Map.Entry<String, Set<String>> entry, ClassLoader classLoader) throws Exception {
        String groupId = entry.getKey();
        Set<String> members = entry.getValue();
        LogUtils.show("genAutoClickParams groupId = " + groupId);
        LogUtils.show("genAutoClickParams members = " + members);
        ChannelRequestParams.Builder builder = new ChannelRequestParams.Builder();
        builder.setGroupId(groupId);
        builder.setPhoneNumberList(new ArrayList<>(entry.getValue()));
        builder.setXpGrpcMethodEnum(XpGrpcMethodEnum.KICKOFF);
        return builder.build();
    }
}

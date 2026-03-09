package com.demo.java.xposed.rcs.apiCaller.manager;

import com.demo.java.xposed.rcs.apiCaller.core.GrpcCallHelper;
import com.example.messages.ChannelRequestParams;
import com.demo.java.xposed.rcs.hook.messages.Rcs;
import com.demo.java.xposed.utils.LogUtils;
import com.google.protobuf.util.JsonFormat;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.robv.android.xposed.XposedHelpers;

public class ActionHandlerManager {


    public static List<String> handleAddGroupUsers(ChannelRequestParams channelRequestParams, ClassLoader classLoader) throws Exception {
        Object result = GrpcCallHelper.getChannelResult(channelRequestParams,classLoader);
        LogUtils.show("handleAddGroupUsers result = " + result);
        return channelRequestParams.getPhoneNumberList();
    }

    public static List<String> handleCreateGroup(ChannelRequestParams channelRequestParams, ClassLoader classLoader) throws Exception {
        Object result = GrpcCallHelper.getChannelResult(channelRequestParams,classLoader);
        LogUtils.show("handleCreateGroup result = " + result);
        return channelRequestParams.getPhoneNumberList();

    }


    public static List<String> handleKickGroupUsers(ChannelRequestParams channelRequestParams, ClassLoader classLoader) throws Exception {
        Object result = GrpcCallHelper.getChannelResult(channelRequestParams,classLoader);
        LogUtils.show("handleKickGroupUsersRequest result = " + result);
        return channelRequestParams.getPhoneNumberList();
    }

    public static Map<String, String> handleLookupRegistered(ChannelRequestParams channelRequestParams, ClassLoader classLoader) throws Exception {
        Object result = GrpcCallHelper.getChannelResult(channelRequestParams,classLoader);
            LogUtils.show("handleLookupRegistered result = " + result);
            Rcs.LookupRegisteredResposne lookupRegisteredResposne = Rcs.LookupRegisteredResposne.parseFrom((byte[]) XposedHelpers.callMethod(result, "toByteArray"));
            Map<String,String> map =new HashMap<>();
            for (Rcs.Ebrz item : lookupRegisteredResposne.getPhoneItemList()) {
                Rcs.Profile profile = item.getProfile();
                Rcs.PhoneNumber phoneNumber = item.getPhoneNumber();
                if (profile != null && profile.getPropertiesCount() > 0) {
                    if(profile.getId().isEmpty()){
                        map.put(phoneNumber.getNumber(),"apple");
                    }else {
                        map.put(phoneNumber.getNumber(),"android");
                    }
                }else {
                    map.put(phoneNumber.getNumber(),"no_rcs");
                }


            }
            String json = JsonFormat.printer().print(lookupRegisteredResposne);
            LogUtils.simpleShow("handleLookupRegistered lookupRegisteredResposne json= " + json);
            return map;

    }



}
/*
"number": "+447521344345"
"id": "3029e5d2-66fe-41e8-a6a8-7eabf2cde2f1"


"id": "2427f6a5-9f66-4a72-be42-ae5ce0066d02",
 */

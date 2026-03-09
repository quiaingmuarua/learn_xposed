package com.demo.java.xposed.rcs.apiCaller.core;

import static com.demo.java.xposed.rcs.apiCaller.cache.CachedUnaryRpc.cacheTokenFromHex;

import android.text.TextUtils;

import com.demo.java.xposed.rcs.apiCaller.cache.CachedGroupInfo;
import com.demo.java.xposed.rcs.apiCaller.cache.CachedUnaryRpc;
import com.demo.java.xposed.rcs.apiCaller.cache.XposedClassCacher;
import com.example.sekiro.messages.model.ChannelRequestParams;
import com.example.sekiro.messages.model.XpGrpcMethodEnum;
import com.example.sekiro.messages.shared.CommandException;
import com.example.sekiro.messages.shared.ErrorCode;
import com.demo.java.xposed.rcs.hook.messages.Rcs;
import com.demo.java.xposed.utils.LogUtils;
import com.google.protobuf.InvalidProtocolBufferException;

import java.util.EnumMap;
import java.util.List;
import java.util.function.Function;

import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class GrpcCallHelper {

    private static final EnumMap<XpGrpcMethodEnum, Object> methodDescriptorMap = new EnumMap<>(XpGrpcMethodEnum.class);
    private static final EnumMap<XpGrpcMethodEnum, Object> defaultInstanceMap = new EnumMap<>(XpGrpcMethodEnum.class);
    private static final EnumMap<XpGrpcMethodEnum, Function<ChannelRequestParams, byte[]>> requestGeneratorMap = new EnumMap<>(XpGrpcMethodEnum.class);

    public static void run(XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {

        try {
            ClassLoader classLoader = loadPackageParam.classLoader;
            initMethodDescriptorMap(classLoader);
            initDefaultInstanceMap(classLoader);
            initRequestGeneratorMap(classLoader);
        } catch (Exception e) {
            LogUtils.printStackErrInfo("GrpcCallHelper run err= ", e);
        }
    }

    private static  void initRequestGeneratorMap(ClassLoader classLoader){
        // 注册每个方法的请求体构建逻辑
        requestGeneratorMap.put(XpGrpcMethodEnum.LOOKUP, GrpcCallHelper::genXpLookupRegisteredRequests);
        requestGeneratorMap.put(XpGrpcMethodEnum.KICKOFF, GrpcCallHelper::genXpActionGroupUsersRequests);
        requestGeneratorMap.put(XpGrpcMethodEnum.ADD_GROUP_USERS, GrpcCallHelper::genXpActionGroupUsersRequests);
        requestGeneratorMap.put(XpGrpcMethodEnum.RECEIVE_MESSAGES, GrpcCallHelper::genXpReceiveMessages);
        LogUtils.show("GrpcCallHelper  initRequestGeneratorMap " + methodDescriptorMap);

    }

    private static void initDefaultInstanceMap(ClassLoader classLoader) throws ClassNotFoundException {
        LogUtils.show("GrpcCallHelper initDefaultInstanceMap " + XposedClassCacher.LookupRegisteredRequetsClass);
        defaultInstanceMap.put(XpGrpcMethodEnum.LOOKUP, XposedHelpers.getStaticObjectField(XposedClassCacher.LookupRegisteredRequetsClass, "f"));
        defaultInstanceMap.put(XpGrpcMethodEnum.KICKOFF, XposedHelpers.getStaticObjectField(XposedClassCacher.KickGroupUsersRequestClass, "e"));
        defaultInstanceMap.put(XpGrpcMethodEnum.ADD_GROUP_USERS, XposedHelpers.getStaticObjectField(XposedClassCacher.AddGroupUsersRequestClass, "e"));
        defaultInstanceMap.put(XpGrpcMethodEnum.RECEIVE_MESSAGES, XposedHelpers.getStaticObjectField(XposedClassCacher.ReceiveMessagesRequestsRpcClass, "c"));

    }

    private static void initMethodDescriptorMap(ClassLoader classLoader) throws ClassNotFoundException {
        Class<?> RegistrationGrpcClass = classLoader.loadClass("ebda");
        Class<?> GroupMethodDescriptorClass = classLoader.loadClass("earq");
        Class<?> MessagingDescriptorClass = classLoader.loadClass("ebcf");
        methodDescriptorMap.put(XpGrpcMethodEnum.LOOKUP, XposedHelpers.callStaticMethod(RegistrationGrpcClass, "b"));
        methodDescriptorMap.put(XpGrpcMethodEnum.KICKOFF, XposedHelpers.callStaticMethod(GroupMethodDescriptorClass, "f"));
        methodDescriptorMap.put(XpGrpcMethodEnum.ADD_GROUP_USERS, XposedHelpers.callStaticMethod(GroupMethodDescriptorClass, "b"));
        methodDescriptorMap.put(XpGrpcMethodEnum.RECEIVE_MESSAGES, XposedHelpers.getStaticObjectField(MessagingDescriptorClass, "a"));
        LogUtils.show("GrpcCallHelper initMethodDescriptorMap " + methodDescriptorMap);
    }

    public static Object getChannelResult(ChannelRequestParams channelRequestParams,ClassLoader classLoader) throws Exception {
        Object InternalGrpcChannel = CachedUnaryRpc.getInternalGrpcChannel();
        Object Metadata = CachedUnaryRpc.getMetadata();
        XpGrpcMethodEnum xpGrpcMethodEnum=channelRequestParams.getXpGrpcMethodEnum();


        if (InternalGrpcChannel == null || Metadata == null) {
            LogUtils.show("getCachedRequestParams is null");
            throw new CommandException(ErrorCode.XP_ENV_ERROR, "getCachedRequestParams is null");
        }

        Object methodDescriptor = methodDescriptorMap.get(xpGrpcMethodEnum);
        if (methodDescriptor == null) {
            LogUtils.show("methodDescriptor is null");
            //重新进行初始化
            initMethodDescriptorMap(classLoader);
            throw new CommandException(ErrorCode.XP_ENV_ERROR, "methodDescriptor is null");
        }
        //如果有token hex则用参数传递的最新的
        if(!TextUtils.isEmpty(channelRequestParams.getTokenHex())) {
            LogUtils.show("channelRequestParams has new token");
            cacheTokenFromHex(channelRequestParams.getTokenHex());
        }


        // 使用函数式接口从 Map 中获取对应的生成器方法
        Function<ChannelRequestParams, byte[]> generator = requestGeneratorMap.get(xpGrpcMethodEnum);
        if (generator == null) {
            throw new CommandException(ErrorCode.PARSE_ERROR, "不支持的请求类型: " + xpGrpcMethodEnum +" 支持的类型 " + requestGeneratorMap.keySet());
        }
        byte[] bytes = generator.apply(channelRequestParams);

        Object defaultInstance = defaultInstanceMap.get(xpGrpcMethodEnum);

        //转换成messages 对象
        Object requestData = XposedHelpers.callMethod(defaultInstance, "parseFrom", defaultInstance, bytes);
        return GrpcCallSender.sendGrpc(classLoader,InternalGrpcChannel, methodDescriptor ,requestData, Metadata);
    }


    private static byte[] genXpActionGroupUsersRequests(ChannelRequestParams channelRequestParams) {
        String groupId= channelRequestParams.getGroupId();
        List<String> phoneNumberList = channelRequestParams.getPhoneNumberList();
        if(TextUtils.isEmpty(groupId)){
            throw new CommandException(ErrorCode.PARSE_ERROR,"makeActionGroupUsersRequests groupId不能为空");
        }
        Rcs.RcsConferenceProperties rcsConferenceProperties= null;
        try {
            rcsConferenceProperties = Rcs.RcsConferenceProperties.parseFrom(CachedGroupInfo.getRrsConferencePropertiesBytes(groupId));
        } catch (InvalidProtocolBufferException e) {
            throw new CommandException(ErrorCode.PARSE_ERROR,"解析 rcsConferenceProperties 错误");
        }

        if(rcsConferenceProperties==null){
            throw new CommandException(ErrorCode.PARSE_ERROR,"makeActionGroupUsersRequests groupId不存在");
        }

        LogUtils.show("makeActionGroupUsersRequests groupId " + groupId + " phoneNumberList=" + phoneNumberList);
        Rcs.ActionGroupUsersRequests.Builder actionGroupUsersRequests = Rcs.ActionGroupUsersRequests.newBuilder();
        actionGroupUsersRequests.setTachyonRegistrationToken(CachedUnaryRpc.getNewTokenWithoutClearFlag());
        Rcs.ActionIdItem.Builder actionItemBuilder = Rcs.ActionIdItem.newBuilder();
        actionItemBuilder.setIdType(2);
        actionItemBuilder.setId(groupId);
        actionItemBuilder.setMsgType("RCS");
        Rcs.GroupProperty.Builder groupProperty = Rcs.GroupProperty.newBuilder();
        groupProperty.setKey(rcsConferenceProperties.getCountryCode());
        groupProperty.setValue(rcsConferenceProperties.getConferenceType());
        groupProperty.build();
        Rcs.GroupProperties.Builder groupProperties = Rcs.GroupProperties.newBuilder();
        groupProperties.setGroupProperty(groupProperty);
        actionItemBuilder.setGroupProperties(groupProperties);
        actionItemBuilder.build();
        actionGroupUsersRequests.setActionIdItem(actionItemBuilder);
        for (String phoneNumber : phoneNumberList) {
            Rcs.PhoneNumber.Builder phoneNumberBuilder = Rcs.PhoneNumber.newBuilder();
            phoneNumberBuilder.setType(1);
            phoneNumberBuilder.setNumber(phoneNumber);
            phoneNumberBuilder.setProtocol("RCS");
            actionGroupUsersRequests.addPhoneNumber(phoneNumberBuilder);
        }
        return actionGroupUsersRequests.build().toByteArray();
    }

    private static byte[] genXpReceiveMessages(ChannelRequestParams channelRequestParams) {
        LogUtils.show("makeReceiveMessages  ReceiveMessagesRequest");
        Rcs.ReceiveMessagesRequestsRpc.Builder receiveMessagesRequest = Rcs.ReceiveMessagesRequestsRpc.newBuilder();
        receiveMessagesRequest.setTachyonRegistrationToken(CachedUnaryRpc.getNewToken());

        return receiveMessagesRequest.build().toByteArray();

    }

    private static byte[] genXpLookupRegisteredRequests(ChannelRequestParams channelRequestParams) {
        LogUtils.show("makeLookupRegisteredRequests LookupRegisteredRequests phoneNumberList " + channelRequestParams.getPhoneNumberList());
        Rcs.LookupRegisteredRequests.Builder lookupRegisteredRequests = Rcs.LookupRegisteredRequests.newBuilder();
        lookupRegisteredRequests.setTachyonRegistrationToken(CachedUnaryRpc.getNewToken());
        lookupRegisteredRequests.setGroupLookupConf(Rcs.GroupLookupConf.newBuilder().setMode(2).build());
        for (String phoneNumber : channelRequestParams.getPhoneNumberList()) {
            if (!phoneNumber.contains("+")){
                phoneNumber="+"+phoneNumber;
            }
            Rcs.PhoneNumber.Builder phoneNumberBuilder = Rcs.PhoneNumber.newBuilder();
            phoneNumberBuilder.setNumber(phoneNumber);
            phoneNumberBuilder.setType(1);
            lookupRegisteredRequests.addPhoneNumber(phoneNumberBuilder.build());
        }
        return lookupRegisteredRequests.build().toByteArray();
    }



}

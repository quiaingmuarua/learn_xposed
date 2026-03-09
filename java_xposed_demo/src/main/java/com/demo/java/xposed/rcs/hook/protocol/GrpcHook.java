package com.demo.java.xposed.rcs.hook.protocol;

import com.demo.java.xposed.base.BaseAppHook;
import com.demo.java.xposed.device.PluginInit;
import com.demo.java.xposed.rcs.apiCaller.cache.CachedUnaryRpc;
import com.demo.java.xposed.rcs.hook.messages.Rcs;
import com.demo.java.xposed.rcs.hook.messages.Rcs.LookupRegisteredRequests;
import com.demo.java.xposed.rcs.model.MsgItem;
import com.demo.java.xposed.rcs.model.RegisterKeyInfo;
import com.demo.java.xposed.rcs.model.SendMsgKeyInfo;
import com.demo.java.xposed.utils.LogUtils;
import com.demo.java.xposed.utils.collection.StringUtils;
import com.example.sekiro.messages.cache.XposedClassCacher;
import com.example.sekiro.messages.shared.CachedGroupInfo;
import com.google.protobuf.util.JsonFormat;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class GrpcHook extends BaseAppHook {


    public static void run(XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {

        try {
            hookReceiveMessagesResponse(loadPackageParam);
            hookChannel(loadPackageParam);
            if (!PluginInit.isDebug) {
                return;
            }
            hookCronet(loadPackageParam);
            hookMessageMetadata(loadPackageParam);
            hooKClientCall(loadPackageParam);
            hookAsyncChannel(loadPackageParam);

        } catch (Exception e) {
            LogUtils.printStackErrInfo("GrpcProtoBufHook", e);
        }

    }


    public static void hookAsyncChannel(XC_LoadPackage.LoadPackageParam loadPackageParam) {
        Class<?> StubClientCallsClass = XposedClassCacher.StubClientCallsClass;
        ClassLoader classLoader = loadPackageParam.classLoader;
        try {
            Class<?> AbsrractClientCallClass = classLoader.loadClass("edbb");
            Class<?> AbstractClientCallListenerClass = classLoader.loadClass("eedx");
            XposedHelpers.findAndHookMethod(StubClientCallsClass, "b", AbsrractClientCallClass, java.lang.Object.class, AbstractClientCallListenerClass, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    super.beforeHookedMethod(param);
                    LogUtils.printParams("StubClientCallsClass hookAsyncChannel", param.args);
                    LogUtils.show("StubClientCallsClass sendMessage hex= " + protoObjToHex(param.args[1]));
                }

                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    super.afterHookedMethod(param);
                }
            });
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }


    }

    public static void hookChannel(XC_LoadPackage.LoadPackageParam loadPackageParam){
        ClassLoader classLoader = loadPackageParam.classLoader;
        Class<?> GrpcChannelManagerClass = null;
        try {
            GrpcChannelManagerClass = classLoader.loadClass("bjsw");
            Class<?> eeoz = classLoader.loadClass("eeoz");
            Class<?> drfc = classLoader.loadClass("drfc");
            Class<?> bkze = classLoader.loadClass("bkze");
            Class<?> afcn = classLoader.loadClass("afcn");
            XposedHelpers.findAndHookConstructor(GrpcChannelManagerClass, android.content.Context.class, eeoz, drfc, bkze, eeoz, afcn, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    super.beforeHookedMethod(param);

                }
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    super.afterHookedMethod(param);
                    LogUtils.printParams("GrpcChannelManagerClass hookChannel",param.args);
                    LogUtils.printParams("GrpcChannelManagerClass hookChannel this",param.thisObject);

                }
            });
            Class<?> InternalGrpcChannelClass = classLoader.loadClass("edsw");
            Class<?> MethodDescriptorClass = XposedClassCacher.MethodDescriptorClass;
            Class<?> CallOptionsClass = XposedClassCacher.callOptionsClass;
            Class<?> ManagedChannelImpl = XposedClassCacher.ManagedChannelImpl;
            XposedHelpers.findAndHookMethod(InternalGrpcChannelClass, "a", MethodDescriptorClass, CallOptionsClass, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    super.beforeHookedMethod(param);
                }

                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    super.afterHookedMethod(param);
                    Object thisChannel = param.thisObject;
                    Object methodDescriptor = param.args[0];
                    Object callOptions = param.args[1];
                    // 取出 InternalGrpcChannel.f178486a -> RealChannel
                    Object realChannel = XposedHelpers.getObjectField(thisChannel, "a");
                    LogUtils.show("InternalGrpcChannelClass newCall  InternalGrpcChannel =" + printChannel(thisChannel));
                    LogUtils.show("InternalGrpcChannelClass newCall  realChannel=" + printChannel(thisChannel));
                    LogUtils.show("InternalGrpcChannelClass newCall  methodDescriptor=" + methodDescriptor);
                    LogUtils.show("InternalGrpcChannelClass newCall  callOptions=" + callOptions);
                    CachedUnaryRpc.cacheInternalGrpcChannel(realChannel);


                }
            });
            XposedHelpers.findAndHookMethod(ManagedChannelImpl, "a", MethodDescriptorClass, CallOptionsClass, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    super.beforeHookedMethod(param);
                    LogUtils.printParams("hooKClientCall ManagedChannelImpl args ", param.args);
                    LogUtils.printParams("hooKClientCall ManagedChannelImpl this ", param.thisObject);
                }

                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    super.afterHookedMethod(param);
                }
            });


        } catch (ClassNotFoundException e) {
            LogUtils.printStackErrInfo("hookChannel err",e);
        }

    }


    public static void hookCronet(XC_LoadPackage.LoadPackageParam loadPackageParam) throws ClassNotFoundException {
        ClassLoader classLoader = loadPackageParam.classLoader;
        Class<?> CronetBidirectionalStreamCallbackClass = classLoader.loadClass("edlb");

        Class<?> BidirectionalStreamClass = classLoader.loadClass("org.chromium.net.BidirectionalStream");

        Class<?> UrlResponseInfoClass = classLoader.loadClass("org.chromium.net.UrlResponseInfo");
        XposedHelpers.findAndHookMethod(CronetBidirectionalStreamCallbackClass, "onReadCompleted", BidirectionalStreamClass, UrlResponseInfoClass, java.nio.ByteBuffer.class, boolean.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);
                LogUtils.printParams("CronetBidirectionalStreamCallbackClass onReadCompleted", param.args);
            }

            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
            }
        });

    }

    public static void hooKClientCall(XC_LoadPackage.LoadPackageParam loadPackageParam) {
        try {
            ClassLoader classLoader = loadPackageParam.classLoader;
            Class<?> ClientCallImplClass = XposedClassCacher.ClientCallImplClass;
            XposedHelpers.findAndHookMethod(ClientCallImplClass, "f", java.lang.Object.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    super.beforeHookedMethod(param);
                    Object obj = param.args[0];
                    Object method = XposedHelpers.getObjectField(param.thisObject, "b");
                    String hex_obj=protoObjToHex(obj);
                    LogUtils.show("ClientCallImplClass sendMessage request " + obj);
                    LogUtils.show("ClientCallImplClass sendMessage hex= " + hex_obj);
                    LogUtils.show("ClientCallImplClass sendMessage method " + method);

                    if (method.toString().contains("LookupRegistered")) {
                        LogUtils.show("ClientCallImplClass sendMessage method is LookupRegistered");
                        // 3. 反序列化为 proto 对象
                        LookupRegisteredRequests lookupRegisteredRequests = LookupRegisteredRequests.parseFrom((byte[]) XposedHelpers.callMethod(obj, "toByteArray"));
                        // 4. 可选：打印 JSON（调试）
                        String json = JsonFormat.printer().print(lookupRegisteredRequests);
                        LogUtils.simpleShow("ClientCallImplClass LookupRegisteredRequests json= " + json);
//                        MockLookupRegistered.makeLookupRegisteredRequests(CachedUnaryRpc.tachyonRegistrationTokenHex,)
                        CachedUnaryRpc.cacheTokenFromHex(StringUtils.bytesToHexString(lookupRegisteredRequests.getTachyonRegistrationToken().toByteArray()));

                    }
                    if (method.toString().contains("KickGroupUsers")) {
                        Rcs.ActionGroupUsersRequests kickGroupUsersRequests = Rcs.ActionGroupUsersRequests.parseFrom((byte[]) XposedHelpers.callMethod(obj, "toByteArray"));
                        String json = JsonFormat.printer().print(kickGroupUsersRequests);
                        LogUtils.simpleShow("ClientCallImplClass KickGroupUsers json= " + json);
                        LogUtils.show("ClientCallImplClass KickGroupUsers hex= " + StringUtils.bytesToHexString(kickGroupUsersRequests.toByteArray()));
                    }
                    if(method.toString().contains("ReceiveMessages") && hex_obj.length()>100){
                        Rcs.ReceiveMessagesRequestsRpc receiveMessagesRequestsRpc = Rcs.ReceiveMessagesRequestsRpc.parseFrom((byte[]) XposedHelpers.callMethod(obj, "toByteArray"));
                        String json = JsonFormat.printer().print(receiveMessagesRequestsRpc);
                        LogUtils.simpleShow("ClientCallImplClass ReceiveMessagesRequestsRpc json= " + json);
                        CachedUnaryRpc.cacheTokenFromHex(StringUtils.bytesToHexString(receiveMessagesRequestsRpc.getTachyonRegistrationToken().toByteArray()));
                    }

                    if (method.toString().contains("SendMessage")){
                        //todo

                    }
                }

                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    super.afterHookedMethod(param);
                }
            });

            XposedHelpers.findAndHookMethod(ClientCallImplClass, "a", XposedClassCacher.clientCallListenerClass, XposedClassCacher.MetadataGrpcClass, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    super.beforeHookedMethod(param);
                    LogUtils.printParams("ClientCallImplClass start() args= ", param.args);
                    LogUtils.printParams("ClientCallImplClass start() this=", param.thisObject);
                    CachedUnaryRpc.cacheMetadata(param.args[1]);
                }

                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    super.afterHookedMethod(param);
                }
            });


            Class<?> MethodDescriptorClass = XposedClassCacher.MethodDescriptorClass;
            Class<?> CallOptionsClass = XposedClassCacher.callOptionsClass;
            Class<?> ClientCallsClass = XposedClassCacher.ClientCallsClass;
            Class<?> GrpcChannelClass = XposedClassCacher.GrpcChannelClass;
            Class<?> MetadataGrpcClass = XposedClassCacher.MetadataGrpcClass;
            Class<?> ContinuationUnitClass = XposedClassCacher.ContinuationUnitClass;
            XposedHelpers.findAndHookMethod(ClientCallsClass, "b", GrpcChannelClass, MethodDescriptorClass, java.lang.Object.class, CallOptionsClass, MetadataGrpcClass, ContinuationUnitClass, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    super.beforeHookedMethod(param);
                    Object channel = param.args[0];
                    Object method = param.args[1];
                    Object request = param.args[2];
                    Object callOptions = param.args[3];
                    Object metadata = param.args[4];
                    Object continuation = param.args[5];
                    LogUtils.show("ClientCallsClass Hooked callUnary() 方法");
                    LogUtils.printParams("prams args=", param.args);
                    LogUtils.show("├─ channel     = " + (channel != null ? printChannel(channel) : "null"));
                    LogUtils.show("├─ methodDesc  = " + (method != null ? method : "null"));
                    LogUtils.show("├─ callOptions = " + (callOptions != null ? callOptions : "null"));
                    LogUtils.show("├─ metadata    = " + (metadata != null ? metadata : "null"));
                    LogUtils.show("├─ continuation= " + (continuation != null ? continuation : "null"));
                    LogUtils.show("├─ request hex = " + protoObjToHex(request));

                }

                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    super.afterHookedMethod(param);
                }
            });


        } catch (Exception e) {
            LogUtils.printStackErrInfo("ClientCallImplClass hooKClientCall err ", e);
        }
    }


    public static void hookReceiveMessagesResponse(XC_LoadPackage.LoadPackageParam loadPackageParam) throws ClassNotFoundException {
        ClassLoader classLoader = loadPackageParam.classLoader;
        Class<?> InnerReceiveMessagesResponseObserverClass = classLoader.loadClass("bkna");
        XposedHelpers.findAndHookMethod(InnerReceiveMessagesResponseObserverClass, "c", java.lang.Object.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);
                LogUtils.show("InnerReceiveMessagesResponseObserver bkna onNext " + protoObjToHex(param.args[0]));
                RegisterKeyInfo.getInstance().addStatusList("PONG");
                if (protoObjToHex(param.args[0]).equals("1a00") || protoObjToHex(param.args[0]).equals("2a00") || protoObjToHex(param.args[0]).length()<10) {
                    LogUtils.show("InnerReceiveMessagesResponseObserver onNext receive PONG");
                }
            }

            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
            }
        });


        XposedHelpers.findAndHookMethod(InnerReceiveMessagesResponseObserverClass, "b", java.lang.Throwable.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);
                LogUtils.show("InnerReceiveMessagesResponseObserver onError " + param.args[0]);
                RegisterKeyInfo.getInstance().addStatusList("UNIMPLEMENTED");

            }

            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
            }
        });
    }


    public static void hookMessageMetadata(XC_LoadPackage.LoadPackageParam loadPackageParam) throws ClassNotFoundException {
        ClassLoader classLoader = loadPackageParam.classLoader;

        Class<?> MessageMetadataClass = classLoader.loadClass("dsdj");

        Class<?> ebqfClass = classLoader.loadClass("ebqf");
        Class<?> dnfsClass = classLoader.loadClass("dnfs");
        Class<?> dsbpClass = classLoader.loadClass("dsbp");
        XposedHelpers.findAndHookConstructor(MessageMetadataClass, java.lang.String.class, ebqfClass, ebqfClass, int.class, int.class, dnfsClass, ebqfClass, dsbpClass, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);
            }

            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
                LogUtils.show("MessageMetadataClass " + param.thisObject);
                String msgId= XposedHelpers.getObjectField(param.thisObject, "a").toString();
                Rcs.PhoneNumber senderId=Rcs.PhoneNumber.parseFrom(protoObjToBytes(param.args[1]));
                Rcs.PhoneNumber recipientId=Rcs.PhoneNumber.parseFrom(protoObjToBytes(param.args[2]));
                LogUtils.show("MessageMetadataClass senderId " + printObjToJson(senderId));
                LogUtils.show("MessageMetadataClass recipientId " +printObjToJson(recipientId));
                LogUtils.show("MessageMetadataClass messageId " +msgId);
                MsgItem msgItem = SendMsgKeyInfo.getInstance().getMsgItem(msgId, false);
                LogUtils.show("MessageMetadataClass msgItem " + msgItem);
                if(msgItem!=null && recipientId.getType()==2){
                 msgItem.setGroupId(recipientId.getNumber());
                 LogUtils.show("MessageMetadataClass groupId " +recipientId.getNumber() + " msgItem= " + msgItem);
                }
                if(param.args[6]!=null) {
                    Rcs.ActionIdItem groupContext=Rcs.ActionIdItem.parseFrom(protoObjToBytes(param.args[6]));
                    CachedGroupInfo.addDeliveredMember(groupContext.getId(), senderId.getNumber());
                    SendMsgKeyInfo.getInstance().addDeliveredMember(groupContext.getId(), senderId.getNumber());
                    LogUtils.show("MessageMetadataClass getGroupMembersMap " +CachedGroupInfo.getDeliveredGroupMembersMap());
                    LogUtils.show("MessageMetadataClass groupContext " + printObjToJson(groupContext));

                }

            }
        });
    }


    public static String printChannel(Object channel) {
        return "{ channel_class= " + channel + " authority=" + XposedHelpers.callMethod(channel, "b").toString() + " }";
    }


}

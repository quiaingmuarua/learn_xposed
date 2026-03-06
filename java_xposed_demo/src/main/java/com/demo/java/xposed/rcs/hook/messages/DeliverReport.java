package com.demo.java.xposed.rcs.hook.messages;

import android.text.TextUtils;

import com.demo.java.xposed.device.model.MessageCoreDataModel;
import com.demo.java.xposed.rcs.apiCaller.cache.CachedGroupInfo;
import com.demo.java.xposed.rcs.enums.MsgStatusEnum;
import com.demo.java.xposed.base.BaseAppHook;
import com.demo.java.xposed.rcs.model.MsgItem;
import com.demo.java.xposed.rcs.model.SendMsgKeyInfo;
import com.demo.java.xposed.device.model.SimInfoModel;
import com.demo.java.xposed.utils.LogUtils;
import com.demo.java.xposed.utils.collection.StringUtils;

import java.util.Arrays;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class DeliverReport extends BaseAppHook {


    public static void run(XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {

        LogUtils.show("DeliverReport hook run");
        ClassLoader classLoader = loadPackageParam.classLoader;
         SimInfoModel simInfoModel = SimInfoModel.getInstance(loadPackageParam.packageName);
        Class EncryptedRcsMessageConverterClass = classLoader.loadClass("azfo");
        Class RemoteChatEndpointClass = classLoader.loadClass("aolu");
        Class RcsMessageIdClass = classLoader.loadClass("asjz");
        Class ImmutableListFactoryClass = classLoader.loadClass("dhpt");
        Class MessageTyepEnumClass = classLoader.loadClass("dixy");
        Class CustomHeadersClass = classLoader.loadClass("dutd");
        XposedHelpers.findAndHookMethod(EncryptedRcsMessageConverterClass, "c", byte[].class, java.lang.String.class, RemoteChatEndpointClass, RcsMessageIdClass, ImmutableListFactoryClass, java.lang.String.class, boolean.class, MessageTyepEnumClass, boolean.class, CustomHeadersClass, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {

                super.beforeHookedMethod(param);
                byte[] content = (byte[]) param.args[0];
                String msgId = param.args[3].toString().split(":")[1];
                String receiver = param.args[4].toString();
                MsgItem msgItem = SendMsgKeyInfo.getInstance().getMsgItem(msgId, true);
                msgItem.setContent(StringUtils.bytesToString(content));
                msgItem.setSender(simInfoModel.getPhoneNumber());
                msgItem.setReceiver(receiver.replace("[", "").replace("]", ""));
                msgItem.setStatus(MsgStatusEnum.MSG_SEND.getStatus());
                SendMsgKeyInfo.getInstance().toLocalFile();
                LogUtils.printParams("EncryptedRcsMessageConverterClass ", param.args);
            }

            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
            }
        });


        Class RcsDeliveryReportProcessorClass = classLoader.loadClass("bjgo");
        Class ChatSessionMessageEvent = classLoader.loadClass("com.google.android.ims.rcsservice.chatsession.ChatSessionMessageEvent");
        XposedHelpers.findAndHookMethod(RcsDeliveryReportProcessorClass, "b", RcsMessageIdClass, ChatSessionMessageEvent, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {

                super.beforeHookedMethod(param);

            }

            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
                String msgId = param.args[0].toString().split(":")[1];
                if (param.getResult().toString().contains("SUCCESS")) {
                    MsgItem msgItem = SendMsgKeyInfo.getInstance().getMsgItem(msgId, false);
                    if (msgItem == null) {
                        LogUtils.show("RcsDeliveryReportProcessorClass no msgItem " + msgId);
                        return;
                    }
                    msgItem.setStatus(MsgStatusEnum.MSG_DELIVERED.getStatus());
                    SendMsgKeyInfo.getInstance().toLocalFile();
                }
                LogUtils.printParams("RcsDeliveryReportProcessorClass ", param.args, param.getResult());

            }
        });


        Class ChatApiSenderClass = classLoader.loadClass("bloe");

        XposedHelpers.findAndHookMethod(ChatApiSenderClass, "j", java.lang.Exception.class, RcsMessageIdClass, int.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                LogUtils.printParams("ChatApiSenderClass  exceptionToMessageStatusPlusUri ", param.args);
                String msgId = param.args[1].toString().split(":")[1];
                MsgItem msgItem = SendMsgKeyInfo.getInstance().getMsgItem(msgId, true);
                msgItem.setStatus(MsgStatusEnum.MSG_FAILED.getStatus());
                SendMsgKeyInfo.getInstance().toLocalFile();

                super.beforeHookedMethod(param);
            }

            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
            }
        });

    }
    public static void runV20250319(XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {
        LogUtils.show("runV20250319 DeliverReport");
        ClassLoader classLoader = loadPackageParam.classLoader;
        SimInfoModel simInfoModel = SimInfoModel.getInstance(loadPackageParam.packageName);
        Class optionClass = classLoader.loadClass("j$.util.Optional");
        Class avjz = classLoader.loadClass("avjz");
        Class<?> EncryptedRcsChatMessageSenderClass = classLoader.loadClass("cpzq");
        Class<?> MessageCoreDataClass = classLoader.loadClass("com.google.android.apps.messaging.shared.datamodel.data.common.MessageCoreData");

        XposedHelpers.findAndHookMethod(EncryptedRcsChatMessageSenderClass, "a", MessageCoreDataClass, avjz, optionClass, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);
                LogUtils.printParams("EncryptedRcsChatMessageSenderClass sendChatMessage beforeHookedMethod " ,param.args);
                LogUtils.show("EncryptedRcsChatMessageSenderClass sendChatMessage beforeHookedMethod " + parseMessageCoreDataNew(param.args[0]));
            }

            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
            }
        });

        Class awut = classLoader.loadClass("awut");
        Class bdkt = classLoader.loadClass("bdkt");
        Class epya = classLoader.loadClass("epya");
        Class fcge = classLoader.loadClass("fcge");
        Class EncryptedRcsMessageConverterClass = classLoader.loadClass("cabp");
        Class eokz = classLoader.loadClass("eokz");
        Class chos = classLoader.loadClass("chos");
        XposedHelpers.findAndHookMethod("com.google.android.apps.messaging.shared.datamodel.action.SendMessageAction", classLoader, "H", MessageCoreDataClass, chos, eokz, long.class, int.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);
                MessageCoreDataModel messageCoreDataModel=parseMessageCoreDataNew(param.args[0]);
                String receiver = param.args[2].toString();
                LogUtils.show("SendMessageAction H beforeHookedMethod " + messageCoreDataModel);
                LogUtils.show("SendMessageAction H beforeHookedMethod " + Arrays.toString(param.args));
                MsgItem msgItem = SendMsgKeyInfo.getInstance().getMsgItem(messageCoreDataModel.getMessageId(), true);
                msgItem.setContent(messageCoreDataModel.getText());
                msgItem.setSender(simInfoModel.getPhoneNumber());
                msgItem.setReceiver(receiver.replace("[", "").replace("]", ""));
                msgItem.setStatus(MsgStatusEnum.MSG_SEND.getStatus());
                SendMsgKeyInfo.getInstance().toLocalFile();
            }
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
            }
        });

        XposedHelpers.findAndHookMethod(EncryptedRcsMessageConverterClass, "b", byte[].class, java.lang.String.class, awut, bdkt, eokz, java.lang.String.class, boolean.class, epya, boolean.class, fcge, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);
                if (!param.args[5].toString().equals("text/plain")) {
                    LogUtils.show("EncryptedRcsMessageConverterClass  not text/plain");
                    return;
                }
                byte[] content = (byte[]) param.args[0];
                String msgId = param.args[3].toString().split(":")[1];
                String receiver = param.args[4].toString();
                MsgItem msgItem = SendMsgKeyInfo.getInstance().getMsgItem(msgId, true);
                msgItem.setContent(StringUtils.bytesToString(content));
                msgItem.setSender(simInfoModel.getPhoneNumber());
                msgItem.setReceiver(receiver.replace("[", "").replace("]", ""));
                msgItem.setStatus(MsgStatusEnum.MSG_SEND.getStatus());
                SendMsgKeyInfo.getInstance().toLocalFile();
                LogUtils.printParams("EncryptedRcsMessageConverterClass convert convert", param.args);
            }

            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
            }
        });


        Class RcsDeliveryReportProcessorClass = classLoader.loadClass("cmdx");
        Class epum = classLoader.loadClass("epum");
        Class<?> ChatSessionMessageEventClass = classLoader.loadClass("com.google.android.ims.rcsservice.chatsession.ChatSessionMessageEvent");
        XposedHelpers.findAndHookMethod(RcsDeliveryReportProcessorClass, "c", MessageCoreDataClass, ChatSessionMessageEventClass, boolean.class, long.class, int.class, epum, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);
//                String rcsMessageId = XposedHelpers.callMethod(param.args[0], "H").toString();

                //call aA
//                String senderId= XposedHelpers.callMethod(param.args[0], "aA").toString();
                LogUtils.printParams("RcsDeliveryReportProcessorClass",param.args);
                String msgId = parseMessageCoreDataNew(param.args[0]).getMessageId();
                MsgItem msgItem = SendMsgKeyInfo.getInstance().getMsgItem(msgId, false);
                LogUtils.show("RcsDeliveryReportProcessorClass msgItem " + msgItem);
                if (msgItem == null) {
                    LogUtils.show("RcsDeliveryReportProcessorClass no msgItem " + msgId);
                    return;
                }
                msgItem.setStatus(MsgStatusEnum.MSG_DELIVERED.getStatus());
                msgItem.setUpdateTime(System.currentTimeMillis());
                SendMsgKeyInfo.getInstance().toLocalFile();
                LogUtils.show("RcsDeliveryReportProcessorClass rcsMessageId" + msgId);

            }

            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
            }
        });

    }




    public static void runV240519(XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {


        ClassLoader classLoader = loadPackageParam.classLoader;
        SimInfoModel simInfoModel = SimInfoModel.getInstance(loadPackageParam.packageName);
        Class optionClass = classLoader.loadClass("j$.util.Optional");
        Class apkw = classLoader.loadClass("apkw");
        Class<?> EncryptedRcsChatMessageSenderClass = classLoader.loadClass("bcpf");
        Class bpvy = classLoader.loadClass("bpvy");
        Class dmvc = classLoader.loadClass("dmvc");
        Class aqov = classLoader.loadClass("aqov");
        Class<?> ChatApiSenderClass = classLoader.loadClass("bptx");
        Class<?> MessageCoreDataClass = classLoader.loadClass("com.google.android.apps.messaging.shared.datamodel.data.common.MessageCoreData");

        XposedHelpers.findAndHookMethod(EncryptedRcsChatMessageSenderClass, "a", MessageCoreDataClass, apkw, optionClass, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);
                LogUtils.show("EncryptedRcsChatMessageSenderClass sendChatMessage beforeHookedMethod " + parseMessageCoreData(param.args[0]));
                LogUtils.show("EncryptedRcsChatMessageSenderClass sendChatMessage parseMessageCoreData " + parseMessageCoreData(param.args[0]));
                LogUtils.show("EncryptedRcsChatMessageSenderClass sendChatMessage parseRecipients " + parseRecipients(param.args[1]));
            }

            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
            }
        });

        XposedHelpers.findAndHookMethod(ChatApiSenderClass, "o", MessageCoreDataClass, apkw, bpvy, dmvc, aqov, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);
                MessageCoreDataModel messageCoreDataModel=parseMessageCoreData(param.args[0]);
                SendMsgKeyInfo.getInstance().getMsgItem(messageCoreDataModel.getMessageId(), true);
                LogUtils.printParams("ChatApiSenderClass sendBasicText beforeHookedMethod " ,param.args);
                LogUtils.show("ChatApiSenderClass sendBasicText parseMessageCoreData " + messageCoreDataModel);
                LogUtils.show("ChatApiSenderClass sendBasicText parseRecipients " + parseRecipients(param.args[1]));
            }

            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
            }
        });

        Class<?> UnencryptedRcsChatMessageSenderClass = classLoader.loadClass("bpza");
        XposedHelpers.findAndHookMethod(UnencryptedRcsChatMessageSenderClass, "a", MessageCoreDataClass, apkw, optionClass, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);
                LogUtils.show("UnencryptedRcsChatMessageSenderClass sendChatMessage beforeHookedMethod " + parseMessageCoreData(param.args[0]));
            }

            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
            }
        });


        Class eamb = classLoader.loadClass("eamb");
        Class aqmb = classLoader.loadClass("aqmb");
        Class aviu = classLoader.loadClass("aviu");
        Class dnfs = classLoader.loadClass("dnfs");
        Class donw = classLoader.loadClass("donw");
        Class EncryptedRcsMessageConverterClass = classLoader.loadClass("bcqg");
        Class bjei = classLoader.loadClass("bjei");
        XposedHelpers.findAndHookMethod("com.google.android.apps.messaging.shared.datamodel.action.SendMessageAction", classLoader, "H", MessageCoreDataClass, bjei, dnfs, long.class, int.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);
                LogUtils.printParams("SendMessageAction H beforeHookedMethod " , param.args);
                MessageCoreDataModel messageCoreDataModel=parseMessageCoreData(param.args[0]);
                String receiver = param.args[2].toString();
                LogUtils.show("SendMessageAction H beforeHookedMethod " + messageCoreDataModel);
                MsgItem msgItem = SendMsgKeyInfo.getInstance().getMsgItem(messageCoreDataModel.getMessageId(), true);
                msgItem.setContent(messageCoreDataModel.getText());
                msgItem.setSender(simInfoModel.getPhoneNumber());
                msgItem.setReceiver(receiver.replace("[", "").replace("]", ""));
                msgItem.setStatus(MsgStatusEnum.MSG_SEND.getStatus());
                if(!TextUtils.isEmpty(msgItem.getGroupId())){
                    CachedGroupInfo.addSentMembers(msgItem.getGroupId(), Arrays.asList(receiver.replace("[", "").replace("]", "").split(",")));
                    LogUtils.show("SendMessageAction H beforeHookedMethod getSentGroupMembersMap= " + CachedGroupInfo.getSentGroupMembersMap());
                }
                SendMsgKeyInfo.getInstance().toLocalFile();
            }
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
            }
        });

        XposedHelpers.findAndHookMethod(EncryptedRcsMessageConverterClass, "c", byte[].class, java.lang.String.class, aqmb, aviu, dnfs, java.lang.String.class, boolean.class, donw, boolean.class, eamb, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);
                if (!param.args[5].toString().equals("text/plain")) {
                    LogUtils.show("EncryptedRcsMessageConverterClass  not text/plain");
                    return;
                }
                byte[] content = (byte[]) param.args[0];
                String msgId = param.args[3].toString().split(":")[1];
                String receiver = param.args[4].toString();
                MsgItem msgItem = SendMsgKeyInfo.getInstance().getMsgItem(msgId, true);
                msgItem.setContent(StringUtils.bytesToString(content));
                msgItem.setSender(simInfoModel.getPhoneNumber());
                msgItem.setReceiver(receiver.replace("[", "").replace("]", ""));
                msgItem.setStatus(MsgStatusEnum.MSG_SEND.getStatus());
                SendMsgKeyInfo.getInstance().toLocalFile();
                LogUtils.printParams("EncryptedRcsMessageConverterClass convert convert", param.args);
            }

            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
            }
        });


        Class RcsDeliveryReportProcessorClass = classLoader.loadClass("bnfd");
        Class ChatSessionMessageEvent = classLoader.loadClass("com.google.android.ims.rcsservice.chatsession.ChatSessionMessageEvent");
        Class aviuClass = classLoader.loadClass("aviu");
        Class dojuClass = classLoader.loadClass("doju");
        Class<?> ChatSessionMessageEventClass = classLoader.loadClass("com.google.android.ims.rcsservice.chatsession.ChatSessionMessageEvent");
        XposedHelpers.findAndHookMethod(RcsDeliveryReportProcessorClass, "c", MessageCoreDataClass, ChatSessionMessageEventClass, boolean.class, long.class, int.class, dojuClass, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);
                LogUtils.printParams("RcsDeliveryReportProcessorClass ",parseMessageCoreData(param.args[0]));
//                String rcsMessageId = XposedHelpers.callMethod(param.args[0], "H").toString();

                //call aA
//                String senderId= XposedHelpers.callMethod(param.args[0], "aA").toString();
                String msgId = parseMessageCoreData(param.args[0]).getMessageId();
                MsgItem msgItem = SendMsgKeyInfo.getInstance().getMsgItem(msgId, false);
                LogUtils.show("RcsDeliveryReportProcessorClass msgItem " + msgItem);
                if (msgItem == null) {
                    LogUtils.show("RcsDeliveryReportProcessorClass no msgItem " + msgId);
                    return;
                }
                msgItem.setStatus(MsgStatusEnum.MSG_DELIVERED.getStatus());
                msgItem.setUpdateTime(System.currentTimeMillis());
                SendMsgKeyInfo.getInstance().toLocalFile();
                LogUtils.show("RcsDeliveryReportProcessorClass rcsMessageId " + msgId);

            }

            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
            }
        });

    }
    public static MessageCoreDataModel parseMessageCoreDataNew(Object object) {
        String rcsMessageId = XposedHelpers.callMethod(object, "F").toString();
        String msgId = rcsMessageId.split(":")[1];
        //call ax
        String text = XposedHelpers.callMethod(object, "aA").toString();

        //call aA
        String senderId= XposedHelpers.callMethod(object, "aC").toString();
        return new MessageCoreDataModel(msgId, text,senderId);
    }



    public static MessageCoreDataModel parseMessageCoreData(Object object) {
        String rcsMessageId = XposedHelpers.callMethod(object, "H").toString();
        String msgId = rcsMessageId.split(":")[1];
        //call ax
        String text = XposedHelpers.callMethod(object, "ax").toString();

        //call aA
        String senderId= XposedHelpers.callMethod(object, "aA").toString();
        return new MessageCoreDataModel(msgId, text,senderId);
    }

    public static  String parseRecipients(Object object){
            String descriptor= XposedHelpers.getObjectField(object, "a").toString();
//            Object aqnl= XposedHelpers.getObjectField(object, "c");
            return "descriptor= "+descriptor;
    }




}


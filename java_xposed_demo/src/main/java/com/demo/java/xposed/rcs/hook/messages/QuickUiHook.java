package com.demo.java.xposed.rcs.hook.messages;

import android.text.TextUtils;

import com.demo.java.xposed.base.BaseAppHook;
import com.demo.java.xposed.rcs.model.RegisterKeyInfo;
import com.demo.java.xposed.rcs.model.SendMsgKeyInfo;
import com.demo.java.xposed.utils.Base64Utils;
import com.demo.java.xposed.utils.LogUtils;
import com.example.sekiro.messages.shared.CachedGroupInfo;

import java.lang.reflect.Method;
import java.util.Set;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class QuickUiHook  extends BaseAppHook {

    public static void run(XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {
        groupServiceAction(loadPackageParam);
        ui(loadPackageParam);
        capabilities(loadPackageParam);
        abortUpdate(loadPackageParam);

    }

    public static void ui(XC_LoadPackage.LoadPackageParam loadPackageParam) throws ClassNotFoundException {
        ClassLoader classLoader = loadPackageParam.classLoader;
        Class ConversationIdType = XposedHelpers.findClass("com.google.android.apps.messaging.shared.datamodel.data.datatypes.ConversationIdType", classLoader);
        Class ConversationId = XposedHelpers.findClass("com.google.android.apps.messaging.shared.api.messaging.conversation.ConversationId", classLoader);
        Class MessageIdType = XposedHelpers.findClass("com.google.android.apps.messaging.shared.datamodel.data.datatypes.MessageIdType", classLoader);
        Class<?> ConversationArchievStatusClass = classLoader.loadClass("bbsg");
        XposedHelpers.findAndHookConstructor("com.google.android.apps.messaging.home.select.SelectedConversation", classLoader, ConversationIdType, ConversationId, MessageIdType, long.class, long.class, ConversationArchievStatusClass, int.class, boolean.class, boolean.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, boolean.class, boolean.class, int.class, java.lang.String.class, int.class, java.lang.String.class, boolean.class, int.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);
                LogUtils.printParams("QuickSendHook ", param.args);
            }

            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
            }
        });

        Class<?> ConversationListAdapteClass = classLoader.loadClass("acuq");
        XposedHelpers.findAndHookMethod(ConversationListAdapteClass, "I", int.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);
                LogUtils.printParams("QuickSendHook ConversationListAdapteClass", param.args);
            }
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
                LogUtils.printParams("QuickSendHook ConversationListAdapteClass after", param.getResult());
            }
        });

        Class<?> TaskExecutorServiceClass = classLoader.loadClass("drfc");
        Class<?> dljnClass = classLoader.loadClass("dljn");
        Class<?> dlckClass = classLoader.loadClass("dlck");
        Class<?> acwoClass = classLoader.loadClass("acwo");
        Class<?> spyClass = classLoader.loadClass("spy");
        Class<?> eeozClass = classLoader.loadClass("eeoz");
        Class<?> optionClass = classLoader.loadClass("j$.util.Optional");
        XposedHelpers.findAndHookConstructor(ConversationListAdapteClass, android.content.Context.class,TaskExecutorServiceClass, dljnClass, dlckClass, acwoClass, spyClass, eeozClass, eeozClass, eeozClass, optionClass, eeozClass, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);
            }
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
                LogUtils.show("ConversationListAdapteClass constructor hooked this="+param.thisObject);
            }
        });


    }

    public static void abortUpdate(XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {
        ClassLoader classLoader = loadPackageParam.classLoader;
        Class<?> clazz = XposedHelpers.findClass("dhtm", classLoader);
        Method targetMethod = clazz.getDeclaredMethod("a");

        // 替换方法逻辑，完全不执行原始代码
        XposedBridge.hookMethod(targetMethod, new XC_MethodReplacement() {
            @Override
            protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {
                // 这里是你自定义的行为
                LogUtils.show("🚫 成功拦截 dhtm.a()，原方法已被替换为空实现");
                return null; // 方法返回类型是 void，用 null 即可
            }
        });




    }


    public static void capabilities(XC_LoadPackage.LoadPackageParam loadPackageParam) {
        ClassLoader classLoader = loadPackageParam.classLoader;
        XposedHelpers.findAndHookMethod("bqgv", classLoader, "apply", java.lang.Object.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);
                Object phonenumberItem = XposedHelpers.getObjectField(param.args[0], "b");
                String phoneNumber = XposedHelpers.getObjectField(phonenumberItem, "b").toString();
                Set capabilitiesSet = (Set) XposedHelpers.callMethod(param.args[0], "a");
                LogUtils.show("TachygramNetworkCapabilitiesProvider lambda$getCapabilities$1 apply" + param.args[0]);
                LogUtils.show("TachygramNetworkCapabilitiesProvider phoneNumber", phoneNumber + "  capabilitiesSize=" + capabilitiesSet);
                RegisterKeyInfo.getInstance().addStatusList("PONG");
                if (!capabilitiesSet.isEmpty()) {
                    SendMsgKeyInfo.getInstance().addPhoneRcsStatus(phoneNumber, "rcs");
                } else {
                    SendMsgKeyInfo.getInstance().addPhoneRcsStatus(phoneNumber, "no_rcs");
                }
            }

            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
            }
        });
    }

    public static  void groupServiceAction(XC_LoadPackage.LoadPackageParam loadPackageParam) {

        ClassLoader classLoader = loadPackageParam.classLoader;
        try {
            Class<?> bqudClass = classLoader.loadClass("bqud");
            Class<?> GroupIdClass = classLoader.loadClass("dscm");
            Class<?> ContinuationUnitClass = classLoader.loadClass("eetw");
            XposedHelpers.findAndHookConstructor("bqub", classLoader, bqudClass, GroupIdClass, java.lang.Iterable.class, ContinuationUnitClass, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    super.beforeHookedMethod(param);
                    LogUtils.printParams("QuickSendHook groupServiceAction  removeGroupMembers= ", param.args);
                   Object phoneNumbers=param.args[2];
                    LogUtils.printParams("QuickSendHook groupServiceAction  removeGroupMembers= ", protoObjToHex(XposedHelpers.callMethod(phoneNumbers,"get",0)));
                }
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    super.afterHookedMethod(param);
                }
            });


            XposedHelpers.findAndHookMethod("axvq", classLoader, "aa", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    super.beforeHookedMethod(param);
                }

                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    super.afterHookedMethod(param);
                    Object rcs_group_id = param.getResult();
                    LogUtils.printParams("ConversationsTable rcs_group_id=", rcs_group_id);
                    LogUtils.printParams("QuickSendHook ConversationsTable this= ",param.thisObject);
                    if (rcs_group_id != null && !TextUtils.isEmpty(rcs_group_id.toString()) && XposedHelpers.getObjectField(param.thisObject,"aa")!=null) {
                        String groupId = rcs_group_id.toString();
                        String rcs_conference_uri= XposedHelpers.getObjectField(param.thisObject,"aa").toString();
                        if(TextUtils.isEmpty(rcs_conference_uri))return;
                        String conversationId=XposedHelpers.getObjectField(param.thisObject,"a").toString().trim();
                        LogUtils.printParams("ConversationsTable _id=", conversationId);
                        String base64Str= rcs_conference_uri.split("=")[1];
                        LogUtils.printParams("ConversationsTable rcs_conference_uri base64Str ", base64Str);
                        CachedGroupInfo.updateCurGroupId(groupId);
                        CachedGroupInfo.getOrCreate(groupId).setConversationId(conversationId);
                        CachedGroupInfo.setRrsConferencePropertiesBytes(groupId,Base64Utils.safeDecodeBase64(base64Str));
                    }

                }
            });
        } catch (ClassNotFoundException e) {
           LogUtils.printStackErrInfo("groupServiceAction",e);
        }

    }

}

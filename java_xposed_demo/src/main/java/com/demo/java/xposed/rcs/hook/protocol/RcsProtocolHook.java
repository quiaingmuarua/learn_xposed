package com.demo.java.xposed.rcs.hook.protocol;

import com.demo.java.xposed.device.model.MessageCoreDataModel;
import com.demo.java.xposed.base.BaseAppHook;
import com.demo.java.xposed.device.PluginInit;
import com.demo.java.xposed.rcs.apiCaller.cache.CachedUnaryRpc;
import com.demo.java.xposed.rcs.apiCaller.cache.XposedClassCacher;
import com.demo.java.xposed.rcs.model.RegisterKeyInfo;
import com.demo.java.xposed.utils.LogUtils;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class RcsProtocolHook extends BaseAppHook {

    public static void run(XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {
        register(loadPackageParam);
        groupManage(loadPackageParam);
        if (!PluginInit.isDebug) {
            return;

        }
        lookupRegistered(loadPackageParam);

        spam(loadPackageParam);

    }

    public static void groupManage(XC_LoadPackage.LoadPackageParam loadPackageParam) {
        ClassLoader classLoader = loadPackageParam.classLoader;

        Class<?> ActionIdItemClass = null;
        try {
            ActionIdItemClass = classLoader.loadClass("ebqf");
            Class optionClass = classLoader.loadClass("j$.util.Optional");

            Class<?> GroupIdClass = classLoader.loadClass("dscm");
            Class<?> GroupPropertiesClass = classLoader.loadClass("dscu");

            Class<?> dnhjClass = classLoader.loadClass("dnhj");
            XposedHelpers.findAndHookConstructor(GroupIdClass, ActionIdItemClass, optionClass, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    super.beforeHookedMethod(param);
                }

                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    super.afterHookedMethod(param);
                    LogUtils.printParams("groupManage ActionIdItemClass =", protoObjToHex(param.args[0]));
                    LogUtils.printParams("groupManage GroupId ", param.args);
                }
            });

            XposedHelpers.findAndHookConstructor("dsce", classLoader, GroupIdClass, GroupPropertiesClass, dnhjClass, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    super.beforeHookedMethod(param);
                }

                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    super.afterHookedMethod(param);
                    LogUtils.show("groupManage  CreateGroupRequest= " + param.thisObject);
                }
            });
        } catch (ClassNotFoundException e) {
            LogUtils.printStackErrInfo("groupManage Exception", e);
        }

    }


    public static  void lookupRegistered(XC_LoadPackage.LoadPackageParam loadPackageParam) {
        try {
            ClassLoader classLoader = loadPackageParam.classLoader;
            XposedHelpers.findAndHookMethod("ebda", classLoader, "b", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    super.beforeHookedMethod(param);
                }
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    super.afterHookedMethod(param);
                    LogUtils.printParams("LookupRegistered hook ",param.args,param.getResult());
                }
            });


            Class<?> ContactSelectionTrackerImplClass = classLoader.loadClass("bxbn");

            Class<?> ChipDataClass = classLoader.loadClass("com.google.android.apps.messaging.startchat.chip.ChipData");
            XposedHelpers.findAndHookMethod(ContactSelectionTrackerImplClass, "c", ChipDataClass, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    super.beforeHookedMethod(param);
                }
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    super.afterHookedMethod(param);
                    LogUtils.printParams("LookupRegistered ContactSelectionTrackerImplClass ",param.args,param.getResult() );


                }
            });

            Class<?> LookupRegisteredRequestBuilderClass = classLoader.loadClass("ebmf");
            Class<?> ebqfClass = classLoader.loadClass("ebqf");
            XposedHelpers.findAndHookMethod(LookupRegisteredRequestBuilderClass, "a", ebqfClass, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    super.beforeHookedMethod(param);
                    LogUtils.show("LookupRegisteredRequestBuilderClass ebqf= " +  protoObjToHex(param.args[0]));
                }
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    super.afterHookedMethod(param);
                }
            });

            Class<?> RetrieveRegistrationIdHandlerClass = classLoader.loadClass("bkjr");
            Class<?> TachyonRegistrationToken = XposedClassCacher.TachyonRegistrationToken;
            XposedHelpers.findAndHookMethod(RetrieveRegistrationIdHandlerClass, "c", TachyonRegistrationToken, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    super.beforeHookedMethod(param);
                    String TachyonRegistrationToken=protoObjToHex(param.args[0]);
                    LogUtils.show("LookupRegistered RetrieveRegistrationIdHandlerClass TachyonRegistrationtoken " + TachyonRegistrationToken );
                    CachedUnaryRpc.cacheTokenFromHex(TachyonRegistrationToken);
                }
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    super.afterHookedMethod(param);
                }
            });
        }catch (Exception e){
            LogUtils.printStackErrInfo("LookupRegistered Exception",e);
        }

    }


    public static void register(XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {
        ClassLoader classLoader = loadPackageParam.classLoader;
        XposedHelpers.findAndHookMethod("bjnm", classLoader, "a", java.lang.Object.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);
                LogUtils.show("RegistrationProvider Failed to register with Tachyon err= " + param.args[0]);
                RegisterKeyInfo.getInstance().addStatusList("NOT_FOUND");
            }

            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
            }
        });


        Class<?> bktz = classLoader.loadClass("bktz");
        Class<?> eggt = classLoader.loadClass("eggt");
        XposedHelpers.findAndHookMethod("agny", classLoader, "g", bktz, eggt, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);
                LogUtils.printParams("RcsProvisioningTriggerImpl getSimIdFromRegistrationInfo= ", param.args, param.getResult());

            }

            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
            }
        });

    }

    public  static  void spam(XC_LoadPackage.LoadPackageParam loadPackageParam) {
        try {
            ClassLoader classLoader = loadPackageParam.classLoader;
            Class<?> LinguaSpamEnforcementClass = classLoader.loadClass("bvpj");
            XposedHelpers.findAndHookMethod(LinguaSpamEnforcementClass, "b", java.lang.Object.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    super.beforeHookedMethod(param);
                }

                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    super.afterHookedMethod(param);
                    LogUtils.printParams("LinguaSpamEnforcementClass args",param.args);
                    String msgId=XposedHelpers.getObjectField(param.thisObject,"c").toString();
                    Float score=XposedHelpers.getFloatField(param.thisObject,"a");
                    Class<?> IncomingMessageSpamCheckParamsClass = classLoader.loadClass("bvii");
                    if (param.args[0]!=null && param.args[0].toString().contains("IncomingMessageSpamCheckParams")){

                        Object model = IncomingMessageSpamCheckParamsClass.cast(param.args[0]);
                        Object fieldValue = XposedHelpers.getObjectField(model, "a");
                        LogUtils.printParams("LinguaSpamEnforcementClass  spam result= " ,parseMessageCoreData(fieldValue),score);
                    }else {
                        LogUtils.printParams("LinguaSpamEnforcementClass hook safe msg" ,msgId,score);
                    }

                }
            });
        } catch (Exception e) {

            LogUtils.show("SpamHook Exception" + e);
        }

    }



    private static MessageCoreDataModel parseMessageCoreData(Object object) {
        String rcsMessageId = XposedHelpers.callMethod(object, "H").toString();
        String msgId = rcsMessageId.split(":")[1];
        //call ax
        String text = XposedHelpers.callMethod(object, "ax").toString();

        //call aA
        String senderId= XposedHelpers.callMethod(object, "aA").toString();
        return new MessageCoreDataModel(msgId, text,senderId);
    }


}

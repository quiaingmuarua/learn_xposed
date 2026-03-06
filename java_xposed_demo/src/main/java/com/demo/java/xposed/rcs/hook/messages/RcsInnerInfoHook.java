package com.demo.java.xposed.rcs.hook.messages;

import com.demo.java.xposed.rcs.apiCaller.cache.CachedGroupInfo;
import com.demo.java.xposed.base.BaseAppHook;
import com.demo.java.xposed.utils.LogUtils;
import com.demo.java.xposed.utils.PrintStack;

import java.util.Base64;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class RcsInnerInfoHook  extends BaseAppHook {

    public static void run(XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {


        try {

            ClassLoader classLoader = loadPackageParam.classLoader;

            Class<?> SimIdClass = classLoader.loadClass("cjze");
            XposedHelpers.findAndHookConstructor(SimIdClass, java.lang.String.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    super.beforeHookedMethod(param);
                    LogUtils.printParams("RcsInnerInfoHook SimIdClass", param.args);
                }

                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {

                    super.afterHookedMethod(param);
                }
            });
            Class<?> SimPreferencesClass = classLoader.loadClass("ckyn");

            XposedHelpers.findAndHookMethod(SimPreferencesClass, "c", java.lang.String.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    super.beforeHookedMethod(param);
                }
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    super.afterHookedMethod(param);
//                    LogUtils.printParams("SimPreferencesClass deserializeSimSubscriptionInfo = ",param.args[0].toString().replace("\n","") );
//                    LogUtils.printParams("SimPreferencesClass deserializeSimSubscriptionInfo string ", Base64Utils.safeDecodeBase64ToString(param.args[0].toString().replace("\n","")) );
                }
            });

            Class<?> IdentityMappingInfoClass = classLoader.loadClass("cjzn");

            Class<?> FormattedE164PhoneNumberClass = classLoader.loadClass("cjzb");

            XposedHelpers.findAndHookConstructor(IdentityMappingInfoClass,SimIdClass, FormattedE164PhoneNumberClass, int.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    super.beforeHookedMethod(param);
                    LogUtils.printParams("RcsInnerInfoHook IdentityMappingInfoClass " ,param.args);
                }
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    super.afterHookedMethod(param);
                }
            });
            XposedHelpers.findAndHookConstructor(FormattedE164PhoneNumberClass, java.lang.String.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    super.beforeHookedMethod(param);
                    LogUtils.printParams("RcsInnerInfoHook FormattedE164PhoneNumberClass " ,param.args);
                }
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    super.afterHookedMethod(param);
                }
            });

            XposedHelpers.findAndHookConstructor("bvwc", classLoader, java.lang.String.class, java.lang.String.class, java.lang.String.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    super.beforeHookedMethod(param);
                }
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    super.afterHookedMethod(param);
                    LogUtils.show("PhoneNumber "+param.thisObject);
                }
            });

            XposedHelpers.findAndHookMethod("ckyn", classLoader, "k", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    super.beforeHookedMethod(param);
                }
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    super.afterHookedMethod(param);
                    LogUtils.show(" PhoneNumber ckyn  "+ param.getResult());
                }
            });

            XposedHelpers.findAndHookMethod("bjmo", classLoader, "b", java.lang.String.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    super.beforeHookedMethod(param);
                    ConcurrentMap concurrentMap= (ConcurrentMap) XposedHelpers.getObjectField(param.thisObject,"f");
                    LogUtils.show("PhoneRegistrationProviderCache concurrentMap= " +concurrentMap.get(param.args[0]));

                }
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    super.afterHookedMethod(param);
                    LogUtils.printParams("PhoneRegistrationProviderCache createProvider",param.args,param.getResult());
                }
            });
            Class optionClass = classLoader.loadClass("j$.util.Optional");

            XposedHelpers.findAndHookConstructor("ckbs", classLoader, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class,optionClass, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    super.beforeHookedMethod(param);

                }
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    super.afterHookedMethod(param);
                    LogUtils.show("DroidGuardContentBinding hook "+param.thisObject);
                }
            });


            XposedHelpers.findAndHookMethod("clee", classLoader, "a", android.content.Context.class, java.lang.String.class, int.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    super.beforeHookedMethod(param);
                }
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    super.afterHookedMethod(param);
                    LogUtils.printParams("clee MEI from device hook ",param.args ,param.getResult());
                }
            });

            XposedHelpers.findAndHookMethod("bxud", classLoader, "t", android.os.Bundle.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    super.beforeHookedMethod(param);
                    LogUtils.printParams("RcsSettingsFragmentV2Peer ",param.args);
                }
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    super.afterHookedMethod(param);
                }
            });

            hookNetWork(loadPackageParam);

            Class<?> BuglePhoneNumberUtilsClass = classLoader.loadClass("buvl");

            Class<?> drsfClass = classLoader.loadClass("drsf");
            XposedHelpers.findAndHookMethod(BuglePhoneNumberUtilsClass, "x", java.lang.String.class, drsfClass, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    super.beforeHookedMethod(param);
//                    LogUtils.printParams("BuglePhoneNumberUtilsClass x", param.args);
                }

                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    super.afterHookedMethod(param);
                }
            });

            XposedHelpers.findAndHookMethod(BuglePhoneNumberUtilsClass, "s", java.lang.String.class, java.lang.String.class, java.lang.String.class, boolean.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    super.beforeHookedMethod(param);
                    LogUtils.printParams("BuglePhoneNumberUtilsClass s", param.args);

                }

                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    super.afterHookedMethod(param);
                }
            });

            Class ConversationIdType=classLoader.loadClass("com.google.android.apps.messaging.shared.datamodel.data.datatypes.ConversationIdType");
            Class auzo=classLoader.loadClass("auzo");
            Class eetw=classLoader.loadClass("eetw");
            XposedHelpers.findAndHookMethod("bcjs", classLoader, "h", ConversationIdType, auzo, java.util.Set.class, eetw, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    super.beforeHookedMethod(param);
                }
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    super.afterHookedMethod(param);
                    LogUtils.printParams("ConversationE2eeStatusImpl bcjs h", param.args,param.getResult());
                    if(param.args[0]!=null){
                        String conversationId=  param.args[0].toString().trim();
                        LogUtils.show("ConversationE2eeStatusImpl conversationId"+ conversationId);
                        for (Object number : (Set) param.args[2]) {
                            Rcs.PhoneNumber phoneNumber=Rcs.PhoneNumber.parseFrom(safeToByteArray(number));
//                            LogUtils.show("ConversationE2eeStatusImpl number"+ printObjToJson(phoneNumber));
                            CachedGroupInfo.getFromConversationId(conversationId).addGroupMember(phoneNumber.getNumber());
                        }
                    }

                    LogUtils.show("ConversationE2eeStatusImpl groupMap "+CachedGroupInfo.toCachJsonString());
                }
            });


        } catch (Exception e) {
            PrintStack.printStackErrInfo("RcsInnerInfoHook", e);

        }

    }


    public static void hookNetWork (XC_LoadPackage.LoadPackageParam loadPackageParam) throws ClassNotFoundException {
        ClassLoader classLoader = loadPackageParam.classLoader;
        Class<?> btlqClass = classLoader.loadClass("btlq");
        XposedHelpers.findAndHookMethod("orh", classLoader, "onCapabilitiesChanged", android.net.Network.class, android.net.NetworkCapabilities.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);
                LogUtils.printParams("orh onCapabilitiesChanged", param.args);
            }

            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
            }
        });

        XposedHelpers.findAndHookConstructor("btlb", classLoader, java.lang.Integer.class, btlqClass, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);
                LogUtils.printParams("btlb onCapabilitiesChanged", param.args);
            }

            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
            }
        });
    }


    public static byte[] safeBase64Decode(String input) {
        // 仅保留 Base64 允许的字符
        String cleaned = input.replaceAll("[^A-Za-z0-9+/=]", "");
        try {
            return Base64.getDecoder().decode(cleaned);
        } catch (IllegalArgumentException e) {
            return new byte[0]; // 返回空结果，避免异常
        }
    }


}


//RegistrationProvider: Got StatusRuntimeException for RegisterRefresh
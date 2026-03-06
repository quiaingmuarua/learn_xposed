package com.demo.java.xposed.rcs.hook.messages;

import com.demo.java.xposed.rcs.enums.RcsInvalidStatusEnum;
import com.demo.java.xposed.base.BaseAppHook;
import com.demo.java.xposed.rcs.model.KeyCommonInfo;
import com.demo.java.xposed.rcs.model.KeyInfo;
import com.demo.java.xposed.rcs.model.RegisterKeyInfo;
import com.demo.java.xposed.utils.LogUtils;
import com.demo.java.xposed.utils.PrintStack;

import java.util.Arrays;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class RcsStatusHook extends BaseAppHook {



    public static void run(XC_LoadPackage.LoadPackageParam loadPackageParam) {




        ClassLoader classLoader = loadPackageParam.classLoader;

        try {
            Class RcsInvalidStatusEnumClass=classLoader.loadClass("dkxr");
            Class RcsSettingsFragmentV2PeerClass=classLoader.loadClass("btfr");

            XposedHelpers.findAndHookMethod(RcsSettingsFragmentV2PeerClass, "b",RcsInvalidStatusEnumClass, java.lang.String.class, boolean.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {

                    super.beforeHookedMethod(param);
                }
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    super.afterHookedMethod(param);
                    LogUtils.printParams("RcsSettingsFragmentV2PeerClass afterHookedMethod ,", RcsInvalidStatusEnum.fromValue(Integer.parseInt(param.args[0].toString())),param.args,param.getResult());

                }
            });

            XposedHelpers.findAndHookMethod(RcsSettingsFragmentV2PeerClass, "o", RcsInvalidStatusEnumClass, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    super.beforeHookedMethod(param);
                }
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    KeyInfo.printLog(new KeyInfo(KeyCommonInfo.Tag.info, String.valueOf(RcsInvalidStatusEnum.fromValue(Integer.parseInt(param.args[0].toString())))) );
                    LogUtils.printParams("RcsSettingsFragmentV2PeerClass canDisplayRcsChatsStatus ,", RcsInvalidStatusEnum.fromValue(Integer.parseInt(param.args[0].toString())));
                    String statusStr =RcsInvalidStatusEnum.fromValue(Integer.parseInt(param.args[0].toString())).toString();
                    RegisterKeyInfo.getInstance().setDisplayRcsChatsStatus(statusStr);
                    RegisterKeyInfo.getInstance().addStatusList(statusStr);

                    super.afterHookedMethod(param);
                }
            });


            XposedHelpers.findAndHookMethod(RcsInvalidStatusEnumClass, "toString", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    super.beforeHookedMethod(param);
                }
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    super.afterHookedMethod(param);
                    LogUtils.printParams("RcsInvalidStatusEnumClass toString ,",RcsInvalidStatusEnum.fromValue(Integer.parseInt(param.getResult().toString())));
                    RegisterKeyInfo.getInstance().setDisplayRcsChatsStatus(RcsInvalidStatusEnum.fromValue(Integer.parseInt(param.getResult().toString())).toString());

                }
            });



        }catch ( Exception e){
            PrintStack.printStackErrInfo("RcsStatusHook",e);
        }



    }

    public static void runV20250319(XC_LoadPackage.LoadPackageParam loadPackageParam){
        LogUtils.show("DeliverReport runV20250319");
        ClassLoader classLoader = loadPackageParam.classLoader;
        Class RcsInvalidStatusEnumClass= null;
        try {
            RcsInvalidStatusEnumClass = classLoader.loadClass("esbl");
            XposedHelpers.findAndHookMethod(RcsInvalidStatusEnumClass, "toString", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    super.beforeHookedMethod(param);
                }

                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    super.afterHookedMethod(param);
                    LogUtils.printParams("RcsStatusHook RcsInvalidStatusEnumClass toString ,", RcsInvalidStatusEnum.fromValue(Integer.parseInt(param.getResult().toString())));
                    String statusStr=RcsInvalidStatusEnum.fromValue(Integer.parseInt(param.getResult().toString())).toString();
                    RegisterKeyInfo.getInstance().setDisplayRcsChatsStatus(statusStr);
                    RegisterKeyInfo.getInstance().addStatusList(statusStr);

                }
            });
        } catch (Exception e) {

            PrintStack.printStackErrInfo("RcsStatusHook", e);

        }



    }

    public static void runV240519(XC_LoadPackage.LoadPackageParam loadPackageParam) {
        LogUtils.show("DeliverReport runV240519");
        ClassLoader classLoader = loadPackageParam.classLoader;

        try {

            Class RcsInvalidStatusEnumClass=classLoader.loadClass("dqmv");

            XposedHelpers.findAndHookMethod(RcsInvalidStatusEnumClass, "toString", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    super.beforeHookedMethod(param);
                }

                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    super.afterHookedMethod(param);
                    LogUtils.printParams("RcsStatusHook RcsInvalidStatusEnumClass toString ,", RcsInvalidStatusEnum.fromValue(Integer.parseInt(param.getResult().toString())));
                    String statusStr=RcsInvalidStatusEnum.fromValue(Integer.parseInt(param.getResult().toString())).toString();
                    RegisterKeyInfo.getInstance().setDisplayRcsChatsStatus(statusStr);
                    RegisterKeyInfo.getInstance().addStatusList(statusStr);

                }
            });


            XposedHelpers.findAndHookMethod("apqs", classLoader, "apply", java.lang.Object.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    LogUtils.printParams("RcsStatusHook apqs apply beforeHookedMethod",param.args);
                    super.beforeHookedMethod(param);
                }
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    super.afterHookedMethod(param);
                }
            });

            XposedHelpers.findAndHookMethod("atrk", classLoader, "apply", java.lang.Object.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    super.beforeHookedMethod(param);
                }
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    super.afterHookedMethod(param);
                    Object result=param.getResult();
                    if (result==null || result.toString().equals("Optional.empty")){
                        LogUtils.show("RcsStatusHook invalid_rcs_status " +"RCS status is NULL");

                    }else {
                        LogUtils.show("RcsStatusHook status result= " +result);

                    }

                }
            });

            Class<?> EtouffeeStateMachineClass = classLoader.loadClass("bcoa");

            Class<?> bjzmClass = classLoader.loadClass("bjzm");
            XposedHelpers.findAndHookMethod(EtouffeeStateMachineClass, "k", java.lang.String.class, bjzmClass, bjzmClass, int.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    super.beforeHookedMethod(param);
//                    LogUtils.printParams("EtouffeeStateMachineClass k",param.args);
//                    RegisterKeyInfo.getInstance().addStatusList(XposedHelpers.callMethod(param.args[2],"name").toString());

                }
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    super.afterHookedMethod(param);
                }
            });


            Class<?> bderClass = classLoader.loadClass("bder");
            XposedHelpers.findAndHookConstructor("bcno", classLoader, EtouffeeStateMachineClass, bderClass, bjzmClass, bjzmClass, int.class, java.lang.String.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    super.beforeHookedMethod(param);
//                    LogUtils.printParams("EtouffeeStateMachineClass  e2ee",param.args);
//                    String statusStr= String.valueOf(ProvinsionStatusEnum.fromValue(Integer.parseInt(param.args[1].toString())));
//                    LogUtils.show("EtouffeeStateMachineClass New Etouffee status " + statusStr);
//                    RegisterKeyInfo.getInstance().addStatusList(statusStr);

                }
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    super.afterHookedMethod(param);
                }
            });

            XposedHelpers.findAndHookMethod("bjnz", classLoader, "a", java.lang.Object.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    super.beforeHookedMethod(param);
                    LogUtils.printParams("RegistrationProvider bjnz",param.args);
                    if (Arrays.toString(param.args).contains("NOT_FOUND")){
                        RegisterKeyInfo.getInstance().addStatusList("NOT_FOUND");
                    }
                }
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    super.afterHookedMethod(param);
                }
            });


        } catch (Exception e) {

            PrintStack.printStackErrInfo("RcsStatusHook", e);
        }


    }



}

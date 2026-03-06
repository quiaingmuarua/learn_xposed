package com.demo.java.xposed.rcs.hook.messages;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;

import com.demo.java.xposed.rcs.enums.MSISDN_STATE_ENUM;
import com.demo.java.xposed.base.BaseAppHook;
import com.demo.java.xposed.rcs.hook.common.GmsCommonHook;
import com.demo.java.xposed.rcs.model.KeyCommonInfo;
import com.demo.java.xposed.rcs.model.KeyInfo;
import com.demo.java.xposed.rcs.model.RegisterKeyInfo;
import com.demo.java.xposed.utils.LogUtils;
import com.demo.java.xposed.utils.PrintStack;
import com.demo.java.xposed.utils.collection.StringUtils;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class RegisterHook extends BaseAppHook {


    public static void run(XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {
        LogUtils.show("RegisterHook run");
        ClassLoader classLoader = loadPackageParam.classLoader;
        try {

            //com.google.android.gms.droidguard.DroidGuardResultsRequest
            Class DroidGuardResultsRequestClass = classLoader.loadClass("com.google.android.gms.droidguard.DroidGuardResultsRequest");
            LogUtils.show("RegisterHook droid_guard_sign CommonMessageClass= " + CommonMessageClass.droidGuardSingClass + " droidGuardSingMethod= " + CommonMessageClass.droidGuardSingMethod);

            XposedHelpers.findAndHookMethod(CommonMessageClass.droidGuardSingClass, CommonMessageClass.droidGuardSingMethod, java.lang.String.class, java.util.Map.class, DroidGuardResultsRequestClass, new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            super.beforeHookedMethod(param);
                            try {
                                LogUtils.show("droid_guard_sign class=" +CommonMessageClass.droidGuardSingClass + " method=" +CommonMessageClass.droidGuardSingMethod);
                                LogUtils.printParams("droid_guard_sign start_mock", param.args);
                                Map<String, String> mapParams = (Map<String, String>) param.args[1];
                                String method= (String) param.args[0];
                                if (!TextUtils.isEmpty(method)){
                                    mapParams.put("my_method",method);
                                    param.args[1]=mapParams;
                                }
                            } catch (Exception e) {
                              LogUtils.printStackErrInfo("droidGuardSingClass exception",e);
                            }

                        }

                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            LogUtils.show("droid_guard_sign result= " +param.thisObject);
                        }
                    }

            );
        } catch (Exception e) {
            PrintStack.printStackErrInfo("RegisterHook", e);
        }


    }
    public static void runV20250319(XC_LoadPackage.LoadPackageParam loadPackageParam){
        ClassLoader classLoader = loadPackageParam.classLoader;
        try {
            Class hookRegisterManagerClass = CommonMessageClass.unionManagerEntryClass;
            Class<?> MSISDN_STATE_ENUMClass = classLoader.loadClass("cews");
            Class<?> cfayClass = classLoader.loadClass("dkug");
            XposedHelpers.findAndHookMethod(hookRegisterManagerClass, "ak", android.os.Message.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
//                LogUtils.show("cfbs beforeHookedMethod ak" + param.args[0]);
                    super.beforeHookedMethod(param);
                }

                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    super.afterHookedMethod(param);
                    KeyInfo.printLog(new KeyInfo(KeyCommonInfo.Tag.register, String.valueOf(MSISDN_STATE_ENUM.fromValue(Integer.parseInt(param.getResult().toString())))));
                    RegisterKeyInfo.getInstance().addRegisterStatus(MSISDN_STATE_ENUM.fromValue(Integer.parseInt(param.getResult().toString())).toString());
                    LogUtils.show("RegisterStatusManager afterHookedMethod ak " + param.args[0] + " result= " + MSISDN_STATE_ENUM.fromValue(Integer.parseInt(param.getResult().toString())));
                    ;
                }
            });

            Class ProvisioningHttpRequestClass = classLoader.loadClass("dlcn");
            Class RcsProvisioningEnumsClass = classLoader.loadClass("fasn");
            Class httpRequestEventClass = classLoader.loadClass("famr");
            Class optionClass = classLoader.loadClass("j$.util.Optional");

            XposedHelpers.findAndHookConstructor(ProvisioningHttpRequestClass, RcsProvisioningEnumsClass, int.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, optionClass, httpRequestEventClass, optionClass, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    super.beforeHookedMethod(param);
                }

                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    super.afterHookedMethod(param);
                    LogUtils.show("ProvisioningHttpRequestClass  thisObject= " + param.thisObject);
                    RegisterKeyInfo.getInstance().setProvisioningHttpRequest(param.thisObject.toString());
                }
            });
            Class<?> VerifyMisdnStateExceptionClass = classLoader.loadClass("dgtd");
            XposedHelpers.findAndHookMethod(VerifyMisdnStateExceptionClass, "a", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    super.beforeHookedMethod(param);
                }
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    super.afterHookedMethod(param);
                    LogUtils.show("GmsCommonHook VerifyMisdnStateExceptionClass getCode "+param.getResult());
                    if (Objects.equals(param.getResult().toString(), "5002") ||Objects.equals(param.getResult().toString(), "20")){
                        if (GmsCommonHook.useFakeBundle){
                            param.setResult(5001);

                        }
                    }
                }
            });


            XposedHelpers.findAndHookMethod(hookRegisterManagerClass, "ai", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    super.beforeHookedMethod(param);
                }

                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    super.afterHookedMethod(param);
                    LogUtils.show("ckfb register status  ah result=" + param.getResult());
                    param.setResult(false);
                }
            });
        }catch (Exception e) {
            PrintStack.printStackErrInfo("RegisterHook", e);
        }
    }

    public static void runV20240213(XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {

        ClassLoader classLoader = loadPackageParam.classLoader;
        try {
            hookRegisterManagerClass(classLoader);
            hookPerCheckStatus(classLoader);
            hookDroidGuard(classLoader);
            hookWaitingForOtpStateClass(classLoader);
            hookMessageEnumStatus(classLoader);
        } catch (Exception e) {
            PrintStack.printStackErrInfo("RegisterHook", e);
        }


    }


    public static void runV240519(XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {
        ClassLoader classLoader = loadPackageParam.classLoader;
        try {

            hookV240519State(classLoader);
            Class<?> VerifyMisdnStateExceptionClass = classLoader.loadClass("cfze");

            XposedHelpers.findAndHookMethod(VerifyMisdnStateExceptionClass, "a", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    super.beforeHookedMethod(param);
                }
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    super.afterHookedMethod(param);
                    LogUtils.show("GmsCommonHook VerifyMisdnStateExceptionClass getCode "+param.getResult());
                    if (Objects.equals(param.getResult().toString(), "5002") ||Objects.equals(param.getResult().toString(), "20")){
                        if (GmsCommonHook.useFakeBundle){
                            param.setResult(5001);

                        }
                    }
                }
            });


            Class hookRegisterManagerClass = classLoader.loadClass("ckfb");
            XposedHelpers.findAndHookMethod(hookRegisterManagerClass, "aw", android.os.Message.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    super.beforeHookedMethod(param);
                }

                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    super.afterHookedMethod(param);
                    KeyInfo keyInfo = new KeyInfo(KeyCommonInfo.Tag.register, String.valueOf(MSISDN_STATE_ENUM.fromValue(Integer.parseInt(param.getResult().toString()))));
                    String statusStr = MSISDN_STATE_ENUM.fromValue(Integer.parseInt(param.getResult().toString())).toString();
                    KeyInfo.printLog(keyInfo);
                    RegisterKeyInfo.getInstance().addRegisterStatus(statusStr);
                    RegisterKeyInfo.getInstance().addStatusList(statusStr);
                    LogUtils.show("RegisterStatusManager afterHookedMethod aw " + param.args[0] + " result= " + MSISDN_STATE_ENUM.fromValue(Integer.parseInt(param.getResult().toString())));


                }
            });


            Class ProvisioningHttpRequestClass = classLoader.loadClass("ckiz");
            Class RcsProvisioningEnumsClass = classLoader.loadClass("dyma");
            Class httpRequestEventClass = classLoader.loadClass("dygc");
            Class optionClass = classLoader.loadClass("j$.util.Optional");

            XposedHelpers.findAndHookConstructor(ProvisioningHttpRequestClass, RcsProvisioningEnumsClass, int.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, optionClass, httpRequestEventClass, optionClass, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    super.beforeHookedMethod(param);
                }

                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    super.afterHookedMethod(param);
                    super.afterHookedMethod(param);
                    LogUtils.show("ProvisioningHttpRequestClass  thisObject= " + param.thisObject);
                    RegisterKeyInfo.getInstance().setProvisioningHttpRequest(param.thisObject.toString());
                }
            });

            Class MsgStatuEnumsClass = classLoader.loadClass("cjzx");

            XposedHelpers.findAndHookMethod(MsgStatuEnumsClass, "a", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    super.beforeHookedMethod(param);
                }

                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    super.afterHookedMethod(param);
                    LogUtils.show("MsgStatuEnums " + MSISDN_STATE_ENUM.fromValue((Integer) param.getResult()));
//                    if (MSISDN_STATE_ENUM.fromValue((Integer) param.getResult()).toString().contains("MSG_TIMEOUT")){
//                        LogUtils.printStack4("MSG_TIMEOUT");
//                    }

                }
            });


            Class WaitingForOtpStateClass = classLoader.loadClass("ckew");
            Class<?> MSISDN_STATE_ENUMClass = classLoader.loadClass("cjzx");

            XposedHelpers.findAndHookMethod(WaitingForOtpStateClass, "k", android.os.Message.class, MSISDN_STATE_ENUMClass, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
                    LogUtils.show("WaitingForOtpStateClass beforeHookedMethod k" + param.args[0] + param.args[1]);
                    super.beforeHookedMethod(param);
                }

                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    LogUtils.show("WaitingForOtpStateClass afterHookedMethod k" + param.args[0] + param.args[1]);
                    super.afterHookedMethod(param);

                }
            });

            XposedHelpers.findAndHookMethod(WaitingForOtpStateClass, "a", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    super.beforeHookedMethod(param);
                }

                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    LogUtils.show("WaitingForOtpStateClass afterHookedMethod a result=" + param.getResult());
                    super.afterHookedMethod(param);
                    String statusStr=param.getResult().toString();
                    RegisterKeyInfo.getInstance().addRegisterStatus(statusStr);
                    RegisterKeyInfo.getInstance().addStatusList(statusStr);
                }
            });
            XposedHelpers.findAndHookMethod(RcsProvisioningEnumsClass, "toString", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    super.beforeHookedMethod(param);
                }

                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {

                    super.afterHookedMethod(param);
                    LogUtils.show("RcsProvisioningEnumsClass status= " + param.getResult());
                }
            });


        } catch (Exception e) {
            PrintStack.printStackErrInfo("droid_guard_sign ", e);
        }


    }

    public static void hookV240519State(ClassLoader classLoader) throws ClassNotFoundException {

        Class<?> RequestWithTokenStateClass = classLoader.loadClass("ckdx");
        XposedHelpers.findAndHookMethod(RequestWithTokenStateClass, "b", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);
                LogUtils.show("RequestWithTokenStateClass beforeHookedMethod b result=" + param.getResult());
            }

            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
            }
        });


        XposedHelpers.findAndHookMethod("dvsy", classLoader, "a", int.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);
            }

            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
                LogUtils.show("dvsy register status  a result=" + param.getResult());
//                param.setResult(5);
            }
        });


        XposedHelpers.findAndHookMethod("ckfb", classLoader, "ah", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);
            }

            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
                LogUtils.show("ckfb register status  ah result=" + param.getResult());
                param.setResult(false);
            }
        });


        XposedHelpers.findAndHookMethod("ckei", classLoader, "e", java.lang.Object.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);
                LogUtils.show("VerifyMsisdnState ckei register status  e result=" + param.args[0]);
            }

            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);

            }
        });
    }


    public static void hookMessageEnumStatus(ClassLoader classLoader) throws ClassNotFoundException {
        Class<?> MSISDN_STATE_ENUMClass = classLoader.loadClass("cews");
        XposedHelpers.findAndHookMethod("cfay", classLoader, "o", android.os.Message.class, MSISDN_STATE_ENUMClass, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                LogUtils.show("cfay beforeHookedMethod o" + param.args[0] + " arg1= " + MSISDN_STATE_ENUM.fromValue(Integer.parseInt(param.args[1].toString())));
                super.beforeHookedMethod(param);
            }

            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
            }
        });


        XposedHelpers.findAndHookMethod(Handler.class, "sendMessageDelayed", android.os.Message.class, long.class, new XC_MethodHook() {

            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                Message message = (Message) param.args[0];
                LogUtils.show(" StateMachineHandlerClass sendMessageDelayed beforeHookedMethod printMessage= " + printMessage(message));
                super.beforeHookedMethod(param);
            }

            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
            }
        });
    }


    public static void hookDroidGuard(ClassLoader classLoader) throws ClassNotFoundException {
        XposedHelpers.findAndHookMethod("cffg", classLoader, "l", java.lang.String.class, java.lang.String.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                LogUtils.show("cffg hookHttp beforeHookedMethod l arg0= " + param.args[0] + " arg1= " + param.args[1]);
                if (param.args[0] == "OTP") {
                    LogUtils.show(" OTPsss  " + StringUtils.bytesToHexString(param.args[1].toString().getBytes()));
                }
                super.beforeHookedMethod(param);
            }

            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
            }
        });

        XposedHelpers.findAndHookMethod("cffg", classLoader, "k", java.lang.String.class, java.lang.String.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                LogUtils.show("cffg_k hookHttp beforeHookedMethod k arg0= " + param.args[0] + " arg1= " + param.args[1]);
                super.beforeHookedMethod(param);
            }

            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
            }
        });

        Class droidGuardContentBindingClass = classLoader.loadClass("ceyt");
        XposedHelpers.findAndHookMethod(droidGuardContentBindingClass, "i", java.lang.String.class, java.lang.String.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                LogUtils.show("droidGuardContentBindingClass concatenateStrings key= " + param.args[0] + " value= " + param.args[1] + " clazz= " + param.thisObject);
                super.beforeHookedMethod(param);
            }

            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
            }
        });

        //com.google.android.gms.droidguard.DroidGuardResultsRequest
        Class DroidGuardResultsRequestClass = classLoader.loadClass("com.google.android.gms.droidguard.DroidGuardResultsRequest");
        XposedHelpers.findAndHookMethod("cbjj", classLoader, "b", java.lang.String.class, java.util.Map.class, DroidGuardResultsRequestClass, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);
            }

            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                LogUtils.show("droid_guard_sign beforeHookedMethod  " + Arrays.toString(param.args) + " result= " + param.getResult());
                super.afterHookedMethod(param);
            }
        });

    }

    public static void hookPerCheckStatus(ClassLoader classLoader) throws ClassNotFoundException {


        Class<?> StateMachineHandlerClass = classLoader.loadClass("ceco");
        Class<?> InterfaceViewModelScopeClass = classLoader.loadClass("dzkk");
        Class<?> cfcjClass = classLoader.loadClass("cfcj");
        Class<?> dyzlClass = classLoader.loadClass("dyzl");
        Class<?> VerifyMsisdnStateClass = classLoader.loadClass("cfbb");

        Class<?> RequestWithMsisdnTokenStateClass = classLoader.loadClass("cfan");

        XposedHelpers.findAndHookMethod(StateMachineHandlerClass, "handleMessage", android.os.Message.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                LogUtils.show("StateMachineHandlerClass handleMessage beforeHookedMethod arg0= " + param.args[0]);
                super.beforeHookedMethod(param);
            }

            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
            }
        });
        Class<?> cebzClass = classLoader.loadClass("cebz");
        XposedHelpers.findAndHookMethod(StateMachineHandlerClass, "c", cebzClass, cebzClass, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                if (param.args[0] == null && param.args[1] == null) {
                    LogUtils.show("StateMachineHandlerClass beforeHookedMethod c arg0= " + param.args[0].getClass().getName() + " arg1=" + param.args[1].getClass().getName(), true);
                } else {
                    LogUtils.show("StateMachineHandlerClass beforeHookedMethod c arg0= " + param.args[0].getClass().getName(), true);
                }
                super.beforeHookedMethod(param);
            }

            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
            }
        });

        // hook sendMessageDelayed(android.os.Message, long)


        Class ProvisioningStateMachineClass = classLoader.loadClass("cfcw");
        XposedHelpers.findAndHookConstructor(ProvisioningStateMachineClass, java.lang.String.class, java.util.Map.class, java.util.Map.class, InterfaceViewModelScopeClass, dyzlClass, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                LogUtils.show("ProvisioningStateMachine  beforeHookedMethod cfcw arg0= " + param.args[0] + " arg1= " + param.args[1] + " arg2= " + param.args[2] + " arg3= " + param.args[3] + " arg4= " + param.args[4]);
                super.beforeHookedMethod(param);
            }

            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
            }
        });


        Class StateMachineV2BuilderForPesmClass = classLoader.loadClass("cfcz");
        XposedHelpers.findAndHookConstructor(StateMachineV2BuilderForPesmClass, java.lang.String.class, InterfaceViewModelScopeClass, dyzlClass, cfcjClass, cfcjClass, cfcjClass, cfcjClass, cfcjClass, cfcjClass, cfcjClass, cfcjClass, cfcjClass, cfcjClass, cfcjClass, cfcjClass, cfcjClass, cfcjClass, cfcjClass, cfcjClass, cfcjClass, cfcjClass, cfcjClass, cfcjClass, cfcjClass, cfcjClass, cfcjClass, cfcjClass, cfcjClass, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);
            }

            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
                LogUtils.show("cfcz afterHookedMethod thisObject= " + param.thisObject);
            }
        });


        XposedHelpers.findAndHookMethod(VerifyMsisdnStateClass, "a", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);
            }

            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                LogUtils.show("VerifyMsisdnStateClass afterHookedMethod a result=" + param.getResult());
                super.afterHookedMethod(param);
            }
        });


        XposedHelpers.findAndHookMethod(RequestWithMsisdnTokenStateClass, "a", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);
            }

            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                LogUtils.show("RequestWithMsisdnTokenStateClass afterHookedMethod a result=" + param.getResult());

                super.afterHookedMethod(param);
            }
        });

        Class ProvisioningHttpRequestClass = classLoader.loadClass("cfew");
        Class RcsProvisioningEnumsClass = classLoader.loadClass("dstq");
        Class httpRequestEventClass = classLoader.loadClass("dsnp");
        Class optionClass = classLoader.loadClass("j$.util.Optional");
        XposedHelpers.findAndHookConstructor(ProvisioningHttpRequestClass, RcsProvisioningEnumsClass, int.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, optionClass, httpRequestEventClass, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {

                super.beforeHookedMethod(param);
            }

            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {

                super.afterHookedMethod(param);
                LogUtils.show("ProvisioningHttpRequestClass  thisObject= " + param.thisObject);

                RegisterKeyInfo.getInstance().setProvisioningHttpRequest(param.thisObject.toString());
            }
        });

    }


    public static void hookWaitingForOtpStateClass(ClassLoader classLoader) throws ClassNotFoundException {
        Class MsgStatuEnumsClass = classLoader.loadClass("cews");

        XposedHelpers.findAndHookMethod(MsgStatuEnumsClass, "a", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);
            }

            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
                LogUtils.show("MsgStatuEnums " + MSISDN_STATE_ENUM.fromValue((Integer) param.getResult()));

            }
        });


        Class InProgressStateClass = classLoader.loadClass("cfaf");
        Class<?> MSISDN_STATE_ENUMClass = classLoader.loadClass("cews");
        XposedHelpers.findAndHookMethod(InProgressStateClass, "k", android.os.Message.class, MSISDN_STATE_ENUMClass, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                LogUtils.show("InProgressStateClass beforeHookedMethod o" + param.args[0] + " arg1= " + MSISDN_STATE_ENUM.fromValue(Integer.parseInt(param.args[1].toString())));

                super.beforeHookedMethod(param);
            }

            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
            }
        });

        Class<?> WaitingForOtpStateClass = classLoader.loadClass("cfbn");
        XposedHelpers.findAndHookMethod(WaitingForOtpStateClass, "k", android.os.Message.class, MSISDN_STATE_ENUMClass, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
                LogUtils.show("WaitingForOtpStateClass beforeHookedMethod k" + param.args[0] + param.args[1]);
                super.beforeHookedMethod(param);
            }

            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                LogUtils.show("WaitingForOtpStateClass afterHookedMethod k" + param.args[0] + param.args[1]);
                super.afterHookedMethod(param);
            }
        });

        XposedHelpers.findAndHookMethod(WaitingForOtpStateClass, "a", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);
            }

            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                LogUtils.show("WaitingForOtpStateClass afterHookedMethod a result=" + param.getResult());

                super.afterHookedMethod(param);
            }
        });

    }


    //总的调度
    public static void hookRegisterManagerClass(ClassLoader classLoader) throws ClassNotFoundException {

        Class hookRegisterManagerClass = classLoader.loadClass("cfbs");
        Class<?> MSISDN_STATE_ENUMClass = classLoader.loadClass("cews");
        Class<?> cfayClass = classLoader.loadClass("cfay");
        XposedHelpers.findAndHookMethod(hookRegisterManagerClass, "ak", android.os.Message.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
//                LogUtils.show("cfbs beforeHookedMethod ak" + param.args[0]);
                super.beforeHookedMethod(param);
            }

            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
                KeyInfo.printLog(new KeyInfo(KeyCommonInfo.Tag.register, String.valueOf(MSISDN_STATE_ENUM.fromValue(Integer.parseInt(param.getResult().toString())))));
                RegisterKeyInfo.getInstance().addRegisterStatus(MSISDN_STATE_ENUM.fromValue(Integer.parseInt(param.getResult().toString())).toString());
                LogUtils.show("RegisterStatusManager afterHookedMethod ak " + param.args[0] + " result= " + MSISDN_STATE_ENUM.fromValue(Integer.parseInt(param.getResult().toString())));
                ;
            }
        });


        XposedHelpers.findAndHookMethod(hookRegisterManagerClass, "G", java.net.HttpURLConnection.class, int.class, cfayClass, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                LogUtils.show("RegisterStatusManager hook beforeHookedMethod   G" + param.args[0] + param.args[1] + param.args[2]);
                super.beforeHookedMethod(param);
            }

            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
            }
        });

        XposedHelpers.findAndHookMethod(hookRegisterManagerClass, "l", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
//                PrintStack.printStack("cfbs_l");
                LogUtils.show("RegisterStatusManager_l Trying to get state name when current state= " + param.getResult());
            }
        });


        XposedHelpers.findAndHookMethod(hookRegisterManagerClass, "f", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);
            }

            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
                KeyInfo.printLog(new KeyInfo(KeyCommonInfo.Tag.info, String.valueOf(param.getResult())));
                LogUtils.show("RegisterStatusManager afterHookedMethod f result= " + param.getResult());
            }
        });


        XposedHelpers.findAndHookMethod(hookRegisterManagerClass, "K", MSISDN_STATE_ENUMClass, java.lang.Object.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                LogUtils.show("RegisterStatusManager beforeHookedMethod K " + MSISDN_STATE_ENUM.fromValue(Integer.parseInt(param.args[0].toString())) + " arg1= " + param.args[1]);
//                PrintStack.printStack("cfbs_K");
                super.beforeHookedMethod(param);
            }

            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
            }
        });


    }


    public static String printMessage(Message message) {
        if (message == null) {

            return "null message";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Message{");
        sb.append("what=").append(message.what);
//        sb.append(", arg1=").append(message.arg1);
//        sb.append(", arg2=").append(message.arg2);
        sb.append(", obj=").append(message.obj);
//        sb.append(", replyTo=").append(message.replyTo);
//        sb.append(", when=").append(message.getWhen());
//        sb.append(", target=").append(message.getTarget().getClass().getName());
//        sb.append(", callback=").append(message.getClass());

        Bundle data = message.getData();
        if (data != null) {
            sb.append(", data=").append(bundleToString(data));
        }

        sb.append('}');
        return sb.toString();
    }


}
/*
Setting visibility of Window
https://rcs-acs-mcc234.jibe.google.com/?vers=0&rcs_state=0&IMSI=234159359517316&IMEI=358287582000288&terminal_model=Pixel%207&terminal_vendor=Goog&terminal_sw_version=14&client_vendor=Goog&client_version=20240213-01.03&rcs_profile=UP_T&rcs_version=5.1B&msisdn=%2B447799919560&token=&SMS_port=0
https://rcs-acs-tmobile-us.jibe.google.com/?vers=0&rcs_state=0&IMSI=310240352671241&IMEI=359145611065988&terminal_model=Pixel%207&terminal_vendor=Goog&terminal_sw_version=13&client_vendor=Goog&client_version=20240213-01.03&rcs_profile=UP_T&rcs_version=5.1B&msisdn=%2B14708861984&token=&SMS_port=0
 */
package com.demo.java.xposed.rcs.hook.messages;

import com.demo.java.xposed.device.PluginInit;
import com.demo.java.xposed.utils.LogUtils;
import com.demo.java.xposed.utils.PrintStack;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class LogHook {

    public static void run(XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {
        LogUtils.show("LogHook run: " + loadPackageParam.packageName);

        try {
            ClassLoader classLoader = loadPackageParam.classLoader;
            hookCarrierServicesLog(classLoader);
            hookTrackingLog(classLoader);
        } catch (Exception e) {
            PrintStack.printStackErrInfo("LogHook", e);
        }


    }
    public static void runV20250319(XC_LoadPackage.LoadPackageParam loadPackageParam){
        LogUtils.show("LogHook runV20250319: " + loadPackageParam.packageName);

        try {
            ClassLoader classLoader = loadPackageParam.classLoader;
            Class<?> TrackingLogClass = classLoader.loadClass("ctld");
            XposedHelpers.findAndHookMethod(TrackingLogClass, "r", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    StringBuilder stringBuilder = (StringBuilder) XposedHelpers.getObjectField(param.thisObject, "b");
                    String text = stringBuilder.toString();
                    if (!text.isEmpty() && !text.contains("BuglePhoneNumberUtils")) {
                        LogUtils.show("TrackingLogClass beforeHookedMethod_r " + text + " this= " + param.thisObject);
                        super.beforeHookedMethod(param);
                    }

                }

                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {

                    super.afterHookedMethod(param);
                }
            });


            Class<?> dluz = classLoader.loadClass("dluz");
            Class<?> CarrierServicesLogClass = classLoader.loadClass("dlvk");
            XposedHelpers.findAndHookMethod(CarrierServicesLogClass, "n", int.class, dluz, java.lang.Throwable.class, java.lang.String.class, java.lang.Object[].class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    super.beforeHookedMethod(param);
                    LogUtils.printParams("CarrierServicesLog ", param.args);
                }
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    super.afterHookedMethod(param);
                }
            });

        } catch (Exception e) {
            PrintStack.printStackErrInfo("LogHook", e);
        }


    }

    public static void runV240519(XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {
        LogUtils.show("LogHook runV240519: " + loadPackageParam.packageName);

        try {
            ClassLoader classLoader = loadPackageParam.classLoader;
            Class<?> TrackingLogClass = classLoader.loadClass("btkd");
            XposedHelpers.findAndHookMethod(TrackingLogClass, "r", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    StringBuilder stringBuilder = (StringBuilder) XposedHelpers.getObjectField(param.thisObject, "b");
                    String text = stringBuilder.toString();
                    if (!text.isEmpty() && !text.contains("BuglePhoneNumberUtils")) {
                        LogUtils.show("TrackingLogClass beforeHookedMethod_r " + text + " this= " + param.thisObject);
                        super.beforeHookedMethod(param);
                    }

                }

                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {

                    super.afterHookedMethod(param);
                }
            });


            Class<?> clcqClass = classLoader.loadClass("clcq");
            Class<?> CarrierServicesLogClass = classLoader.loadClass("cldb");
            XposedHelpers.findAndHookMethod(CarrierServicesLogClass, "n", int.class, clcqClass, java.lang.Throwable.class, java.lang.String.class, java.lang.Object[].class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    super.beforeHookedMethod(param);
                    LogUtils.printParams("CarrierServicesLog ", param.args);
                }
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    super.afterHookedMethod(param);
                }
            });

            Class<?> LogUtilClass = classLoader.loadClass("btig");
            XposedHelpers.findAndHookMethod(LogUtilClass, "s", java.lang.String.class, int.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    super.beforeHookedMethod(param);
                }
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    super.afterHookedMethod(param);
                    param.setResult(true);
                }
            });


            XposedHelpers.findAndHookMethod("btik", classLoader, "r", int.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    super.beforeHookedMethod(param);
                    if(PluginInit.isDebug){
                        param.args[0]=2;
                    }


                }
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    super.afterHookedMethod(param);
                }
            });

            XposedHelpers.findAndHookMethod("asmc", classLoader, "a", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    super.beforeHookedMethod(param);
                }
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    super.afterHookedMethod(param);
                    if(PluginInit.isDebug){
                        //group table 日志配置
                        param.setResult(false);
                    }
                }
            });


            XposedHelpers.findAndHookMethod("clda", classLoader, "c", java.lang.Object.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    super.beforeHookedMethod(param);
                }
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    super.afterHookedMethod(param);
                    LogUtils.show("clda senstive_msg= " +param.args[0]);
//                    param.setResult(param.args[0].toString());
                }
            });
        } catch (Exception e) {
            PrintStack.printStackErrInfo("LogHook", e);
        }


    }

    public static void hookTrackingLog(ClassLoader classLoader) throws ClassNotFoundException {
        Class TrackingLogClass = classLoader.loadClass("bozu");
        XposedHelpers.findAndHookMethod(TrackingLogClass, "x", java.lang.String.class, java.lang.CharSequence.class, int.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                LogUtils.printParams("TrackingLogClass_x", param.args);
                super.beforeHookedMethod(param);
            }

            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
            }
        });

        XposedHelpers.findAndHookMethod(TrackingLogClass, "r", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                StringBuilder stringBuilder = (StringBuilder) XposedHelpers.getObjectField(param.thisObject, "b");
                String text = stringBuilder.toString();
                if (!text.isEmpty() && !text.contains("BuglePhoneNumberUtils")) {
                    LogUtils.show("TrackingLogClass beforeHookedMethod_r " + text + " this= " + param.thisObject);
                    super.beforeHookedMethod(param);
                }

            }

            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {

                super.afterHookedMethod(param);
            }
        });
        //设置debug
        XposedHelpers.findAndHookMethod("dnpw", classLoader, "ad", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);
            }
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
                if(PluginInit.isDebug){
                    LogUtils.show("dnpw setDebug " +param.getResult());
                    param.setResult(true);
                }

            }
        });
    }


    public static void hookCarrierServicesLog(ClassLoader classLoader) throws ClassNotFoundException {
        Class<?> cfyf = classLoader.loadClass("cfyf");
        Class CarrierServicesLogClass = classLoader.loadClass("cfyq");
        XposedHelpers.findAndHookMethod(CarrierServicesLogClass, "x", int.class, java.lang.Throwable.class, cfyf, java.lang.String.class, java.lang.Object[].class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                LogUtils.printParams("CarrierServicesLog ", param.args);
                super.beforeHookedMethod(param);
            }

            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
            }
        });


    }


}


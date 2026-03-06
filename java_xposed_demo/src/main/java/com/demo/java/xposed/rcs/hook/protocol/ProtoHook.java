package com.demo.java.xposed.rcs.hook.protocol;

import com.demo.java.xposed.base.BaseAppHook;
import com.demo.java.xposed.device.PluginInit;
import com.demo.java.xposed.rcs.hook.messages.Rcs;
import com.demo.java.xposed.utils.LogUtils;
import com.google.protobuf.util.JsonFormat;

import java.io.InputStream;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class ProtoHook extends BaseAppHook {


    public static void run(XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {
        if (!PluginInit.isDebug) {
            return;

        }
        hookMessageMarshaller(loadPackageParam);

    }


    public static void hookMessageMarshaller(XC_LoadPackage.LoadPackageParam loadPackageParam) {
        ClassLoader classLoader = loadPackageParam.classLoader;
        try {

            Class<?> ProtoMarshallerClass = classLoader.loadClass("eedm");
            XposedHelpers.findAndHookMethod(ProtoMarshallerClass, "b", java.io.InputStream.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {

                    super.beforeHookedMethod(param);

                }

                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    super.afterHookedMethod(param);
                    LogUtils.show("ProtoHook ProtoMarshallerClass parse obj=" + param.getResult());
                    LogUtils.show("ProtoHook ProtoMarshallerClass parse result MessageLite: " + protoObjToHex(param.getResult()));
                    if (param.getResult().toString().contains("ebmi")) {
                        LogUtils.show("ProtoHook ProtoMarshallerClass parse LookupRegisteredResposne: " + param.getResult().toString());
                        Rcs.LookupRegisteredResposne lookupRegisteredResposne = Rcs.LookupRegisteredResposne.parseFrom((byte[]) XposedHelpers.callMethod(param.getResult(), "toByteArray"));
                        String json = JsonFormat.printer().print(lookupRegisteredResposne);
                        LogUtils.simpleShow("ProtoHook ProtoMarshallerClass lookupRegisteredResposne json= " + json);
                    }
                }
            });
            XposedHelpers.findAndHookMethod(ProtoMarshallerClass, "a", java.lang.Object.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    super.beforeHookedMethod(param);
                }

                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    super.afterHookedMethod(param);
                    InputStream original = (InputStream) param.getResult();
//                    byte[] content = InputStreamWrapper.readAndReset(original);
                    LogUtils.show("ProtoHook ProtoMarshallerClass stream obj=" + param.args[0]);
                    LogUtils.show("ProtoHook ProtoMarshallerClass stream param MessageLite: " + protoObjToHex(param.args[0]));
                    param.setResult(original);
                }
            });


            Class<?> MethodDescriptorMarshaller = classLoader.loadClass("edfd");
            Class<?> MethodDescriptorMethodTypeClass = classLoader.loadClass("edfe");
            Class<?> MethodDescriptorClass = classLoader.loadClass("edff");
            XposedHelpers.findAndHookConstructor(MethodDescriptorClass, MethodDescriptorMethodTypeClass, java.lang.String.class, MethodDescriptorMarshaller, MethodDescriptorMarshaller, boolean.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    super.beforeHookedMethod(param);
                }

                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    super.afterHookedMethod(param);
                    LogUtils.show("ProtoHook MethodDescriptorClass  " + param.thisObject.toString());
                }
            });

            Class<?> GeneratedMessageLiteBuilderClass = classLoader.loadClass("dxna");
            XposedHelpers.findAndHookMethod(GeneratedMessageLiteBuilderClass, "E", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    super.beforeHookedMethod(param);
                }

                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    super.afterHookedMethod(param);
                    LogUtils.show("ProtoHook GeneratedMessageLiteBuilderClass stream obj=" + param.getResult());
                    LogUtils.show("ProtoHook GeneratedMessageLiteBuilderClass stream param MessageLite: " + protoObjToHex(param.getResult()));
                }
            });
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

    }

}

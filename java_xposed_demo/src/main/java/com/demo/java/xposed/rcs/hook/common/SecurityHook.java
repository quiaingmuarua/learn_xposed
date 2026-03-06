package com.demo.java.xposed.rcs.hook.common;

import com.demo.java.xposed.base.BaseAppHook;
import com.demo.java.xposed.utils.LogUtils;
import com.demo.java.xposed.utils.PrintStack;
import com.demo.java.xposed.utils.collection.StringUtils;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class SecurityHook extends BaseAppHook {

    public static void run(XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {
        ClassLoader classLoader = loadPackageParam.classLoader;
        Class SecureMessageClass=XposedHelpers.findClass("com.google.communication.synapse.security.scytale.SecureMessage",classLoader);

        Class DecryptStateClass=XposedHelpers.findClass("com.google.communication.synapse.security.scytale.DecryptState",classLoader);

        //com.google.communication.synapse.security.scytale.ReceiptInfo.class
        Class ReceiptInfoClass=XposedHelpers.findClass("com.google.communication.synapse.security.scytale.ReceiptInfo",classLoader);

        //com.google.communication.synapse.security.scytale.Scope.class
        Class ScopeClass=XposedHelpers.findClass("com.google.communication.synapse.security.scytale.Scope",classLoader);

        //com.google.communication.synapse.security.scytale.SenderTrustedInfo.class
        Class SenderTrustedInfoClass=XposedHelpers.findClass("com.google.communication.synapse.security.scytale.SenderTrustedInfo",classLoader);

        //com.google.communication.synapse.security.scytale.UserDevice.class
        Class UserDeviceClass=XposedHelpers.findClass("com.google.communication.synapse.security.scytale.UserDevice",classLoader);


        try {
            XposedHelpers.findAndHookConstructor("com.google.communication.synapse.security.scytale.EncryptResult", classLoader, java.util.ArrayList.class, SecureMessageClass, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
                    super.beforeHookedMethod(param);
                }
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    super.afterHookedMethod(param);
                    LogUtils.printParams("SecurityHook EncryptResult hook " ,param.args,param.thisObject);
                }
            });


            XposedHelpers.findAndHookConstructor("com.google.communication.synapse.security.scytale.DecryptResult", classLoader, DecryptStateClass, byte[].class, ReceiptInfoClass, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    super.beforeHookedMethod(param);
                }
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    super.afterHookedMethod(param);
                    LogUtils.printParams("SecurityHook DecryptResult hook " ,param.args, param.thisObject);
                    //call decryptedMessage and trans to hex
                    LogUtils.show("SecurityHook DecryptResult decryptedMessage " + StringUtils.bytesToHexString((byte[]) XposedHelpers.callMethod(param.thisObject,"getDecryptedMessage")));
                }
            });

            XposedHelpers.findAndHookMethod("com.google.communication.synapse.security.scytale.NativeMessageEncryptorV2$CppProxy", classLoader, "decryptV2", ScopeClass, UserDeviceClass,SenderTrustedInfoClass, SecureMessageClass, long.class, boolean.class, boolean.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    super.beforeHookedMethod(param);
                }
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    super.afterHookedMethod(param);
                    Object secMessages=param.args[3];
                    LogUtils.printParams("SecurityHook NativeMessageEncryptorV2$CppProxy secMessages "  + (StringUtils.bytesToHexString((byte[]) XposedHelpers.callMethod(secMessages,"getCipherText"))));
                    LogUtils.printParams("SecurityHook NativeMessageEncryptorV2$CppProxy hook " ,param.args, param.thisObject);
                }
            });

        }catch (Exception e){
            PrintStack.printStackErrInfo("SecurityHook inner run",e);
        }


    }


}

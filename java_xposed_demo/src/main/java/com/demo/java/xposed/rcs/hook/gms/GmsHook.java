package com.demo.java.xposed.rcs.hook.gms;

import android.content.Context;

import com.demo.java.xposed.utils.LogUtils;
import com.demo.java.xposed.utils.PrintStack;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class GmsHook {

    public static void run(XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {

        try {
            ClassLoader classLoader = loadPackageParam.classLoader;
            Class<?> SmsPendingIntentReceiverClass = classLoader.loadClass("com.google.android.gms.constellation.verifier.SmsSender$SmsPendingIntentReceiver");
            Class<?> type = SmsPendingIntentReceiverClass.getDeclaredField("a").getType().getDeclaredField("b").getType();
            LogUtils.show("GmsHook run type= " + type);
            ;
            String targetMethod = null;
            Method[] declaredMethods = type.getDeclaredMethods();
            for (Method declaredMethod : declaredMethods) {
                LogUtils.show("GmsHook run method= " + declaredMethod.getName() + " type=" + Arrays.toString(declaredMethod.getParameterTypes()));
                ;
                if (Arrays.equals(declaredMethod.getParameterTypes(), new Object[]{Context.class, String.class, Map.class})) {
                    targetMethod = declaredMethod.getName();
                    LogUtils.show("GmsHook  found targetMethod= " + targetMethod);
                    break;
                }
            }


            if (targetMethod != null) {
                LogUtils.show("GmsHook  start  hook targetMethod= " + targetMethod);
                XposedHelpers.findAndHookMethod(type, targetMethod, android.content.Context.class, java.lang.String.class, java.util.Map.class, new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        LogUtils.show("GmsHook  droidguard_provider beforeHookedMethod param= " + Arrays.toString(param.args));
                        super.beforeHookedMethod(param);
                    }
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        super.afterHookedMethod(param);
                        try {
                            super.afterHookedMethod(param);
                            LogUtils.show("GmsHook droidguard_provider afterHookedMethod result= " + param.getResult());
                        } catch (Exception e) {
                            PrintStack.printStackErrInfo("GmsHook droidguard_provider afterHookedMethod", e);


                        }
                    }
                });

            }


        } catch (Exception e) {
            PrintStack.printStackErrInfo("GmsHook inner run", e);
        }


    }


}

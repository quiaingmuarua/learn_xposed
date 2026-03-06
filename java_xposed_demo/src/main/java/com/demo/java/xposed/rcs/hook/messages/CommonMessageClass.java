package com.demo.java.xposed.rcs.hook.messages;

import android.content.Context;
import android.content.Intent;

import com.demo.java.xposed.utils.LogUtils;
import com.demo.java.xposed.utils.ReflectionUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;

import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class CommonMessageClass {

    public static  Class<?> unionManagerEntryClass;


    public static Class<?> droidGuardSingClass;


    public static  String droidGuardSingMethod;

    public static  Class<?> droidGuardResultsRequestClass;


    public static boolean status = false;


    public static String smsDeliverReceiverMethod;

    public static  String configSmsReceiverMethod;


    public static void run(XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {
        LogUtils.show("CommonMessageClass_run  " +loadPackageParam.packageName +loadPackageParam.processName);
        foundDroidGuardSign(loadPackageParam.classLoader);
        foundSmsDeliverReceiverMethod(loadPackageParam.classLoader);
    }


    public static Class foundRcsProvisioningListenableWorkerClass(ClassLoader classLoader) throws ClassNotFoundException {
        try {
            return classLoader.loadClass("com.google.android.apps.messaging.shared.rcsprovisioning.RcsProvisioningListenableWorker");

        } catch (ClassNotFoundException e) {
            return classLoader.loadClass("com.google.android.apps.messaging.rcsprovisioning.RcsProvisioningListenableWorker");
        }
    }


    public static  void foundSmsDeliverReceiverMethod(ClassLoader classLoader){
        Class<?> targetClass = XposedHelpers.findClass(
                "com.google.android.apps.messaging.shared.receiver.SmsDeliverReceiver",
                classLoader
        );
        for (Method method : targetClass.getDeclaredMethods()) {
            // 匹配方法参数：Context 和 Intent
            Class<?>[] paramTypes = method.getParameterTypes();
            if (paramTypes.length == 2 &&
                    paramTypes[0] == Context.class &&
                    paramTypes[1] == Intent.class &&method.getReturnType() == void.class) {
                smsDeliverReceiverMethod= method.getName();
                LogUtils.show("SmsDeliverReceiver found method "+smsDeliverReceiverMethod);

            }

        }
        Class<?> targetClass1 = XposedHelpers.findClass(
                "com.google.android.apps.messaging.shared.receiver.ConfigSmsReceiver",
                classLoader
        );
        for (Method method : targetClass1.getDeclaredMethods()) {
            // 匹配方法参数：Context 和 Intent
            Class<?>[] paramTypes = method.getParameterTypes();
            if (paramTypes.length == 2 &&
                    paramTypes[0] == Context.class &&
                    paramTypes[1] == Intent.class &&method.getReturnType() == void.class) {
                configSmsReceiverMethod= method.getName();
                LogUtils.show("ConfigSmsReceive found method "+configSmsReceiverMethod);

            }

        }
    }


    public static void foundDroidGuardSign(ClassLoader classLoader) {


        try {
            droidGuardResultsRequestClass= classLoader.loadClass("com.google.android.gms.droidguard.DroidGuardResultsRequest");
            Class<?> RcsProvisioningListenableWorkerClass;

            RcsProvisioningListenableWorkerClass = foundRcsProvisioningListenableWorkerClass(classLoader);
            if (RcsProvisioningListenableWorkerClass == null) {
                LogUtils.show("CommonMessageClass hookDroidGuardSign RcsProvisioningListenableWorkerClass is null");
                return;
            }

            for (Field field :  ReflectionUtils.getFieldsByConditions(RcsProvisioningListenableWorkerClass, true, false, false,true,true)){

                LogUtils.printParams("CommonMessageClass hookDroidGuardSign reflect RcsProvisioningListenableWorkerClass field= " + field.getName());
                Class<?> temp1Class = field.getType();
                if (temp1Class.getDeclaredFields().length > 20) {
                    unionManagerEntryClass=temp1Class;
                    LogUtils.show("CommonMessageClass hookDroidGuardSign found unionManagerEntryClass= " + temp1Class.getName());
                    //                   public final ckca f95301L;
                    for(Field field1 : ReflectionUtils.getFieldsByConditions(unionManagerEntryClass,true,true,false,true,true)){
                        Class<?> temp2Class = field1.getType();
                        LogUtils.printParams("CommonMessageClass hookDroidGuardSign found temp2Class ",temp2Class.getName(),temp2Class.getDeclaredFields().length,temp2Class.getDeclaredMethods().length);
                        if (temp2Class.getDeclaredFields().length!=2 || temp2Class.getDeclaredMethods().length!=2){
                            continue;
                        }
                        LogUtils.show("CommonMessageClass hookDroidGuardSign found temp2Class= " + temp2Class.getName());
                        //    /* renamed from: a */
                        //    private final cgil f95142a;
                        droidGuardSingClass = temp2Class.getDeclaredField("a").getType();
                        LogUtils.show("CommonMessageClass hookDroidGuardSign  " + droidGuardSingClass.getName());
                    }


                    Method[] declaredMethods = droidGuardSingClass.getDeclaredMethods();
                    for (Method declaredMethod : declaredMethods) {
                        if (Arrays.equals(declaredMethod.getParameterTypes(), new Object[]{java.lang.String.class, java.util.Map.class, droidGuardResultsRequestClass})) {
                            droidGuardSingMethod = declaredMethod.getName();
                            LogUtils.show("CommonMessageClass hookDroidGuardSign found droidGuardSingMethod= " + droidGuardSingMethod);
                            break;
                        }

                    }

                }}
        }catch (NoSuchFieldException | ClassNotFoundException e){
            LogUtils.show("CommonMessageClass hookDroidGuardSign error "+e.getMessage());
        }




    }
}

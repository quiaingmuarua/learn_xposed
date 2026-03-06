package com.demo.java.xposed.base;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;

import com.demo.java.xposed.utils.LogUtils;
import com.demo.java.xposed.utils.collection.StringUtils;
import com.google.gson.Gson;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.MessageOrBuilder;
import com.google.protobuf.util.JsonFormat;

import java.lang.reflect.Method;

import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public abstract class BaseAppHook  {


    public static void run(XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {
        LogUtils.show("should not be called");

    }


    public static String bundleToString(Bundle bundle) {
        try {
            if (bundle == null) {
                return "null";
            }
            StringBuilder sb = new StringBuilder();
            sb.append("{");
            for (String key : bundle.keySet()) {
                sb.append(key).append('=').append(bundle.get(key)).append(", ");
            }
            if (sb.length() > 1) {
                sb.setLength(sb.length() - 2); // Remove the trailing ", "
            }
            sb.append("}");
            return sb.toString();
        } catch (Exception e) {
            return "err_to_json: "+bundle.toString();
        }

    }

    public static byte[] protoObjToBytes(Object obj){
        if(obj==null){
            return new byte[]{};
        }
       return (byte[]) XposedHelpers.callMethod(obj, "toByteArray");
    }


    public static String printObjToJson(MessageOrBuilder object) throws InvalidProtocolBufferException {
        if(object==null){
            return "";
        }
        return JsonFormat.printer().print(object);


    }

    public static  String protoObjToHex(Object obj){
        if(obj==null){
            return "";
        }
        String hexString= StringUtils.bytesToHexString((byte[]) XposedHelpers.callMethod(obj, "toByteArray"));
        LogUtils.simpleShow("protoObjToHex obj= " +safeToString(obj) + " string= "+StringUtils.bytesToString(StringUtils.HexStringToBytes(hexString)));

        return  hexString;
    }


    public static String safeToString(Object obj) {
        try {
            Method m = obj.getClass().getMethod("toString");
            return (String) m.invoke(obj);
        } catch (Throwable t) {
            return "[toString() failed]";
        }
    }

    public static byte[] safeToByteArray(Object obj) {
        try {
            Method m = obj.getClass().getMethod("toByteArray");
            return (byte[]) m.invoke(obj);
        } catch (Throwable t) {
            return new byte[0];
        }
    }





    public static String appVersion(Context context) {
        PackageManager packageManager = context.getApplicationContext().getPackageManager();
        String packageName = context.getApplicationContext().getPackageName(); // 替换为目标应用的包名

        try {
            PackageInfo packageInfo = packageManager.getPackageInfo(packageName, 0);
            LogUtils.show("packageInfo " + new Gson().toJson(packageInfo));
            return String.valueOf(packageInfo.versionName);
        } catch (PackageManager.NameNotFoundException e) {
            throw new RuntimeException(e);
        }

    }

}

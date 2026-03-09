package com.demo.java.xposed.rcs.hook.protocol;

import com.demo.java.xposed.base.BaseAppHook;
import com.demo.java.xposed.rcs.apiCaller.cache.CachedUnaryRpc;
import com.demo.java.xposed.utils.LogUtils;
import com.demo.java.xposed.utils.collection.StringUtils;
import com.example.sekiro.messages.cache.XposedClassCacher;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class TachyonTokenHook extends BaseAppHook {

    public static void run(XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {
        ClassLoader classLoader = loadPackageParam.classLoader;
        XposedHelpers.findAndHookMethod(XposedClassCacher.RegRefreshRpcHandlerClass, "a", XposedClassCacher.TachyonRegistrationToken, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);
                String TachyonRegistrationToken=protoObjToHex(param.args[0]);
                LogUtils.show("LookupRegistered RegRefreshRpcHandlerClass TachyonRegistrationtoken " + TachyonRegistrationToken );
                CachedUnaryRpc.cacheTokenFromHex(TachyonRegistrationToken);
            }
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
            }
        });


        XposedHelpers.findAndHookMethod("bjnn", classLoader, "a", java.lang.Object.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);
                final byte[] bArr = (byte[]) param.args[0];
                CachedUnaryRpc.cacheOAuthToken(bArr);
                LogUtils.show("TachyonRegistrationtoken Getting Tachyon registration " + StringUtils.bytesToHexString(bArr));
            }
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
            }
        });


        Class<?> PhenotypeServerTokensInterceptorClass = classLoader.loadClass("dusq");
        Class<?> duqr = classLoader.loadClass("duqr");
        XposedHelpers.findAndHookMethod(PhenotypeServerTokensInterceptorClass, "a", duqr, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);
            }
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);

                LogUtils.show("PhenotypeServerTokensInterceptorClass MetadataGrp= " +XposedHelpers.getObjectField(param.args[0],"a"));
                LogUtils.show("PhenotypeServerTokensInterceptorClass CallOptions= " +XposedHelpers.getObjectField(param.args[0],"b"));
                LogUtils.show("PhenotypeServerTokensInterceptorClass MethodDescriptor= " +XposedHelpers.getObjectField(param.args[0],"c"));
                LogUtils.show("PhenotypeServerTokensInterceptorClass HOST= " +XposedHelpers.getObjectField(param.args[0],"d"));
                LogUtils.show("PhenotypeServerTokensInterceptorClass result= "+param.getResult());
                CachedUnaryRpc.cacheMetadata(XposedHelpers.getObjectField(param.args[0],"a"));
            }
        });


    }
}

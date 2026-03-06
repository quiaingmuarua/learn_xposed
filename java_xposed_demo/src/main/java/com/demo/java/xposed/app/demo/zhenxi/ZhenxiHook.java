package com.demo.java.xposed.app.demo.zhenxi;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;
import android.view.View;

import com.demo.java.xposed.utils.CLogUtils;

import java.util.ArrayList;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class ZhenxiHook implements IXposedHookLoadPackage {

    public static boolean isNeedIntoMyDex = true;
    public static Context mContext;
    public static ClassLoader mLoader;
    public static Activity topActivity;
    private static Object GSON = null;
    private static View All = null;
    private static View verifyImageView = null;
    //方法所属class路径
    private static String ClassPath = "";
    private static String MethodName = "";
    //匹配对方so的名字
    private final String IntoSoName = "libTest.so";
    private String packageName = "com.kejian.one";
    //是否需要HookNaitive方法的 开关
    private boolean isNeedHookNative = false;
    private Class bin;
    //存放 这个 app全部的 classloader
    private ArrayList<ClassLoader> AppAllCLassLoaderList = new ArrayList<>();
    private Activity mActivity;

    XC_LoadPackage.LoadPackageParam mlpparam;


    public static Object MainObject;

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        CLogUtils.e("进程名字 " + lpparam.processName);
        if (lpparam.processName.contains(packageName)) {
            mlpparam = lpparam;
            CLogUtils.e("发现被Hook的 App");

            try {
                //HookLoadClass();
                HookAttach(lpparam);
            } catch (Throwable e) {
                CLogUtils.e("发现异常 " + e);
                e.printStackTrace();
            }

        }
    }


    private void HookAttach(XC_LoadPackage.LoadPackageParam lpparam) {
        XposedHelpers.findAndHookMethod(Application.class, "attach",
                Context.class,
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        super.afterHookedMethod(param);
                        CLogUtils.e("走了 attachBaseContext方法 ");
                        mContext = (Context) param.args[0];
                        mLoader = mContext.getClassLoader();
                        CLogUtils.e("拿到classloader");

                    }
                });


        XposedHelpers.findAndHookMethod(Activity.class,
                "onCreate",
                Bundle.class,
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        super.afterHookedMethod(param);

                    }

                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        super.beforeHookedMethod(param);
                        mActivity = (Activity) param.thisObject;
                        CLogUtils.e(mActivity.getClass().getName());
                        if (mActivity.getClass().getName().equals("com.kejian.one.MainActivity")) {
                            CLogUtils.e("拿到 MainActivity 实例");
                            MainObject = param.thisObject;
                            //intoMySo(mLoader);
//                            initSekiro();
                        }
                    }
                });

    }

}

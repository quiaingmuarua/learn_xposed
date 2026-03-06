package com.demo.java.xposed.rcs.hook.common;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Base64;

import com.demo.java.xposed.base.BaseAppHook;
import com.demo.java.xposed.rcs.hook.gms.DroidGuardSign;
import com.demo.java.xposed.rcs.model.RegisterKeyInfo;
import com.demo.java.xposed.rcs.model.RegistrationRequest;
import com.demo.java.xposed.rcs.model.ResponseData;
import com.demo.java.xposed.device.model.SimInfoModel;
import com.demo.java.xposed.utils.LogUtils;
import com.demo.java.xposed.utils.collection.StringUtils;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class GmsCommonHook extends BaseAppHook {

    private static final ThreadLocal<String> myMethod = new ThreadLocal<>();
    public static boolean useFakeBundle = false;

    public static void run(XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {

        LogUtils.show("GmsCommonHook_run  " + loadPackageParam.packageName + loadPackageParam.processName);
        hookConstellationClass( loadPackageParam);
        handleDroidGuardSign(loadPackageParam.classLoader);


    }

    public static void handleDroidGuardSign(ClassLoader classLoader) {
        try {
            Class<?> DroidGuardResultsRequestClass = classLoader.loadClass("com.google.android.gms.droidguard.DroidGuardResultsRequest");
            XposedHelpers.findAndHookMethod("com.google.android.gms.droidguard.internal.IDroidGuardHandle$Stub$Proxy", classLoader, "initWithExtras", java.lang.String.class, DroidGuardResultsRequestClass, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    super.beforeHookedMethod(param);
                    Object DroidGuardResultsRequest =param.args[1];
                    LogUtils.printParams("GmsCommonHook IDroidGuardHandle$Stub$Proxy initWithExtras " ,param.args);
                    LogUtils.show("GmsCommonHook IDroidGuardHandle$Stub$Proxy DroidGuardResultsRequest bundle " +bundleToString((Bundle) XposedHelpers.getObjectField(DroidGuardResultsRequest,"a")));
                }

                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    super.afterHookedMethod(param);
                    LogUtils.printParams("GmsCommonHook IDroidGuardHandle$Stub$Proxy after initWithExtras " ,param.args);

                }
            });
            XposedHelpers.findAndHookMethod("com.google.android.gms.droidguard.internal.IDroidGuardHandle$Stub$Proxy", classLoader, "snapshot", java.util.Map.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    super.beforeHookedMethod(param);
                    LogUtils.printParams("GmsCommonHook IDroidGuardHandle$Stub$Proxy snapshot " ,param.args);
                    Map<String, String> mapParams = (Map<String, String>) param.args[0];
                    String method =mapParams.remove("my_method");
                    myMethod.set(method);
                    param.args[0]=mapParams;
                    LogUtils.show("GmsCommonHook IDroidGuardHandle$Stub$Proxy my_method= " +method);

                }

                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    super.afterHookedMethod(param);
                    LogUtils.printParams("GmsCommonHook IDroidGuardHandle$Stub$Proxy after snapshot " ,param.args);
                    String myMethods= myMethod.get();
                    if (!Objects.equals(myMethods, "tachyon_registration")){
                        LogUtils.show("GmsCommonHook IDroidGuardHandle$Stub$Proxy only handle tachyon_registration now is =" + myMethods);
                        byte[] result = (byte[]) param.getResult();
                        LogUtils.show("GmsCommonHook IDroidGuardHandle$Stub$Proxy raw_snapshot len=" + result.length + " content" + StringUtils.bytesToHexString(result));
                        return;
                    }
                    ResponseData responseData = DroidGuardSign.auto_ensure_droidGuard_sign("gms", RegistrationRequest.getVersion1("tachyon_registration", (Map<String, String>) param.args[0]).toJsonStr(), 5);

                    if (responseData == null) {
                        LogUtils.show("GmsCommonHook IDroidGuardHandle$Stub$Proxy responseData is null");
                        LogUtils.show("GmsCommonHook IDroidGuardHandle$Stub$Proxy" + "use raw_data");
                        return;
                    }
                    Map<String, String> data = responseData.getData();
                    String resultData = data.get("result");

//                    String filePath="/data/data/com.google.android.apps.messaging/"+getLast100Characters(resultData) +".txt";
//                    FileUtils.writeToFile(filePath, resultData);
                    if (TextUtils.isEmpty(resultData) || resultData.length() < 10) {
                        LogUtils.show("GmsCommonHook IDroidGuardHandle$Stub$Proxy resultData is null");
                        LogUtils.show("GmsCommonHook IDroidGuardHandle$Stub$Proxy" + "use raw_data");
                        return;
                    }
                    if (resultData.startsWith("messaging")) {
                        LogUtils.show("droid_guard_sign error app");
                        return;
                    }
                    if (resultData.startsWith("gms_")) {
                        resultData = resultData.substring(4);
                    }
                    if (resultData.startsWith("RES_")) {
                        resultData = resultData.substring(4);

                    }
                    LogUtils.show("GmsCommonHook IDroidGuardHandle$Stub$Proxy resultData " + resultData);
                    byte[] decode = Base64.decode(resultData, 3);
                    LogUtils.show("GmsCommonHook IDroidGuardHandle$Stub$Proxy new_snapshot len=" + decode.length + " content=" + StringUtils.bytesToHexString(decode));
                    param.setResult(decode);

                }
            });

            XposedHelpers.findAndHookConstructor("com.google.android.gms.droidguard.DroidGuardResultsRequest", classLoader, android.os.Bundle.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    LogUtils.printParams("GmsCommonHook DroidGuardResultsRequest ", bundleToString((Bundle) param.args[0]));
                    super.beforeHookedMethod(param);
                }

                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    super.afterHookedMethod(param);
                }
            });
        } catch (Exception e) {
            LogUtils.show("GmsCommonHook IDroidGuardHandle$Stub$Proxy error " + e.getMessage());
        }


    }

    public static void hookConstellationClass(XC_LoadPackage.LoadPackageParam loadPackageParam) throws ClassNotFoundException {
        ClassLoader classLoader=loadPackageParam.classLoader;


        Class IdTokenRequestClass = classLoader.loadClass("com.google.android.gms.constellation.IdTokenRequest");
        XposedHelpers.findAndHookConstructor("com.google.android.gms.constellation.VerifyPhoneNumberRequest", classLoader, java.lang.String.class, long.class, IdTokenRequestClass, android.os.Bundle.class, java.util.List.class, boolean.class, int.class, java.util.List.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);
                LogUtils.printParams("GmsCommonHook VerifyPhoneNumberRequest== ", param.args);
//                param.args[0]="upi-mo-sms-strict";

                Bundle bundle = (Bundle) param.args[3];
                RegisterKeyInfo.getInstance().setVerifyPhoneNumberRequest(bundleToString(bundle), (String) param.args[0]);
                LogUtils.show("GmsCommonHook VerifyPhoneNumberRequest bundle" + bundleToString(bundle));
                if (!bundle.containsKey("IMSI")) {
                    useFakeBundle = true;
                    LogUtils.show("GmsCommonHook VerifyPhoneNumberRequest use fake_bundle");
                    param.args[0] = "upi-mo-sms-strict";
                    //{consent_type=RCS_DEFAULT_ON_LEGAL_FYI, IMSI=310240450799118, required_consumer_consent=RCS, session_id=02c70c18-041f-4212-8be7-7a894c1c0200}
                    Bundle bundle1 = new Bundle();
                    bundle1.putString("consent_type", "RCS_DEFAULT_ON_LEGAL_FYI");
                    bundle1.putString("IMSI", SimInfoModel.getInstance(loadPackageParam.packageName).getSubscriberId());
                    bundle1.putString("required_consumer_consent", "RCS");
                    bundle1.putString("session_id", bundle.getString("session_id"));
                    param.args[3] = bundle1;
                    LogUtils.show("GmsCommonHook VerifyPhoneNumberRequest fake_bundle" + bundleToString(bundle1));
                } else {
                    useFakeBundle = false;
                }


            }

            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
            }
        });


        XposedHelpers.findAndHookConstructor("com.google.android.gms.constellation.PhoneNumberVerification", classLoader, java.lang.String.class, long.class, int.class, int.class, java.lang.String.class, android.os.Bundle.class, int.class, long.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                LogUtils.printParams("GmsCommonHook PhoneNumberVerification ", param.args);

                super.beforeHookedMethod(param);
            }

            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
            }
        });


        XposedHelpers.findAndHookConstructor("com.google.android.gms.constellation.IdTokenRequest", classLoader, java.lang.String.class, java.lang.String.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                LogUtils.printParams("GmsCommonHook IdTokenRequest ", param.args);
                super.beforeHookedMethod(param);
            }

            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
            }
        });


        XposedHelpers.findAndHookConstructor("com.google.android.gms.constellation.ImsiRequest", classLoader, java.lang.String.class, java.lang.String.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);
                LogUtils.show("GmsCommonHook  ImsiRequest " + Arrays.toString(param.args));
            }

            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
            }
        });
        Class<?> PhoneNumberVerificationClass = classLoader.loadClass("com.google.android.gms.constellation.PhoneNumberVerification");
        XposedHelpers.findAndHookConstructor("com.google.android.gms.constellation.VerifyPhoneNumberResponse", classLoader, Array.newInstance(PhoneNumberVerificationClass, 0).getClass(), android.os.Bundle.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);

                LogUtils.show("GmsCommonHook VerifyPhoneNumberResponse bundleToString" + bundleToString((Bundle) param.args[1]));
                LogUtils.printParams("GmsCommonHook VerifyPhoneNumberResponse ", param.args);
                if (useFakeBundle) {
                    param.args[0] = Array.newInstance(PhoneNumberVerificationClass, 0);
                    LogUtils.show("GmsCommonHook VerifyPhoneNumberResponse use fake_bundle");

                }
            }

            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
            }
        });


        XposedHelpers.findAndHookConstructor("com.google.android.gms.constellation.PhoneNumberVerification", classLoader, java.lang.String.class, long.class, int.class, int.class, java.lang.String.class, android.os.Bundle.class, int.class, long.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);
                Bundle bundle = (Bundle) param.args[5];
                //
                // GmsCommonHook PhoneNumberVerification  args=  = +97336086893, 1735978992000, 2, 0, eyJhbGciOiJSUzI1NiIsI
                //GmsCommonHook PhoneNumberVerification bundle{consent_type=RCS_DEFAULT_ON_LEGAL_FYI, calling_api=verifyPhoneNumber, IMSI=426021591257043, required_consumer_consent=RCS, mcc_mnc=42602
                LogUtils.show("GmsCommonHook PhoneNumberVerification bundle" + bundleToString(bundle));
                if (Arrays.toString(param.args).toLowerCase().contains("eyj")) {
                    useFakeBundle = false;
                    LogUtils.show("GmsCommonHook PhoneNumberVerification not_use fake_bundle");
                }
                LogUtils.printParams("GmsCommonHook PhoneNumberVerification  args= ", param.args);
            }

            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
            }
        });


    }


    public static String getLast100Characters(String str) {
        // 检查字符串长度
        if (str.length() <= 100) {
            return str; // 如果字符串长度小于等于 100，直接返回原字符串
        } else {
            return str.substring(str.length() - 20); // 截取末尾 100 位
        }
    }

}


/*
PhoneNumberVerification  = , 0, 0, -1, null, Bundle[mParcelledData.dataSize=456], 0, 0

CarrierServicesLog  = 3, null, (PEv2-SM234531961-DefaultCallSmsData-1), %s [%s], [UPI - Unrecognized VerificationStatus from calling verifyPhoneNumber: 0, 1dcd79fe-b1f7-4d8d-8318-513a7991ae61]

310240255649689
310240390921681
 */
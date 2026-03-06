package com.demo.java.xposed.device;


import android.content.res.Resources;
import android.net.NetworkCapabilities;
import android.telephony.ServiceState;
import android.telephony.SignalStrength;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;

import com.demo.java.xposed.base.BaseAppHook;
import com.demo.java.xposed.device.model.SimInfoModel;
import com.demo.java.xposed.utils.LogUtils;
import com.demo.java.xposed.utils.PrintStack;

import java.lang.reflect.Field;
import java.util.Locale;
import java.util.TimeZone;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class TelephonyHook extends BaseAppHook {

    public static void run(XC_LoadPackage.LoadPackageParam loadPackageParam) {
        try {
            LogUtils.show("TelephonyHook run: " + loadPackageParam.packageName);
            SimInfoModel simInfoModel = SimInfoModel.getInstance(loadPackageParam.packageName);
            ClassLoader classLoader = loadPackageParam.classLoader;
            handleHookSubscriptionInfo(classLoader, simInfoModel);
            handleHookSimTelephonyManager(classLoader, simInfoModel);
            mockValidSimCard(simInfoModel);
//            handleNetworkHook(classLoader);
            TimeZone.setDefault(getTimeZoneByCountry(simInfoModel.getSimCountryName()));
            Locale locale = Resources.getSystem()
                    .getConfiguration()
                    .getLocales()
                    .get(0);

            Locale locale1 = new Locale(locale.getLanguage(), simInfoModel.getSimCountryName().toUpperCase()); // 泰国
            Locale.setDefault(locale1);

            LogUtils.show(" new Locale" +Locale.getDefault().getCountry());
        } catch (Exception e) {
            PrintStack.printStackErrInfo("TelephonyHook inner run", e);
        }
    }

    private static  void handleNetworkHook(ClassLoader classLoader){


        try {
            // 1. 拦截 hasTransport(TRANSPORT_VPN)
            XposedHelpers.findAndHookMethod(
                    NetworkCapabilities.class,
                    "hasTransport",
                    int.class,
                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            int transportType = (int) param.args[0];
                            if (transportType == NetworkCapabilities.TRANSPORT_VPN) {
                                LogUtils.show("[VPNBypass] Blocked hasTransport(VPN)");
                                param.setResult(false);
                            }
                        }
                    }
            );


            // 3. 拦截 toString() 打印前伪装字段（mTransportTypes + mTransportInfo）
            XposedHelpers.findAndHookMethod(
                    NetworkCapabilities.class,
                    "toString",
                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            Object nc = param.thisObject;

                            try {
                                Field mTransportTypesField = nc.getClass().getDeclaredField("mTransportTypes");
                                mTransportTypesField.setAccessible(true);
                                int wifiBitmask = 1 << NetworkCapabilities.TRANSPORT_WIFI;
                                mTransportTypesField.setInt(nc, wifiBitmask);

                                Field mTransportInfoField = nc.getClass().getDeclaredField("mTransportInfo");
                                mTransportInfoField.setAccessible(true);
                                mTransportInfoField.set(nc, null);

                                LogUtils.show("[VPNBypass] toString() patched fields: Wi-Fi only");
                            } catch (Throwable e) {
                                LogUtils.show("[VPNBypass] toString() patch failed: " + e.getMessage());
                            }
                        }
                    }
            );

            // 4. Hook 构造函数，初始化时清除 VPN 字段
            XposedHelpers.findAndHookConstructor(
                    NetworkCapabilities.class,
                    new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            Object nc = param.thisObject;
                            try {
                                Field mTransportTypesField = nc.getClass().getDeclaredField("mTransportTypes");
                                mTransportTypesField.setAccessible(true);
                                int wifiBitmask = 1 << NetworkCapabilities.TRANSPORT_WIFI;
                                mTransportTypesField.setInt(nc, wifiBitmask);

                                Field mTransportInfoField = nc.getClass().getDeclaredField("mTransportInfo");
                                mTransportInfoField.setAccessible(true);
                                mTransportInfoField.set(nc, null);

                                LogUtils.show("[VPNBypass] Constructor patched NetworkCapabilities",true);
                            } catch (Throwable e) {
                                LogUtils.show("[VPNBypass] Constructor patch failed: " + e.getMessage());
                            }
                        }
                    }
            );
            XposedHelpers.findAndHookMethod(
                    "android.net.NetworkInfo",
                    classLoader,
                    "getTypeName",
                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            LogUtils.show("[VPNBypass] getTypeName() -> WIFI");
                            param.setResult("WIFI"); // 或 "MOBILE"
                        }
                    }
            );


        } catch (Exception e) {
            LogUtils.show("[VPNBypass] Exception: " + e.getMessage());
        }
    }




    private static void handleHookSubscriptionInfo(ClassLoader classLoader, SimInfoModel simInfoModel) {
        try {

            Class<?> SubscriptionInfo = XposedHelpers.findClass("android.telephony.SubscriptionInfo", classLoader);
            //called android.telephony.SubscriptionInfo.getIccId()
            //(Ljava/lang/String;)retval=8901240357126712418
            hook_void_argument(SubscriptionInfo, "getIccId", simInfoModel.getIccId());
            //called android.telephony.SubscriptionInfo.getMnc()
            hook_void_argument(SubscriptionInfo, "getMnc", Integer.parseInt(simInfoModel.getMnc()));
            //called android.telephony.SubscriptionInfo.getMcc()
            //(I)retval=310
            hook_void_argument(SubscriptionInfo, "getMcc", Integer.parseInt(simInfoModel.getMcc()));

            //called android.telephony.SubscriptionInfo.getDisplayName()
            //(Ljava/lang/CharSequence;)retval=Ultra
            hook_void_argument(SubscriptionInfo, "getDisplayName", simInfoModel.getSimOperatorName());
            hook_void_argument(SubscriptionInfo, "getCarrierName", simInfoModel.getSimOperatorName());
            //called android.telephony.SubscriptionInfo.getCountryIso()
            //(Ljava/lang/String;)retval=us
            hook_void_argument(SubscriptionInfo, "getCountryIso", simInfoModel.getSimCountryName());

            //called android.telephony.SubscriptionInfo.getNumber()
            //(Ljava/lang/String;)retval=1540470257
            hook_void_argument(SubscriptionInfo, "getNumber", simInfoModel.getPhoneNumber(true,false));
        } catch (Exception e) {
            PrintStack.printStackErrInfo("handleHookSubscriptionInfo", e);
        }


    }

    private static void handleHookSimTelephonyManager(ClassLoader classLoader, SimInfoModel simInfoModel) {
        //hookMultiDex "android.telephony.TelephonyManager"

        try {
            //called android.telephony.TelephonyManager.getLine1Number(int)
            //(int)arg[0]=3
            //(Ljava/lang/String;)retval=13235578576
            hook_int_argument(TelephonyManager.class, "getLine1Number", simInfoModel.getPhoneNumber(true,false));


            //getSimOperator 23410
            hook_void_argument(TelephonyManager.class, "getSimOperator", simInfoModel.getSimOperator());
            //called android.telephony.TelephonyManager.getSimOperatorNumericForPhone(int)
            hook_int_argument(TelephonyManager.class, "getSimOperatorNumericForPhone", simInfoModel.getSimOperator());


            //getSimOperatorNumeric 23410
            //called android.telephony.TelephonyManager.getSimOperatorNumeric(int)
            //(int)arg[0]=1
            //(Ljava/lang/String;)retval=310240
            hook_int_argument(TelephonyManager.class, "getSimOperatorNumeric", simInfoModel.getSimOperator());


            //getSimSerialNumber 8944110068871232646
            //called android.telephony.TelephonyManager.getSimSerialNumber(int)
            //(int)arg[0]=3
            //(Ljava/lang/String;)retval=89234100044172772536
            hook_int_argument(TelephonyManager.class, "getSimSerialNumber", simInfoModel.getIccId());


            //called android.telephony.TelephonyManager.getSimSerialNumber()
            //(Ljava/lang/String;)retval=89441000304439712627
            hook_void_argument(TelephonyManager.class, "getSimSerialNumber", simInfoModel.getIccId());


            //getSimCarrierId 1492  canonical_id
            //called android.telephony.TelephonyManager.getSimCarrierId()
            //(I)retval=2083
            try {
                hook_void_argument(TelephonyManager.class, "getSimCarrierId", Integer.parseInt(simInfoModel.getCarrierId()));
            }catch (Exception e){
                LogUtils.show("handleHookSimTelephonyManager getSimCarrierId 出现异常 " + e.getMessage());
            }


            //getSubscriberId 234107633026171
            //called android.telephony.TelephonyManager.getSubscriberId(int)
            hook_int_argument(TelephonyManager.class, "getSubscriberId", simInfoModel.getSubscriberId());


            //SIM卡运营商 O2   getSimOperatorNameForPhone
            //called android.telephony.TelephonyManager.getSimOperatorNameForPhone(int)
            //(int)arg[0]=0
            //(Ljava/lang/String;)retval=Lebara
            hook_int_argument(TelephonyManager.class, "getSimOperatorNameForPhone", simInfoModel.getSimOperatorName());
            //called android.telephony.TelephonyManager.getSimOperatorName()
            //(Ljava/lang/String;)retval=Ultra
            hook_void_argument(TelephonyManager.class, "getSimOperatorName", simInfoModel.getSimOperatorName());


            //called android.telephony.TelephonyManager.getSimCarrierIdName()
            //(Ljava/lang/CharSequence;)retval=Ultra/Univision
            CharSequence charSequence1 = simInfoModel.getSimOperatorName();
            hook_void_argument(TelephonyManager.class, "getSimCarrierIdName", charSequence1);

            //called android.telephony.TelephonyManager.getSimCountryIso()
            //(Ljava/lang/String;)retval=gb
            hook_void_argument(TelephonyManager.class, "getSimCountryIso", simInfoModel.getSimCountryName());


            //called android.telephony.TelephonyManager.getSimCountryIsoForPhone(int)
            //(int)arg[0]=0
            //(Ljava/lang/String;)retval=us
            hook_int_argument(TelephonyManager.class, "getSimCountryIsoForPhone", simInfoModel.getSimCountryName());

            //called android.telephony.TelephonyManager.getImei(int)
            //(int)arg[0]=1
            //(Ljava/lang/String;)retval=864630039187789
            if (simInfoModel.getImei()!=null && !simInfoModel.getImei().isEmpty()){
                hook_int_argument(TelephonyManager.class, "getImei", simInfoModel.getImei());

            }

            //called android.telephony.SubscriptionManager.getPhoneNumber(int)
            //(int)arg[0]=1
            //(Ljava/lang/String;)retval=+12136655759
            hook_int_argument(TelephonyManager.class, "getLine1Number", simInfoModel.getPhoneNumber(true,true));
            hook_int_argument(SubscriptionManager.class,"getPhoneNumber",simInfoModel.getPhoneNumber(true,true));

            //called android.telephony.SubscriptionManager.isValidSubscriptionId(int)
            //(int)arg[0]=1
            //(Z)retval=true
            hook_int_argument(SubscriptionManager.class,"isValidSubscriptionId",true);


        } catch (Exception e) {
            PrintStack.printStackErrInfo("handleHookSimTelephonyManager", e);
        }

    }



    private static void mockValidSimCard(SimInfoModel simInfoModel) {
        //called android.telephony.ServiceState.getState()
        //(I)retval=0
        hook_void_argument(ServiceState.class, "getState", 0);
        hook_void_argument(ServiceState.class, "getVoiceRegState", 0);
        hook_void_argument(SignalStrength.class,"getLevel", 5);
        //called android.telephony.TelephonyManager.getNetworkOperatorName()
        //(Ljava/lang/String;)retval=CMCC
        hook_void_argument(TelephonyManager.class, "getNetworkOperatorName", simInfoModel.getSimOperatorName());
        //called android.telephony.TelephonyManager.getNetworkOperatorName(int)
        //(int)arg[0]=2
        //(Ljava/lang/String;)retval=CMCC
        hook_int_argument(TelephonyManager.class, "getNetworkOperatorName", simInfoModel.getSimOperatorName());
        //called android.telephony.TelephonyManager.getNetworkCountryIso(int)
        //(int)arg[0]=0
        //(Ljava/lang/String;)retval=cn
        hook_int_argument(TelephonyManager.class, "getNetworkCountryIso", simInfoModel.getSimCountryName());
        //called android.telephony.TelephonyManager.getNetworkCountryIsoForPhone(int)
        //(int)arg[0]=0
        //(Ljava/lang/String;)retval=cn
        hook_int_argument(TelephonyManager.class, "getNetworkCountryIsoForPhone", simInfoModel.getSimCountryName());



        //called android.telephony.TelephonyManager.getSimOperator()
        //(Ljava/lang/String;)retval=52005
        hook_void_argument(TelephonyManager.class,"getSimOperator",simInfoModel.getSimOperator());

        //called java.util.Locale.getCountry()
        //(Ljava/lang/String;)retval=GB
        hook_void_argument(Locale.class,"getCountry",null);


    }

    public static void hook_int_argument(Class<?> clazz, String methodName, boolean result) {
        try {

            XposedHelpers.findAndHookMethod(clazz, methodName, int.class, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {

                    super.afterHookedMethod(param);
                    LogUtils.show("hook_int_argument class="+clazz.getName() +"  method=" + methodName + " original_result=" + param.getResult() + " fake_result=" + result);
                    param.setResult(result);

                }
            });

        } catch (Exception e) {
            LogUtils.show("hook_int_argument 出现异常 " + e.getCause());
        }
    }



    public static void hook_int_argument(Class<?> clazz, String methodName, String result) {
        try {

            XposedHelpers.findAndHookMethod(clazz, methodName, int.class, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {

                    super.afterHookedMethod(param);
                    LogUtils.show("hook_int_argument class="+clazz.getName() +"  method=" + methodName + " original_result=" + param.getResult() + " fake_result=" + result);
                    param.setResult(result);

                }
            });

        } catch (Exception e) {
            LogUtils.show("hook_int_argument 出现异常 " + e.getCause());
        }
    }


    public static void hook_int_argument(Class<?> clazz, String methodName, int result) {
        try {

            XposedHelpers.findAndHookMethod(clazz, methodName, int.class, new XC_MethodHook() {

                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    super.beforeHookedMethod(param);
//                    LogUtils.show("before hook_int_argument " + methodName + " original_result=" + param.getResult() + " fake_result=" + result);
                }


                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    super.afterHookedMethod(param);
                    setResult(param, methodName, result);

                }
            });

        } catch (Exception e) {
            LogUtils.show(clazz + "hook_int_argument" + methodName + "出现异常 " + e);
        }
    }


    public static void hook_void_argument(Class<?> clazz, String methodName, int result) {
        try {

            XposedHelpers.findAndHookMethod(clazz, methodName, new XC_MethodHook() {

                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    super.beforeHookedMethod(param);
                }


                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    super.afterHookedMethod(param);
                    setResult(param, methodName, result);
                }
            });

        } catch (Exception e) {
            LogUtils.show(clazz + "hook_void_argument" + methodName + "出现异常 " + e.getMessage());
        }
    }

    public static void hook_void_argument(Class<?> clazz, String methodName, CharSequence result) {
        try {

            XposedHelpers.findAndHookMethod(clazz, methodName, new XC_MethodHook() {

                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    super.beforeHookedMethod(param);
                }


                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    super.afterHookedMethod(param);
                    setResult(param, methodName, result);
                }
            });

        } catch (Exception e) {
            LogUtils.show(clazz + " hook_void_argument " + methodName + "出现异常 " + e.getMessage());
        }
    }


    public static void hook_void_argument(Class<?> clazz, String methodName, String result) {
        try {

            XposedHelpers.findAndHookMethod(clazz, methodName, new XC_MethodHook() {

                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    super.beforeHookedMethod(param);
                }

                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    super.afterHookedMethod(param);
                    setResult(param, methodName, result);

                }
            });

        } catch (Exception e) {
            LogUtils.show(clazz + " hook_void_argument " + methodName + "出现异常 " + e.getMessage());
        }
    }


    public static void setResult(XC_MethodHook.MethodHookParam param, String methodName, Object result) {

        if (result != null) {
            LogUtils.show("after setResult " + methodName + " original_result=" + param.getResult() + " fake_result=" + result);
            param.setResult(result);
        }

    }


    public static TimeZone getTimeZoneByCountry(String countryCode) {
        if (countryCode == null) {
            return TimeZone.getDefault();
        }

        switch (countryCode.toUpperCase()) {
            case "CN":
                return TimeZone.getTimeZone("Asia/Shanghai");

            case "US":
                return TimeZone.getTimeZone("America/New_York"); // 美国默认东部

            case "JP":
                return TimeZone.getTimeZone("Asia/Tokyo");

            case "GB":
                return TimeZone.getTimeZone("Europe/London");

            case "DE":
                return TimeZone.getTimeZone("Europe/Berlin");

            // ===== 新增国家 =====
            case "TH": // 泰国
                return TimeZone.getTimeZone("Asia/Bangkok");

            case "VN": // 越南
                return TimeZone.getTimeZone("Asia/Ho_Chi_Minh");

            case "IN": // 印度
                return TimeZone.getTimeZone("Asia/Kolkata");

            case "CA": // 加拿大（默认东部）
                return TimeZone.getTimeZone("America/Toronto");

            case "BR": // 巴西（默认圣保罗）
                return TimeZone.getTimeZone("America/Sao_Paulo");

            case "PH": // 菲律宾
                return TimeZone.getTimeZone("Asia/Manila");

            case "MY": // 马来西亚
                return TimeZone.getTimeZone("Asia/Kuala_Lumpur");

            case "ID": // 印度尼西亚（默认雅加达）
                return TimeZone.getTimeZone("Asia/Jakarta");

            default:
                return TimeZone.getDefault(); // fallback
        }
    }


    /*
    722341332047708
    424032583032354

    89543430422320742588
    89971033170709505580
     */
}
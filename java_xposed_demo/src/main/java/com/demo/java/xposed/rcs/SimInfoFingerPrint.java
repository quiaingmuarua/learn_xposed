package com.demo.java.xposed.rcs;

import static androidx.core.content.ContextCompat.getSystemService;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;

import com.demo.java.xposed.utils.LogUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.HashMap;
import java.util.Map;


public class SimInfoFingerPrint {

    private static final String TAG = "SimInfoFingerPrint";



    @SuppressLint("MissingPermission")
    public static String  handTelephonyManager(Context context) {
        try {
            TelephonyManager telephonyManager = (TelephonyManager) getSystemService(context, TelephonyManager.class);
            SubscriptionManager subscriptionManager = (SubscriptionManager) getSystemService(context, SubscriptionManager.class);

            subscriptionManager.getActiveSubscriptionInfoList();
            //gson dump telephonyManager
            Map<String, Object> result = new HashMap<>();

            result.put("getSimOperator", telephonyManager.getSimOperator());
            result.put("getSimCountryIso", telephonyManager.getSimCountryIso());
            result.put("getSimOperatorName", telephonyManager.getSimOperatorName());
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                result.put("getSimCarrierId", telephonyManager.getSimCarrierId());
            }
            result.put("getSimState", telephonyManager.getSimState());
            result.put("getNetworkOperator", telephonyManager.getNetworkOperator());
            result.put("getNetworkOperatorName", telephonyManager.getNetworkOperatorName());
            result.put("getNetworkCountryIso", telephonyManager.getNetworkCountryIso());
            result.put("getNetworkType", telephonyManager.getNetworkType());
            result.put("getPhoneType", telephonyManager.getPhoneType());

            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            String gs = gson.toJson(result);
            LogUtils.show(TAG + " handTelephonyManager: " + gs);
            return gs;
        }catch (Exception e){
            LogUtils.show(TAG + " handTelephonyManager error: " + e.getMessage());
            return "";
        }


    }
}

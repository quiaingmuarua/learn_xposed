package com.demo.java.xposed.rcs.pdu;

import android.telephony.SmsMessage;

import com.demo.java.xposed.utils.LogUtils;
import com.demo.java.xposed.utils.collection.StringUtils;
import com.google.gson.Gson;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
public class SmsUtils {


    public static void printSmsMessage(SmsMessage smsMessage) {

        try {
            Map<String, Object> map = new HashMap<>();

        map.put("getServiceCenterAddress", smsMessage.getServiceCenterAddress());
            map.put("message_body", smsMessage.getMessageBody());
            map.put("message_class", String.valueOf(smsMessage.getMessageClass()));
            map.put("originating_address", smsMessage.getOriginatingAddress());
            map.put("status", String.valueOf(smsMessage.getStatus()));
            map.put("timestamp", String.valueOf(smsMessage.getTimestampMillis()));
            map.put("index_on_icc", smsMessage.getIndexOnIcc());
            map.put("pdu", StringUtils.bytesToHexString(smsMessage.getPdu()));
            map.put("protocol_identifier", smsMessage.getProtocolIdentifier());
            map.put("pseudo_subject", smsMessage.getPseudoSubject());
            map.put("user_data", Arrays.toString(smsMessage.getUserData()));
            map.put("is_cphs_mwi_message", smsMessage.isCphsMwiMessage());
            map.put("is_mwi_clear_message", smsMessage.isMWIClearMessage());
            map.put("is_mwi_set_message", smsMessage.isMWISetMessage());
            map.put("is_mwi_dont_store", smsMessage.isMwiDontStore());
            map.put("is_replace", smsMessage.isReplace());
            map.put("is_reply_path_present", smsMessage.isReplyPathPresent());
            map.put("is_status_report_message", smsMessage.isStatusReportMessage());
            // Additional processing for user_data
            byte[] userData = smsMessage.getUserData();
            String userDataText = new String(userData); // Assuming the user data is in text format
            map.put("user_data_text", userDataText);
            String json = new Gson().toJson(map);
            LogUtils.show("printSmsMessage "+json);
        } catch (Exception e) {
            LogUtils.show("SmsMessage "+e);
        }

    }





}

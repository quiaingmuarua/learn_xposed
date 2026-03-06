package com.demo.java.xposed.rcs.fake;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.provider.Telephony;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.util.Log;

import com.demo.java.xposed.utils.LogUtils;

import java.util.List;

public class FakerSimMessage {
    private static final String TAG = "FakerSimMessage";



    public static boolean insertFakeMessage(Context context, String address, String message, String serviceCenterNumber) {
        try {
            if (context == null) {
                LogUtils.show("context is null");
                return false;
            }
            if (address == null || message == null) {
                LogUtils.show("address or message is null");
                return false;
            }
            LogUtils.show("insertFakeMessage " + address + " " + message + " " + serviceCenterNumber);
            //Your Messenger verification code is G-048450
            ContentValues values = new ContentValues();
            values.put("address", address);
//            values.put("type", "1"); // 短信类型（1 表示接收到的消息）
            values.put(Telephony.Sms.TYPE, Telephony.Sms.MESSAGE_TYPE_INBOX);

            values.put("read", 0); // 短信阅读状态（0 表示未读）
            values.put("body", message); // 短信内容
            values.put("date", System.currentTimeMillis());
            values.put("date_sent", (System.currentTimeMillis() - 5000) * 1000 / 1000); // 短信时间
            values.put("service_center", serviceCenterNumber);  //  添加服务中心号码
            values.put("sub_id", getDefaultSubId(context)); // 添加订阅ID
//            values.put("status", 0);  // 短信状态
            values.put("protocol", 0);  // 协议类型（例如，0 表示 SMS，1 表示 MMS）
            values.put("reply_path_present", 0);  // 回信路径是否存在
            values.put("error_code", 0);

            Uri uri = Uri.parse(String.valueOf(Telephony.Sms.Inbox.CONTENT_URI));
//            Uri uri = Uri.parse(String.valueOf("content://sms/"));
//            Uri uri = Uri.parse("content://sms/inbox");
            // 插入数据到内容提供者
            Uri insertedUri = context.getContentResolver().insert(uri, values);
            LogUtils.show("Fake message inserted");
            // 检查插入的 URI
            if (insertedUri != null) {
                LogUtils.show("Fake message inserted: " + insertedUri.toString());
                // 通知系统数据库已更新
                context.getContentResolver().notifyChange(Telephony.Sms.CONTENT_URI, null);

                return true;
            } else {
                LogUtils.show("Failed to insert fake message, returned URI is null");
                return false;

            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to insert fake message: ", e);
            return false;
        }


    }


    public static int getDefaultSubId(Context context) {
        SubscriptionManager subscriptionManager = (SubscriptionManager) context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE);

        // 获取默认的短信订阅ID
        int defaultSmsSubId = SubscriptionManager.getDefaultSmsSubscriptionId();

        // 验证默认的订阅ID是否有效
        if (defaultSmsSubId != SubscriptionManager.INVALID_SUBSCRIPTION_ID) {
            return defaultSmsSubId;
        }

        // 如果默认短信订阅ID无效，则获取活动的订阅ID列表
        @SuppressLint("MissingPermission") List<SubscriptionInfo> subscriptionInfoList = subscriptionManager.getActiveSubscriptionInfoList();
        if (subscriptionInfoList != null && !subscriptionInfoList.isEmpty()) {
            return subscriptionInfoList.get(0).getSubscriptionId(); // 返回第一个活动的订阅ID
        }

        return SubscriptionManager.INVALID_SUBSCRIPTION_ID; // 如果没有活动的订阅ID，则返回无效ID
    }



}

/*


2024-05-29 23:02:53.753  1407-3556  ActivityManager         pid-1407                             I  Broadcasting: Intent { act=com.hicloud.android.clone.action.SMS_RESTORE_COMPLETE flg=0x400000 }

 */
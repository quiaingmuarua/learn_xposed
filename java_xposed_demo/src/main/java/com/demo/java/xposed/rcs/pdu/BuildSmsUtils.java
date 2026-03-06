package com.demo.java.xposed.rcs.pdu;


import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.telephony.PhoneNumberUtils;
import android.telephony.SmsManager;

import com.demo.java.xposed.utils.LogUtils;
import com.demo.java.xposed.utils.collection.StringUtils;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

// 短信Intent构造工具
public class BuildSmsUtils {

    private static final String TAG = "BuildSmsUtils";

    public static final String TEST_SMS_RECEIVED = "com.mm.test.sms_received";

    public static void testSendSms(Context context, String number, String content) {
        Intent intent = BuildSmsUtils.getSmsIntent(number, content);
        ;
//        intent.setPackage(context.getPackageName());// "com.example.newsmsapp"
        intent.setAction("android.provider.Telephony.SMS_DELIVER");
        context.sendBroadcast(intent);
    }

    public static Intent getSmsIntent(String number, String body) {
        return getSmsIntent(number, body, 1, "12672593845");

    }

    public  static Intent getSmsIntent(String number, String body, int subId, String serviceCenter) {
        return getSmsIntent(number, body, subId, serviceCenter,"android.provider.Telephony.SMS_DELIVER",4780649518282692879L,null);
    }
    public static  Intent getSmsIntent(String number, String body, int subId, String serviceCenter,String action,Long messageId,ComponentName componentName) {
        return getSmsIntent(number, body, subId, serviceCenter, action, 419430416, messageId,componentName);
    }

    /*
       BugleBroadcastReceiver beforeHookedMethod intent= {"component":"com.google.android.apps.messaging.shared.receiver.SmsDeliverReceiver","flags":419430416,"action":"android.provider.Telephony.SMS_DELIVER","extras":{"android.telephony.extra.SUBSCRIPTION_INDEX":1,"android.telephony.extra.SLOT_INDEX":0,"phone":0,"pdus":[[7,-111,68,-121,32,0,48,35,36,18,-48,-57,-9,-5,-52,46,15,-47,97,58,0,0,66,96,17,49,20,18,64,44,-39,119,93,14,106,-106,-25,-13,-78,-5,92,-106,-125,-20,101,121,-38,-100,30,-121,-23,-23,-73,27,52,126,-109,-53,-96,-12,28,116,108,-63,104,50,89,-19,6]],"format":"3gpp","messageId":4780649518282692879,"subscription":1}}
       BugleBroadcastReceiver beforeHookedMethod intent= {"component":"com.google.android.apps.messaging.shared.receiver.ConfigSmsReceiver","flags":419430416,"action":"android.provider.Telephony.SMS_RECEIVED","extras":{"android.telephony.extra.SUBSCRIPTION_INDEX":1,"android.telephony.extra.SLOT_INDEX":0,"phone":0,"pdus":[[7,-111,68,-121,32,0,48,35,36,18,-48,-57,-9,-5,-52,46,15,-47,97,58,0,0,66,96,17,49,20,18,64,44,-39,119,93,14,106,-106,-25,-13,-78,-5,92,-106,-125,-20,101,121,-38,-100,30,-121,-23,-23,-73,27,52,126,-109,-53,-96,-12,28,116,108,-63,104,50,89,-19,6]],"format":"3gpp","messageId":4780649518282692879,"subscription":1}}

     {"component":"com.google.android.apps.messaging.shared.receiver.SmsDeliverReceiver","flags":419430416,"action":"android.provider.Telephony.SMS_DELIVER","extras":{"android.telephony.extra.SUBSCRIPTION_INDEX":1,"android.telephony.extra.SLOT_INDEX":0,"phone":0,"pdus":[[7,-111,67,6,7,-128,80,-11,36,13,-48,-54,-76,-72,44,29,78,1,0,0,66,96,17,33,34,4,0,44,-39,119,93,14,106,-106,-25,-13,-78,-5,92,-106,-125,-20,101,121,-38,-100,30,-121,-23,-23,-73,27,52,126,-109,-53,-96,-12,28,116,108,-43,110,-79,88,44,6]],"format":"3gpp","messageId":7028905069470922559,"subscription":1}}

    {"component":"com.google.android.apps.messaging.shared.receiver.ConfigSmsReceiver","flags":419430416,"action":"com.google.android.apps.messaging.shared.receiver.ConfigSmsReceiver","extras":{"android.telephony.extra.SUBSCRIPTION_INDEX":1,"android.telephony.extra.SLOT_INDEX":0,"phone":0,"pdus":[[7,-111,67,6,7,-128,112,-11,36,16,-48,-12,-14,-100,30,33,-105,-37,111,0,0,66,96,33,33,32,100,35,7,-29,-15,13,7,-125,-63,0]],"format":"3gpp","messageId":-8173873853569760313,"subscription":1}}
     */

    public static Intent getSmsIntent(String number, String body, int subId, String serviceCenter,String action,int flags,Long messageId,ComponentName componentName) {
        if (body == null) {
            LogUtils.show("sms body is null");
            return null;
        }
        // 防止短信过长
        SmsManager smsManager = SmsManager.getDefault();
        ArrayList<String> messages = smsManager.divideMessage(body);
        int size = messages.size();
        Object[] objArray = new Object[size];
        for (int i = 0; i < size; ++i) {
            byte[] pdu = createFakeSms(number, messages.get(i), serviceCenter);
            objArray[i] = pdu;
        }
//        Uri uri = Uri.parse("http://example.com:37273/resource");
        Intent intent = new Intent();
        intent.setAction(action);
        intent.setComponent(componentName);
        intent.setFlags(flags);
        intent.putExtra("pdus", objArray);
        intent.putExtra("format", "3gpp");
        intent.putExtra("subscription", subId);
        intent.putExtra("messageId", messageId);
        intent.putExtra("android.telephony.extra.SUBSCRIPTION_INDEX",1);
        intent.putExtra("android.telephony.extra.SLOT_INDEX",0);
        intent.putExtra("phone", 0);
//        intent.setData(uri); // 设置 Uri 数据
        return intent;
    }

    // 创建pdu
    // 创建pdu
    public static byte[] createFakeSms(String sender, String body, String serviceCenter) {
        byte[] pdu = null;
        try {

            byte[] scBytes = PhoneNumberUtils.networkPortionToCalledPartyBCD(serviceCenter);

            int lsmcs = scBytes.length;
            ByteArrayOutputStream bo = new ByteArrayOutputStream();
            bo.write(lsmcs);// 短信服务中心长度
            bo.write(scBytes);// 短信服务中心号码
            bo.write(0x24);
//            bo.write(0x04);

            // 处理发送者信息，判断是名字还是号码
            int senderType; // 发送者类型标识
            if (PhoneNumberUtils.isWellFormedSmsAddress(sender)) {

                // 如果发送者是一个有效的号码
                byte[] senderBytes = PhoneNumberUtils.networkPortionToCalledPartyBCD(sender);

                bo.write((byte) sender.length());// 发送方号码长度
                bo.write(senderBytes);// 发送方号码
//                bo.write(0x00);// 协议标示，00为普通GSM，点对点方式
            } else {
                // 如果发送者是名字，使用GSM 7-bit编码
                byte[] senderBytes = StringUtils.HexStringToBytes(GSM.encode(sender,true));
                bo.write((byte) senderBytes.length*2); // 发送方号码长度
                bo.write(0xD0); // 发送方地址的类型
                bo.write(senderBytes); // 发送方地址，名称格式

            }

            // Protocol Identifier (PID)
            bo.write(0x00);

            // Data Coding Scheme (DCS)
            bo.write(0x00);

            byte[] dateBytes = new byte[7];

            Calendar calendar = new GregorianCalendar();
            dateBytes[0] = reverseByte(toBCD(calendar.get(Calendar.YEAR) % 100));
            dateBytes[1] = reverseByte(toBCD(calendar.get(Calendar.MONTH) + 1));
            dateBytes[2] = reverseByte(toBCD(calendar.get(Calendar.DAY_OF_MONTH)));
            dateBytes[3] = reverseByte(toBCD(calendar.get(Calendar.HOUR_OF_DAY)));
            dateBytes[4] = reverseByte(toBCD(calendar.get(Calendar.MINUTE)));
            dateBytes[5] = reverseByte(toBCD(calendar.get(Calendar.SECOND)));
            dateBytes[6] = reverseByte(toBCD((calendar.get(Calendar.ZONE_OFFSET)
                    + calendar.get(Calendar.DST_OFFSET)) / (60 * 1000 * 15)));


            String className = "com.android.internal.telephony.GsmAlphabet";
            Class<?> clazz = Class.forName(className);
            Method method = clazz.getMethod("stringToGsm7BitPacked", String.class);
            method.setAccessible(true);
            byte[] bodybytes = (byte[]) method.invoke(null, body);

//                bo.write(0x00); // TP-DCS Data coding scheme
            bo.write(dateBytes);
            bo.write(bodybytes);


            pdu = bo.toByteArray();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return pdu;
    }

    private static byte reverseByte(byte b) {
        return (byte) ((b & 0xF0) >> 4 | (b & 0x0F) << 4);
    }

    private static byte toBCD(int value) {
        return (byte) ((value / 10 << 4) | (value % 10));
    }


    private static byte[] encodeGsm7Bit(String message) {
        int messageLength = message.length();
        int septetCount = (messageLength * 7 + 7) / 8;
        byte[] pdu = new byte[septetCount];
        int bitOffset = 0;

        for (int i = 0; i < messageLength; i++) {
            char c = message.charAt(i);
            Byte value = GSM_7BIT_MAP.get(c);
            if (value == null) {
                throw new IllegalArgumentException("Unsupported character: " + c);
            }

            int byteOffset = bitOffset / 8;
            int bitPosition = bitOffset % 8;

            pdu[byteOffset] |= (value << bitPosition) & 0xFF;
            if (byteOffset + 1 < pdu.length && bitPosition > 1) {
                pdu[byteOffset + 1] |= (value >> (8 - bitPosition)) & 0xFF;
            }
            bitOffset += 7;
        }

        return pdu;
    }

    private static final Map<Character, Byte> GSM_7BIT_MAP = new HashMap<>();

    static {
        char[] chars = "@£$¥èéùìòÇ\nØø\rÅåΔ_ΦΓΛΩΠΨΣΘΞ\u001BÆæßÉ !\"#¤%&'()*+,-./0123456789:;<=>?¡ABCDEFGHIJKLMNOPQRSTUVWXYZÄÖÑÜ§¿abcdefghijklmnopqrstuvwxyzäöñüà".toCharArray();
        try {
            for (byte i = 0; i < chars.length - 1; i++) {
                GSM_7BIT_MAP.put(chars[i], i);
            }
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }


    }


    private static byte[] encodeUCS2(String message) throws UnsupportedEncodingException {
        byte[] textPart = message.getBytes(StandardCharsets.UTF_16BE);
        byte[] ret = new byte[textPart.length + 1];
        ret[0] = (byte) (textPart.length & 0xff);
        System.arraycopy(textPart, 0, ret, 1, textPart.length);
        return ret;
    }


}

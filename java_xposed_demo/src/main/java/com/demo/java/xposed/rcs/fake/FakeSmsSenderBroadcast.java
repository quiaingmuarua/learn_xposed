package com.demo.java.xposed.rcs.fake;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.demo.java.xposed.utils.LogUtils;

import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class FakeSmsSenderBroadcast {

    private static final String TAG = "FakeSmsSenderBroadcast";

    public static void sendFakeSms(Context context, String address, String message, String serviceCenterNumber, Long timestamp) {
        try {
            LogUtils.show("sendFakeSms: " + address + " " + message + " " + serviceCenterNumber + " " + timestamp);
            String pduSftring = "0791448720005000F0240D916491040000F0000080061102001023B20001";
            byte[] pdu = hexStringToByteArray(pduSftring);
            // Create the intent
            Intent intent = new Intent();
            intent.setAction("android.provider.Telephony.SMS_RECEIVED");
            intent.putExtra("pdus", new Object[]{pdu});
            intent.putExtra("format", "3gpp");

            // Send the broadcast
            context.sendBroadcast(intent);

            Log.d(TAG, "Fake SMS broadcast sent.");

        } catch (Exception e) {

            Log.d(TAG, "Failed to send fake SMS: " + e.getMessage());
        }



    }



    private static byte[] createFakeSmsPdu(String address, String message, String serviceCenterNumber, Long timestamp) {
        try {
            if (timestamp == null) {
                timestamp = System.currentTimeMillis();
            }

            // Encode service center number to semi-octet format
            byte[] scAddressBytes = encodePhoneNumber(serviceCenterNumber);
            int scAddressLength = scAddressBytes.length;

            // Encode address to semi-octet format
            byte[] addressBytes = encodePhoneNumber(address);
            int addressLength = addressBytes.length;

            // Encode message
            byte[] messageBytes = message.getBytes(StandardCharsets.UTF_8);
            int messageLength = messageBytes.length;

            // Create PDU
            byte[] pdu = new byte[1 + scAddressLength + 1 + 1 + 1 + addressLength + 1 + 1 + 1 + messageLength + 7];
            int index = 0;

            // Service Center Address Length
            pdu[index++] = (byte) scAddressLength;
            // Service Center Address
            System.arraycopy(scAddressBytes, 0, pdu, index, scAddressLength);
            index += scAddressLength;

            // PDU Type
            pdu[index++] = (byte) 0x04;
            // Message Reference
            pdu[index++] = (byte) 0x00;
            // Address Length
            pdu[index++] = (byte) (address.length());
            // Address
            System.arraycopy(addressBytes, 0, pdu, index, addressLength);
            index += addressLength;

            // Protocol Identifier
            pdu[index++] = (byte) 0x00;
            // Data Coding Scheme
            pdu[index++] = (byte) 0x00;
            // User Data Length
            pdu[index++] = (byte) messageLength;
            // User Data
            System.arraycopy(messageBytes, 0, pdu, index, messageLength);
            index += messageLength;

            // Encode timestamp
            byte[] timestampBytes = encodeTimestamp(timestamp);
            System.arraycopy(timestampBytes, 0, pdu, index, timestampBytes.length);

            return pdu;
        } catch (Exception e) {
            Log.d(TAG, "Failed to create PDU: " + e.getMessage());
            return null;
        }
    }

    private static byte[] encodePhoneNumber(String phoneNumber) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < phoneNumber.length(); i += 2) {
            if (i + 1 < phoneNumber.length()) {
                result.append(phoneNumber.charAt(i + 1)).append(phoneNumber.charAt(i));
            } else {
                result.append('F').append(phoneNumber.charAt(i));
            }
        }
        return hexStringToByteArray(result.toString());
    }

    private static byte[] encodeTimestamp(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyMMddHHmmss", Locale.US);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        String formattedDate = sdf.format(new Date(timestamp));
        byte[] timestampBytes = new byte[7];
        for (int i = 0; i < 7; i++) {
            int secondDigit = Character.digit(formattedDate.charAt(i * 2), 10);
            int firstDigit = Character.digit(formattedDate.charAt(i * 2 + 1), 10);
            timestampBytes[i] = (byte) ((firstDigit << 4) | secondDigit);
        }
        // Adding timezone information
        timestampBytes[6] |= 0x00; // Timezone byte (here it's set to 0 for UTC)
        return timestampBytes;
    }

    private static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }
}

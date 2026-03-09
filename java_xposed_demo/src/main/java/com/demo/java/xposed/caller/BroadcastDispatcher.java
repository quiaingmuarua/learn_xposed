package com.demo.java.xposed.caller;



import android.content.Context;
import android.content.Intent;

import com.demo.java.xposed.rcs.apiCaller.manager.ActionHandlerManager;
import com.example.messages.ChannelRequestParams;
import com.demo.java.xposed.utils.LogUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class BroadcastDispatcher {
    /*

    am broadcast -a android.provider.Telephony.SMS_RECEIVED -n com.google.android.apps.messaging/.shared.receiver.ConfigSmsReceiver --es "xp_action" "LookupRegistered"

     */

    public static void handle(Context context, Intent intent, ClassLoader classLoader) {

        try {
            String xpAction = intent.getStringExtra("xp_action");
            LogUtils.show("ActionHandlerManager handler intent " + xpAction);
            if (Objects.equals(xpAction, "LookupRegistered")) {
                List<String> phoneNumberList = Arrays.asList("+12043922400", "+14144260101", "+14435620141","+19312619170","+19312619171","+19312619166");
                ActionHandlerManager.handleLookupRegistered(new ChannelRequestParams.Builder().setPhoneNumberList(phoneNumberList).build(), classLoader);
            }

        } catch (Exception e) {
            LogUtils.printStackErrInfo("ActionHandlerManager err " , e);
        }

    }
}

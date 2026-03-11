package com.demo.java.xposed.rcs;

import com.demo.java.xposed.device.PluginInit;
import com.demo.java.xposed.device.TelephonyHook;
import com.demo.java.xposed.rcs.hook.RcsApplicationHook;
import com.demo.java.xposed.rcs.hook.common.GmsCommonHook;
import com.demo.java.xposed.rcs.hook.common.SecurityHook;
import com.demo.java.xposed.system.OkhttpHook;
import com.demo.java.xposed.utils.LogUtils;

import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class RcsHook {


    public static void handleLoadPackage(XC_LoadPackage.LoadPackageParam loadPackageParam)  {
        LogUtils.show("XposedPluginVersion =" + PluginInit.version + " RcsHook handleLoadPackage: " + loadPackageParam.packageName);
        try {
            LogUtils.show("PluginInit info " + PluginInit.info());


            // hook sim卡
            if (!PluginInit.isOriginalSim) {
                LogUtils.show("now is not original sim");
                TelephonyHook.run(loadPackageParam);
            }
            if (PluginInit.mostLess) {
                LogUtils.show("now is most less hook");
                return;
            }

            if (!loadPackageParam.packageName.contains("messaging")) {
                LogUtils.show("now is google service packageName= " + loadPackageParam.packageName);
                return;
            }
            if (loadPackageParam.packageName.equals("com.google.android.apps.messaging")) {

                RcsApplicationHook.run(loadPackageParam);
                OkhttpHook.run(loadPackageParam);
                GmsCommonHook.run(loadPackageParam);
                SecurityHook.run(loadPackageParam);
            }
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

}

/*

89840200011645253939
89840480009188193654
452021164593193
452040913448098
262030873769347
515036321771621
89492261627060983387
89630320143210753454
ProvisioningEngineDataRetriever, Rcs is enabled from user settings: %s, [true]

TrackingLogClass beforeHookedMethod_r SentMessageProcessor: Done sending RCS message{id:100} rcsMessage{id:MxJMuk-8uDSkGlHxujjBhRWg} {x-message-id:-2621569450726393527} conversation{id:8}, status: SUCCEEDED this= bowy@a781641
TrackingLogClass beforeHookedMethod_r RegisterPhoneRpcHandler: Using RCS token in RegisterRequest, failInvalidToken: true this= bowy@15926cc
TrackingLogClass beforeHookedMethod_r SentMessageProcessor: Done sending RCS message{id:37} rcsMessage{id:MxxTftgzt9RBGcjD-LUhm2ew} {x-message-id:-3494075112449722903} conversation{id:5}, status: NO_RETRY this= bowy@fe8c957


DownloadProgressMonitor
handleWindowVisibility: no activity for token android.os.BinderProxy@7e24b4a
 Failed to get RCS configuration from the cached provider, configuration is empty.     zzz
 getPhoneNumberForRcsProvisioningIdOptional: accessing uninitialized provisioning identities.


 com.google
 */
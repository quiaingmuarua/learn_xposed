package com.demo.java.xposed.sekiro;

import android.content.Context;

import com.demo.java.xposed.rcs.apiCaller.core.GrpcCallHelper;
import com.demo.java.xposed.rcs.apiCaller.core.GrpcCallSender;
import com.demo.java.xposed.rcs.apiCaller.core.RcsCommandRegistry;
import com.demo.java.xposed.utils.LogUtils;
import com.example.command.core.CommandContext;
import com.example.messages.XposedCommandRouter;
import com.example.messages.cache.XposedClassCacher;

public class SekiroClientManager {


    public static void initRcsClient(Context context,String product){

        try {
            XposedClassCacher.run(context.getClassLoader());
            CommandContext.init(context);
            GrpcCallHelper.run(context.getClassLoader());

            RcsCommandRegistry.init(context.getClassLoader());
            GrpcCallSender.run();
            XposedCommandRouter.initXposed();
            SekiroClientInit.init(
                    "rcs",
                    context,
                    RcsSekiroActions.createHandlers()
            );
        } catch (Exception e) {
            LogUtils.printStackErrInfo("initRcsClient",e);
        }

    }
}

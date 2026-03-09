package com.example.sekiro.telegram.base;

import com.example.sekiro.util.SimpleLogUtils;

import cn.iinti.sekiro3.business.api.interfaze.SekiroResponse;

public class TelegramRpcExecutor {

    private final TelegramRpcInvoker rpcInvoker;

    public TelegramRpcExecutor(TelegramRpcInvoker rpcInvoker) {
        this.rpcInvoker = rpcInvoker;
    }

    @FunctionalInterface
    public interface RpcRequestSupplier {
        Object get() throws Exception;
    }

    public void execute(
            String actionName,
            String logSuffix,
            long timeoutMs,
            RpcRequestSupplier supplier,
            SekiroResponse resp
    ) throws Exception {
        String prefix = "[" + actionName + "]";
        String extra = (logSuffix == null || logSuffix.trim().isEmpty()) ? "" : ", " + logSuffix;

        SimpleLogUtils.show(prefix + " start" + extra);

        Object request = supplier.get();
        String resultJson = rpcInvoker.sendRequestSync(request, timeoutMs);

        SimpleLogUtils.show(prefix + " success" + extra);
        resp.success(resultJson);
    }
}
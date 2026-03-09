package com.example.sekiro.telegram.base;

import com.example.sekiro.util.SimpleLogUtils;

public class TelegramRpcExecutor {

    private final TelegramRpcInvoker rpcInvoker;

    public TelegramRpcExecutor(TelegramRpcInvoker rpcInvoker) {
        this.rpcInvoker = rpcInvoker;
    }

    @FunctionalInterface
    public interface RpcRequestSupplier {
        Object get() throws Exception;
    }

    public String executeSync(
            String actionName,
            String logSuffix,
            long timeoutMs,
            RpcRequestSupplier supplier
    ) throws Exception {
        String prefix = "[" + actionName + "]";
        String extra = (logSuffix == null || logSuffix.trim().isEmpty()) ? "" : ", " + logSuffix;

        SimpleLogUtils.show(prefix + " start" + extra);

        Object request = supplier.get();
        String resultJson = rpcInvoker.sendRequestSync(request, timeoutMs);

        SimpleLogUtils.show(prefix + " success" + extra);
        return resultJson;
    }
}
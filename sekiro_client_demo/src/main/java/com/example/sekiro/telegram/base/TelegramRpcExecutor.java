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
            TelegramRequestParams params,
            RpcRequestSupplier supplier
    ) throws Exception {
        String prefix = "[" + params.getActionName() + "]";
        String extra = (params.getLogSuffix() == null || params.getLogSuffix().trim().isEmpty())
                ? ""
                : ", " + params.getLogSuffix();

        SimpleLogUtils.show(prefix + " start" + extra);

        Object request = supplier.get();
        String resultJson = rpcInvoker.sendRequestSync(request, params.getTimeoutMs());

        SimpleLogUtils.show(prefix + " success" + extra);
        return resultJson;
    }
}
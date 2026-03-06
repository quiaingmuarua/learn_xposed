package com.example.sekiro.telegram;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class PendingRequest {

    private final String requestId;
    private final CountDownLatch latch = new CountDownLatch(1);
    private volatile String resultJson;
    private volatile String errorMsg;
    private final AtomicBoolean completed = new AtomicBoolean(false);

    public PendingRequest(String requestId) {
        this.requestId = requestId;
    }

    public String getRequestId() {
        return requestId;
    }

    public void success(String json) {
        if (completed.compareAndSet(false, true)) {
            this.resultJson = json;
            latch.countDown();
        }
    }

    public void fail(String errorMsg) {
        if (completed.compareAndSet(false, true)) {
            this.errorMsg = errorMsg;
            latch.countDown();
        }
    }

    public boolean await(long timeoutMs) throws InterruptedException {
        return latch.await(timeoutMs, TimeUnit.MILLISECONDS);
    }

    public boolean isSuccess() {
        return completed.get() && errorMsg == null;
    }

    public String getResultJson() {
        return resultJson;
    }

    public String getErrorMsg() {
        return errorMsg;
    }
}
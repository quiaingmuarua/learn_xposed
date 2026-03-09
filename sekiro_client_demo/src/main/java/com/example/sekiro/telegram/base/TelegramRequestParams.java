package com.example.sekiro.telegram.base;


public class TelegramRequestParams {

    private final String actionName;
    private final String logSuffix;
    private final long timeoutMs;

    public TelegramRequestParams(String actionName, String logSuffix, long timeoutMs) {
        this.actionName = actionName;
        this.logSuffix = logSuffix;
        this.timeoutMs = timeoutMs;
    }

    public String getActionName() {
        return actionName;
    }

    public String getLogSuffix() {
        return logSuffix;
    }

    public long getTimeoutMs() {
        return timeoutMs;
    }
}
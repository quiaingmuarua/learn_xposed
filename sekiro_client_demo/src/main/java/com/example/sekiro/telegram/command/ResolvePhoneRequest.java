package com.example.sekiro.telegram.command;


public class ResolvePhoneRequest {
    private final String phone;
    private final long timeoutMs;

    public ResolvePhoneRequest(String phone, long timeoutMs) {
        this.phone = phone;
        this.timeoutMs = timeoutMs;
    }

    public String getPhone() {
        return phone;
    }

    public long getTimeoutMs() {
        return timeoutMs;
    }
}
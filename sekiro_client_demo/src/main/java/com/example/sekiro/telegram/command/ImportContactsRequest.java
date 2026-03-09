package com.example.sekiro.telegram.command;

import com.example.sekiro.telegram.model.ImportContactItem;

import java.util.List;

public class ImportContactsRequest {
    private final java.util.List<ImportContactItem> contacts;
    private final long timeoutMs;

    public ImportContactsRequest(List<ImportContactItem> contacts, long timeoutMs) {
        this.contacts = contacts;
        this.timeoutMs = timeoutMs;
    }

    public List<ImportContactItem> getContacts() {
        return contacts;
    }

    public long getTimeoutMs() {
        return timeoutMs;
    }
}
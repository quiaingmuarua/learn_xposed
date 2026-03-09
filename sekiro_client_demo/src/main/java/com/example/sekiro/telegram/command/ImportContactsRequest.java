package com.example.sekiro.telegram.command;

import com.example.sekiro.telegram.model.ImportContactItem;

import java.util.List;

public class ImportContactsRequest implements TelegramCommandRequest {
    private final List<ImportContactItem> contacts;
    private final long timeoutMs;

    public ImportContactsRequest(List<ImportContactItem> contacts, long timeoutMs) {
        this.contacts = contacts;
        this.timeoutMs = timeoutMs;
    }

    public List<ImportContactItem> getContacts() {
        return contacts;
    }

    @Override
    public long getTimeoutMs() {
        return timeoutMs;
    }
}
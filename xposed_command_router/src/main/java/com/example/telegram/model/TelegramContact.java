package com.example.telegram.model;


public class TelegramContact {
    private long clientId;
    private String phone;
    private String firstName;
    private String lastName;

    private long userId;
    private long accessHash;

    public TelegramContact() {
    }

    public TelegramContact(long clientId, String phone, String firstName, String lastName) {
        this.clientId = clientId;
        this.phone = phone;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public TelegramContact(long userId, long accessHash) {
        this.userId = userId;
        this.accessHash = accessHash;
    }

    public long getClientId() {
        return clientId;
    }

    public void setClientId(long clientId) {
        this.clientId = clientId;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public long getAccessHash() {
        return accessHash;
    }

    public void setAccessHash(long accessHash) {
        this.accessHash = accessHash;
    }

    public boolean canImport() {
        return phone != null && !phone.trim().isEmpty();
    }

    public boolean canDelete() {
        return userId != 0L && accessHash != 0L;
    }
}
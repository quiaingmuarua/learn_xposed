package com.example.sekiro.telegram.model;


public class ImportContactItem {
    private long clientId;
    private String phone;
    private String firstName;
    private String lastName;

    public ImportContactItem() {
    }

    public ImportContactItem(long clientId, String phone, String firstName, String lastName) {
        this.clientId = clientId;
        this.phone = phone;
        this.firstName = firstName;
        this.lastName = lastName;
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
}
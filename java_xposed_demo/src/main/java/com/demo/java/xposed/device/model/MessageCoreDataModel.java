package com.demo.java.xposed.device.model;

import androidx.annotation.NonNull;

public class MessageCoreDataModel {

    String messageId;
    String text;
    String sender;


    public MessageCoreDataModel(String messageId,String text,String sender) {
        this.messageId = messageId;
        this.text = text.substring(0, Math.min(text.length(), 1000));
        this.sender = sender;
    }


    public String getMessageId() {
        return messageId;
    }

    public String getText() {
        return text;
    }

    @NonNull
    @Override
    public String toString() {
        return "MessageCoreData{" +
                "messageId='" + messageId + '\'' + ", text='" + text + '\'' + ", sender='" + sender + '\'' +
                '}';
    }
}

package com.demo.java.xposed.rcs.model;

import androidx.annotation.NonNull;

import com.demo.java.xposed.rcs.enums.MsgStatusEnum;

public class MsgItem {


    private String sender;
    private String receiver;
    private String content;
    private String messageId;
    private String groupId;
    //SENDING SENT DELIVERED  SEEN
    private String status = MsgStatusEnum.MSG_INIT.getStatus();

    private Long createTime = System.currentTimeMillis();

    private Long updateTime = System.currentTimeMillis();

    public MsgItem(String messageId) {
        this.messageId = messageId;
    }

    public MsgItem(String sender, String receiver, String content, String messageId) {
        this.sender = sender;
        this.receiver = receiver;
        this.content = content.substring(0, Math.min(content.length(), 10));
        this.messageId = messageId;
    }


    public void setSender(String sender) {
        this.sender = sender;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver.replace("+", "");
    }

    public void setContent(String content) {
        this.content = content.substring(0, Math.min(content.length(), 10));
    }


    public Long getCreateTime() {
        return createTime;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessageId() {
        return messageId;
    }


    public String getStatus() {
        return status == null ? MsgStatusEnum.MSG_SEND.getStatus() : status;
    }


    public void setUpdateTime(Long updateTime) {
        this.updateTime = updateTime;
    }


    public void setGroupId(String groupId) {
        this.groupId = groupId;

    }
    public String getGroupId() {
        return groupId;
    }


    @NonNull
    public  String toString() {
        return "MsgItem{" +
                "sender='" + sender + '\'' +
                ", receiver='" + receiver + '\'' +
                ", content='" + content + '\'' +
                ", messageId='" + messageId + '\'' +
                ", groupId='" + groupId + '\'' +
                ", status='" + status + '\'' +
                ", createTime=" + createTime +
                ", updateTime=" + updateTime +
                '}';
    }
}

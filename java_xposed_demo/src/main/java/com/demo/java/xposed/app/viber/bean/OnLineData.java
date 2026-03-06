package com.demo.java.xposed.app.viber.bean;


public class OnLineData {
    public boolean isOnLine;
    public String memberId;
    public long time;



    public String getMemberId() {
        return memberId;
    }

    public void setMemberId(String memberId) {
        this.memberId = memberId;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }
}

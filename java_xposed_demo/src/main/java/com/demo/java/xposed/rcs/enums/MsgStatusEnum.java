package com.demo.java.xposed.rcs.enums;

public enum MsgStatusEnum {
    MSG_INIT("INIT"),
    MSG_SEND("SENT"),
    MSG_DELIVERED("DELIVERED"),
    MSG_SEEN("SEEN"),
    MSG_FAILED("FAILED");

    private String status;




  private MsgStatusEnum(String status) {
    this.status = status;
  }

  public String getStatus() {
    return status;
  }

}

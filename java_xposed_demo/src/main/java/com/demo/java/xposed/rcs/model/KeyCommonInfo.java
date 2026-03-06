package com.demo.java.xposed.rcs.model;

public class KeyCommonInfo {


    public static class Status {

        // 状态 200 成功
        public static int machineState = 100;

        // 标签 send 、 register
        public static int pluginStatus = 100;

    }


    public static class Tag {
        public static String info="info"; //展示设备信息
        public static String send = "send";
        public static String register = "register";
        public static  String environment = "environment";
        public static String proto="proto";
    }

}

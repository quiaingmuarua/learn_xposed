package com.example.sekiro.messages.model;


import java.util.Map;

public class ApiRequest {

    int task_type;
    Map<String,String> task_content;
    String phone;
    int status;

    public ApiRequest(int task_type, Map<String,String> task_content, String phone, int status) {
        this.task_type = task_type;
        this.task_content = task_content;
        this.phone = phone;
        this.status = status;
    }

    public ApiRequest(int task_type, String phone, int status) {
        this.task_type = task_type;
        this.phone = phone;
        this.status = status;
    }


}

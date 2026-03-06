package com.demo.java.xposed.rcs.apiCaller.model;

import androidx.annotation.NonNull;

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


    @NonNull
    public String toString(){
        return "task_type:"+task_type+",task_content:"+task_content+",phone:"+phone+",status:"+status;
    }
}

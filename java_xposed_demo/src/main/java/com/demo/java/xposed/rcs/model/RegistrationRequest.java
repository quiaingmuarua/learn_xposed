package com.demo.java.xposed.rcs.model;

import com.demo.java.xposed.device.PluginInit;
import com.google.gson.Gson;

import java.util.Map;

public class RegistrationRequest {
    private  static  GmsSignConfigModel gmsSignConfigModel=GmsSignConfigModel.getInstance();
    private String requests_version;
    private String sig_method;
    private String sig_method_name;
    private Map<String, String> sig_params ;
    private FilterParams filter_params;
    private String deviceId;
    private String key;

    // Getters and Setters
    public String getRequests_version() {
        return requests_version;
    }

    public void setRequests_version(String requests_version) {
        this.requests_version = requests_version;
    }

    public String getSig_method() {
        return sig_method;
    }

    public void setSig_method(String sig_method) {
        this.sig_method = sig_method;
    }

    public String getSig_method_name() {
        return sig_method_name;
    }

    public void setSig_method_name(String sig_method_name) {
        this.sig_method_name = sig_method_name;
    }

    public Map<String, String> getSig_params() {
        return sig_params;
    }

    public void setSig_params(Map<String, String> sig_params) {
        this.sig_params = sig_params;
    }

    public FilterParams getFilter_params() {
        return filter_params;
    }

    public void setFilter_params(FilterParams filter_params) {
        this.filter_params = filter_params;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }


    // Inner class for filter_params
    public static class FilterParams {
        private String version;
        private String sig_app_name;
        private String phone_number;
        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }

        public String getSig_app_name() {
            return sig_app_name;
        }

        public String getPhone_number() {
            return phone_number;
        }

        public void setPhone_number(String phone_number) {
            this.phone_number = phone_number;
        }

        public void setSig_app_name(String sig_app_name) {
            this.sig_app_name = sig_app_name;
        }
    }

    public static RegistrationRequest getVersion1(String sig_method, Map<String, String> sig_params) {
        RegistrationRequest request = new RegistrationRequest();
        request.setRequests_version("1");
        request.setSig_method(sig_method);
        request.setSig_method_name("droidGuard");
        request.setDeviceId(PluginInit.deviceId);
        request.setKey("2e&5g18i.Ike");

        // 设置 sig_params
        request.sig_params = sig_params;
        // 设置 filter_params
        RegistrationRequest.FilterParams filterParams = new RegistrationRequest.FilterParams();
        filterParams.setVersion(gmsSignConfigModel.getGmsConfig().getVersion());
        filterParams.setSig_app_name(gmsSignConfigModel.getGmsConfig().getSigAppName());
        request.setFilter_params(filterParams);

        return request;
    }


    public static RegistrationRequest getVersion100(String sig_method, Map<String, String> sig_params) {

        RegistrationRequest request = new RegistrationRequest();
        request.setRequests_version("1");
        request.setSig_method(sig_method);
        request.setSig_method_name("droidGuard");
        request.setDeviceId(PluginInit.deviceId);
        request.setKey("2e&5g18i.Ike");

        // 设置 sig_params
        request.sig_params = sig_params;
        // 设置 filter_params
        RegistrationRequest.FilterParams filterParams = new RegistrationRequest.FilterParams();
        filterParams.setVersion(gmsSignConfigModel.getRegisterConfig().getVersion());
        filterParams.setSig_app_name(gmsSignConfigModel.getRegisterConfig().getSigAppName());
        request.setFilter_params(filterParams);

        return request;
    }


    public String toJsonStr() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }
}

package com.demo.java.xposed.rcs.hook.gms;

import com.demo.java.xposed.rcs.model.GmsSignConfigModel;
import com.demo.java.xposed.rcs.model.ResponseData;
import com.demo.java.xposed.rcs.model.SigEnv;
import com.demo.java.xposed.utils.LogUtils;
import com.demo.java.xposed.utils.OkHttpUtil;
import com.google.gson.Gson;

import java.util.Map;
import java.util.Objects;

public class DroidGuardSign {
    private  static final GmsSignConfigModel gmsSignConfigModel=GmsSignConfigModel.getInstance();
    public static String urlPath = "https://dwxa.rcsxaf.xyz/rcs_websocket_data";
    public static String testUrlPath = "https://dwxa.rcsxaf.xyz/rcs_websocket_data_test";


    public static ResponseData auto_ensure_droidGuard_sign(String app,String requestBody, int retryCnt){
        String path=urlPath;
        if(Objects.equals(app, "gms")){
          GmsSignConfigModel.GmsConfig gmsConfig= gmsSignConfigModel.getGmsConfig();
          if(gmsConfig.isUseLocalSign()){
              LogUtils.show("auto_ensure_droidGuard_sign gms use localSign");
              return null;
          }
          if (gmsConfig.isDebug()){
              path=testUrlPath;
          }
          return  ensure_droidGuard_sign(path,requestBody,retryCnt);
        }
        if (Objects.equals(app,"register")){
            GmsSignConfigModel.RegisterConfig registerConfig= gmsSignConfigModel.getRegisterConfig();
            if(registerConfig.isUseLocalSign()){
                LogUtils.show("auto_ensure_droidGuard_sign register use localSign");
                return null;
            }
            if(registerConfig.isDebug()){
                path=testUrlPath;
            }
            return  ensure_droidGuard_sign(path,requestBody,retryCnt);

        }
        LogUtils.show("auto_ensure_droidGuard_sign unknown app "+app);
        return null;
    }

    public static ResponseData ensure_droidGuard_sign(String urlPath, String requestBody, int retryCnt) {

        LogUtils.printParams("ensure_droidGuard_sign " ,urlPath,requestBody,retryCnt);
        ResponseData responseData = null;
        for (int i = 0; i < retryCnt; i++) {
            try {

                responseData = droidGuard_sign_once(urlPath, requestBody);
                break;
            } catch (Exception e) {
                LogUtils.show("droid_guard_sign error " + e.getMessage() + "retry_times=" + i);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ex) {
                    throw new RuntimeException(ex);
                }
            }
        }

        //now is use self data
        SigEnv.saveSigEnv(responseData);
        return responseData;

    }

    private static ResponseData droidGuard_sign_once(String url, String requestBody) throws Exception {

        LogUtils.show("droid_guard_sign requestBody = " + requestBody);
//        if (!Objects.equals(requestData.get("METHOD"), "rcs_provisioning")) {
//            return null;
//        }
        //https://test.wasfish.com/rcs  https://admin.rcsqf.info/rcs_websocket_data
//                        String result = OkHttpUtil.sendPostRequestSync("https://admin.rcsqf.info/rcs_websocket_data", requestBody);
//        String result = OkHttpUtil.sendPostRequestSync("https://dwxa.rcsxaf.xyz/rcs_websocket_data", requestBody);
        String result = OkHttpUtil.sendPostRequestSync(url, requestBody);
//                        String result =OkHttpUtil.sendPostRequestSync("http://34.143.182.93:7007/test/request",requestBody);
        LogUtils.show("droid_guard_sign result= " + result);
        Gson gson = new Gson();
        ResponseData responseData = gson.fromJson(result, ResponseData.class);
        if (responseData == null) {
            throw new Exception("droid_guard_sign responseData is null");
        }
        Map<String, String> data = responseData.getData();
        if (data.isEmpty()) {
            throw new Exception("droid_guard_sign data is empty");
        }
        String resultData = data.get("result");
        assert resultData != null;
        if (resultData.isEmpty() || resultData.length() < 10) {
            throw new Exception("droid_guard_sign resultData is empty");
        }
        LogUtils.show("droid_guard_sign url=" + url + " len=" + resultData.length() + " resultData=" + resultData);
        return responseData;

    }

}

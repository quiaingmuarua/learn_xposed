package com.demo.java.xposed.device;

import com.demo.java.xposed.utils.LogUtils;
import com.demo.java.xposed.utils.ShellUtils;
import com.google.gson.Gson;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class PluginInit {
    public static String version = "xp26-311";

    public static boolean isRegistered=false;

    public static  boolean isTest = checkProp("rcs_test")||checkFile("/data/local/tmp/rcs_test.txt");

    public static boolean isDebug = checkProp("rcs_debug") || checkFile("/data/local/tmp/rcs_debug.txt");

    public static boolean isOriginalSim = checkProp("rcs_original_sim") || checkFile("/data/local/tmp/raw_sim.txt");

    public static boolean rawGmsSign = checkProp("raw_gms_sign") || checkFile("/data/local/tmp/raw_gms_sign.txt");
    public static  boolean mostLess =checkProp("most_less_hook")  ||checkFile("/data/local/tmp/most_less_hook.txt");

    public static  String deviceId= ShellUtils.getSystemProperty("device_id");
    public static String targetVersion =ShellUtils.getSystemProperty("rcs_version");

//    public static String phoneNumber= SimInfoModel.getInstance().getPhoneNumber();

    private static boolean  checkProp(String prop) {
        try {
            String rcsDebug = ShellUtils.getSystemProperty(prop);
            if (!rcsDebug.isEmpty()) {
                LogUtils.show(prop + " " + rcsDebug);
                return true;
            }

            return false;
        }catch (Exception e){
            LogUtils.show("checkProp exception "+e);
        }
        return false;

    }

    /**
     * 检测指定路径的文件是否存在
     *
     * @param filePath 文件路径
     * @return 如果文件存在返回 true，否则返回 false
     */
    public static boolean checkFile(String filePath) {
        try {
            if (filePath == null || filePath.isEmpty()) {
                return false;
            }
            File file = new File(filePath);
            return file.exists() && file.isFile();
        }catch (Exception e){
            return false;
        }

    }

    public static String gsonInfo(){
        Gson gson=new Gson();

       Map<String,Object> PluginInitInfo=new HashMap<>();
        PluginInitInfo.put("version",version);

        PluginInitInfo.put("isRegistered",isRegistered);
        return gson.toJson(PluginInitInfo);
    }

    public static String info(){
        return "isDebug:"+isDebug+" isOriginalSim:"+isOriginalSim +"deviceId:"+deviceId+" targetVersion:"+targetVersion;
    }

}


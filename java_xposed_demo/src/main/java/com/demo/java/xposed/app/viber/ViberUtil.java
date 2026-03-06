package com.demo.java.xposed.app.viber;

import com.demo.java.xposed.app.viber.bean.FilterPhoneNumberBean;
import com.demo.java.xposed.utils.LogUtils;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import de.robv.android.xposed.XposedHelpers;

public class ViberUtil {
    /**
     * getGenerateSequence
     */
    public static int getGenerateSequence(ClassLoader mClassLoader) {
        int data = 0;
        try {
            Class<?> aClass = XposedHelpers.findClass("com.viber.voip.ViberApplication", mClassLoader);
            Object viberApplication = XposedHelpers.callStaticMethod(aClass, "getInstance");
            Object getEngine = XposedHelpers.callMethod(viberApplication, "getEngine", true);
            Object phoneControll = XposedHelpers.callMethod(getEngine, "getPhoneController");
            data = (int) XposedHelpers.callMethod(phoneControll, "generateSequence");
        } catch (Throwable throwable) {
            LogUtils.show("eeee===>" + throwable.getMessage());
        }
        return data;
    }

    /**
     * 触发筛号方法
     */
    public static void filterPhoneNumber(ClassLoader classLoader, String[] data) {
        try {
            LogUtils.show("filterPhoneNumber success");

            Class<?> cGetUsersDetailsV2Msg = XposedHelpers.findClass("com.viber.jni.im2.CGetUsersDetailsV2Msg", classLoader);
            LogUtils.show("cGetUsersDetailsV2Msg success");
            //获取obj
            Object obj = XposedHelpers.newInstance(cGetUsersDetailsV2Msg, data, 0, getGenerateSequence(classLoader));
            //先获取application类
            Class<?> aClass = XposedHelpers.findClass("com.viber.voip.ViberApplication", classLoader);
            //viberApplication的构造方法
            Object viberApplication = XposedHelpers.callStaticMethod(aClass, "getInstance");
            LogUtils.show("viberApplication success");

            //获取com.viber.jni.Engine
            Object getEngine = XposedHelpers.callMethod(viberApplication, "getEngine", true);
            LogUtils.show("getEngine success");
            //获取com.viber.jni.im2.Im2Exchanger
            Object getExchanger = XposedHelpers.callMethod(getEngine, "getExchanger");
            LogUtils.show("getExchanger success");
            XposedHelpers.callMethod(getExchanger, "handleCGetUsersDetailsV2Msg", obj);
            LogUtils.show("callMethod success");

        } catch (Throwable t) {
            LogUtils.show("error_sendRequest===>" + t.getMessage());
        }
    }

    /**
     * 获取离线时间
     */
    public static String handleGetLastOnline(ClassLoader classLoader, List<String> data) {
        String message = "";
        try {
            //先获取application类
            Class<?> aClass = XposedHelpers.findClass("com.viber.voip.ViberApplication", classLoader);
            //viberApplication的构造方法
            Object viberApplication = XposedHelpers.callStaticMethod(aClass, "getInstance");
            //获取com.viber.jni.Engine
            Object getEngine = XposedHelpers.callMethod(viberApplication, "getEngine", true);
            //获取com.viber.jni.im2.Im2Exchanger
            Object getOnlineUserActivityHelper = XposedHelpers.callMethod(getEngine, "getOnlineUserActivityHelper");
            Object obj = XposedHelpers.callMethod(getOnlineUserActivityHelper, "obtainInfo", data);
            Gson gson = new Gson();
             message = gson.toJson(obj);
        } catch (Throwable t) {
            LogUtils.show("sendRequest===>" + t.getMessage());
        }
        return message;
    }

    /**
     * 获取当前是否连接
     */
    public static boolean handleGetIsConnect(ClassLoader classLoader) {
        try {
            //先获取application类
            Class<?> aClass = XposedHelpers.findClass("com.viber.voip.ViberApplication", classLoader);
            Object viberApplication = XposedHelpers.callStaticMethod(aClass, "getInstance");
            Object getEngine = XposedHelpers.callMethod(viberApplication, "getEngine", true);
            Object phoneControll = XposedHelpers.callMethod(getEngine, "getPhoneController");
            boolean isConnected = (boolean) XposedHelpers.callMethod(phoneControll, "isConnected");
            return isConnected;
        } catch (Throwable t) {
            LogUtils.show("sendRequest===>" + t.getMessage());
        }
        return false;
    }

    /**
     * 获取当前是否登录
     *
     * @param classLoader
     */
    public static boolean handleGetPersonalProfile(ClassLoader classLoader) {
        try {
            //先获取application类
            Class<?> aClass = XposedHelpers.findClass("com.viber.voip.ViberApplication", classLoader);
            Object viberApplication = XposedHelpers.callStaticMethod(aClass, "getInstance");
            Object getEngine = XposedHelpers.callMethod(viberApplication, "getEngine", true);
            Object phoneControll = XposedHelpers.callMethod(getEngine, "getPhoneController");
            boolean handleGetPersonalProfile = (boolean) XposedHelpers.callMethod(phoneControll, "handleGetPersonalProfile");
            return handleGetPersonalProfile;
        } catch (Throwable t) {
            LogUtils.show("sendRequest===>" + t.getMessage());
        }
        return false;
    }

    private static int status = 1;   //status=1时代表注册过viber
    private static final String EMPTY_MESSAGE = "{}";

    /**
     * 处理viber筛号返回数据
     *
     * @param jsonArray
     * @param classLoader
     * @param cmd
     * @return
     * @throws Exception
     */
    public static JsonObject handleViberData(JsonArray jsonArray, ClassLoader classLoader, String cmd) throws Exception {

        List<FilterPhoneNumberBean> listPhoneNumberBean = new ArrayList<>();
        List<String> listMid = new ArrayList<>();

        JsonObject dataJSON = new JsonObject();

        for (int k = 0; k < jsonArray.size(); k++) {
            JsonObject jsonObject1 = jsonArray.get(k).getAsJsonObject();

            // 只返回 status = 1（有效）的数据
            if (jsonObject1.get("Status").getAsInt() == status) {

                FilterPhoneNumberBean filterPhoneNumberBean = new FilterPhoneNumberBean();

                String phoneNumber = jsonObject1.get("PhoneNumber").getAsString();
                String MID = jsonObject1.get("MID").getAsString();

                filterPhoneNumberBean.setPhoneNumber(phoneNumber);
                filterPhoneNumberBean.setMemberId(MID);

                listPhoneNumberBean.add(filterPhoneNumberBean);
                listMid.add(MID);
            }
        }

        Gson gson = new Gson();

        // List → JsonElement
        dataJSON.add("phoneNumberData", gson.toJsonTree(listPhoneNumberBean));
        dataJSON.addProperty("EffectiveNum", listPhoneNumberBean.size());

        return dataJSON;
    }


    public static String getPhone (String msg){
        String phone = "";
        try {
            String REGEX = "[^0-9]";
            Pattern.compile(REGEX).matcher(msg).replaceAll("").trim();
              phone = Pattern.compile(REGEX).matcher(msg).replaceAll("").trim();
        }catch (Exception e){

        }
      return phone;
    }
}

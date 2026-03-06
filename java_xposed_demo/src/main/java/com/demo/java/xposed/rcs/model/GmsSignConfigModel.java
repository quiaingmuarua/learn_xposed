package com.demo.java.xposed.rcs.model;

import com.demo.java.xposed.utils.LogUtils;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.annotations.SerializedName;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class GmsSignConfigModel {
    private static final GmsSignConfigModel INSTANCE = init();

    @SerializedName("register_config")
    private RegisterConfig registerConfig;

    @SerializedName("gms_config")
    private GmsConfig gmsConfig;

    public RegisterConfig getRegisterConfig() {
        return registerConfig;
    }

    public void setRegisterConfig(RegisterConfig registerConfig) {
        this.registerConfig = registerConfig;
    }

    public GmsConfig getGmsConfig() {
        return gmsConfig;
    }

    public void setGmsConfig(GmsConfig gmsConfig) {
        this.gmsConfig = gmsConfig;
    }

    @Override
    public String toString() {
        return "GmsSignConfigModel{" +
                "registerConfig=" + registerConfig +
                ", gmsConfig=" + gmsConfig +
                '}';
    }


    public static class RegisterConfig {
        @SerializedName("use_local_sign")
        private boolean useLocalSign = false;

        @SerializedName("is_debug")
        private boolean isDebug = false;

        @SerializedName("version")
        private String version;

        @SerializedName("sig_app_name")
        private String sigAppName;

        public boolean isUseLocalSign() {
            return useLocalSign;
        }

        public void setUseLocalSign(boolean useLocalSign) {
            this.useLocalSign = useLocalSign;
        }

        public boolean isDebug() {
            return isDebug;
        }

        public void setDebug(boolean debug) {
            isDebug = debug;
        }

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }

        public String getSigAppName() {
            return sigAppName;
        }

        public void setSigAppName(String sigAppName) {
            this.sigAppName = sigAppName;
        }

        @Override
        public String toString() {
            return "RegisterConfig{" +
                    "useLocalSign=" + useLocalSign +
                    ", isDebug=" + isDebug +
                    ", version='" + version + '\'' +
                    ", sigAppName='" + sigAppName + '\'' +
                    '}';
        }
    }

    public static class GmsConfig {
        @SerializedName("is_debug")
        private boolean isDebug = false;

        @SerializedName("use_local_sign")
        private boolean useLocalSign = false;

        @SerializedName("version")
        private String version = "1";

        @SerializedName("sig_app_name")
        private String sigAppName;

        public boolean isDebug() {
            return isDebug;
        }

        public void setDebug(boolean debug) {
            isDebug = debug;
        }

        public boolean isUseLocalSign() {
            return useLocalSign;
        }

        public void setUseLocalSign(boolean useLocalSign) {
            this.useLocalSign = useLocalSign;
        }

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }

        public String getSigAppName() {
            return sigAppName;
        }

        public void setSigAppName(String sigAppName) {
            this.sigAppName = sigAppName;
        }

        @Override
        public String toString() {
            return "GmsConfig{" +
                    "isDebug=" + isDebug +
                    ", useLocalSign=" + useLocalSign +
                    ", version='" + version + '\'' +
                    ", sigAppName='" + sigAppName + '\'' +
                    '}';
        }
    }

    public static GmsSignConfigModel getInstance() {
        return INSTANCE;
    }


    public static GmsSignConfigModel init() {
        LogUtils.show("GmsSignConfigModel init");
        GmsSignConfigModel gmsSignConfigModel = loadConfigFromFile();
        if (gmsSignConfigModel == null) {
            LogUtils.show("GmsSignConfigModel use default config");
        } else {
            return gmsSignConfigModel;
        }
        GmsSignConfigModel defaultGmsSignConfigModel = new GmsSignConfigModel();
        GmsConfig gmsConfig1 = new GmsConfig();
        gmsConfig1.setDebug(false);
        gmsConfig1.setUseLocalSign(false);
        gmsConfig1.setVersion("1");
//        gmsConfig1.setSigAppName("gms");
        RegisterConfig registerConfig1 = new RegisterConfig();
//        registerConfig1.setVersion("100");
//        registerConfig1.setSigAppName("messaging");
        registerConfig1.setUseLocalSign(true);
        defaultGmsSignConfigModel.setGmsConfig(gmsConfig1);
        defaultGmsSignConfigModel.setRegisterConfig(registerConfig1);
        LogUtils.show("GmsSignConfigModel init= " + defaultGmsSignConfigModel);
        return defaultGmsSignConfigModel;


    }


    public static GmsSignConfigModel loadConfigFromFile() {
        String filePath = "/data/local/tmp/sig_config.json";
        File file = new File(filePath);

        if (!file.exists()) {
            LogUtils.show("配置文件不存在: " + filePath);
            return null;
        }

        try (FileReader reader = new FileReader(file)) {
            Gson gson = new Gson();
            return gson.fromJson(reader, GmsSignConfigModel.class);
        } catch (JsonSyntaxException e) {
            LogUtils.show("JSON语法错误: " + e.getMessage());
        } catch (IOException e) {
            LogUtils.show("读取文件失败: " + e.getMessage());
        }

        return null;
    }
}

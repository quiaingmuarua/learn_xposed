package com.demo.java.xposed.rcs.apiCaller.model;

import com.demo.java.xposed.rcs.shared.CommandException;
import com.demo.java.xposed.rcs.shared.ErrorCode;
import com.demo.java.xposed.utils.LogUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.List;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

public class ChannelRequestParams {

    private final String groupId;
    private final List<String> phoneNumberList;
    private final XpGrpcMethodEnum xpGrpcMethodEnum;
    private final  String tokenHex;

    // 私有构造函数
    private ChannelRequestParams(Builder builder) {
        this.groupId = builder.groupId;
        this.phoneNumberList = builder.phoneNumberList;
        this.xpGrpcMethodEnum = builder.xpGrpcMethodEnum;
        this.tokenHex = builder.tokenHex;
    }

    // Getter 方法
    public String getGroupId() {
        return groupId;
    }

    public List<String> getPhoneNumberList() {
        return phoneNumberList;
    }

    public XpGrpcMethodEnum getXpGrpcMethodEnum() {
        return xpGrpcMethodEnum;
    }

    public String getTokenHex() {
        return tokenHex;
    }


    // Builder 静态内部类
    public static class Builder {
        private String groupId;
        private List<String> phoneNumberList;
        private XpGrpcMethodEnum xpGrpcMethodEnum;
        private String tokenHex;

        // 定义一个 Map 映射 HTTP 方法到验证逻辑
        private static final Map<XpGrpcMethodEnum, BiConsumer<List<String>, String>> validatorMap = new HashMap<>();

        static {
            // 注册每个 HTTP 方法对应的验证逻辑
            validatorMap.put(XpGrpcMethodEnum.ADD_GROUP_USERS, ChannelRequestParams::checkPhoneNumbers);
            validatorMap.put(XpGrpcMethodEnum.KICKOFF, ChannelRequestParams::checkPhoneNumbers);
            validatorMap.put(XpGrpcMethodEnum.LOOKUP, ChannelRequestParams::checkPhoneNumbers);
            // 其他方法的参数校验可以继续扩展
        }

        public Builder setXpGrpcMethodEnum(XpGrpcMethodEnum xpGrpcMethodEnum) {
            this.xpGrpcMethodEnum = xpGrpcMethodEnum;
            return this;
        }

        public Builder setGroupId(String groupId) {
            this.groupId = groupId;
            return this;
        }

        public Builder setPhoneNumberList(String rawPhoneParam) {
            this.phoneNumberList =  getPhoneNumberList(rawPhoneParam);
            return this;
        }

        public Builder setTokenHex(String tokenHex) {
            this.tokenHex = tokenHex;
            return this;
        }


        public Builder setPhoneNumberList(List<String> phoneNumberList) {
            this.phoneNumberList =  phoneNumberList;
            return this;
        }

        // 自动根据请求类型来验证和设置参数
        public Builder autoSetParams() {
            if (xpGrpcMethodEnum != null && validatorMap.containsKey(xpGrpcMethodEnum)) {
                // 获取对应的验证逻辑并执行
                Objects.requireNonNull(validatorMap.get(xpGrpcMethodEnum)).accept(phoneNumberList, groupId);
            }
            return this;
        }

        public ChannelRequestParams build() {
            return new ChannelRequestParams(this);
        }
    }


    private static List<String> getPhoneNumberList(String rawPhoneParam) {
        if (rawPhoneParam == null) {
            return null;
        }

        String decoded;
        try {
            decoded = URLDecoder.decode(rawPhoneParam, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new CommandException(ErrorCode.PARSE_ERROR, "解码 'phones' 失败");
        }
        List<String> phoneList = Arrays.stream(decoded.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(ChannelRequestParams::normalizePhone)
                .collect(Collectors.toList());
        LogUtils.show("[CommandRouter] 标准化后的 phoneList = " + phoneList);
        return phoneList;
    }

    private static String normalizePhone(String raw) {
        raw = raw.replaceAll("[^\\d+]", ""); // 去除非数字字符
        return raw.startsWith("+") ? raw : "+" + raw;
    }


    public static void  checkPhoneNumbers(List<String> phoneNumberList,String groupId) {
        if (phoneNumberList == null || phoneNumberList.isEmpty()) {
            throw new IllegalArgumentException("Phone numbers are required.");
        }
    }

    @Override
    public String toString() {
        return "ChannelRequestParams{" +
                "groupId='" + groupId + '\'' +
                ", phoneNumberList=" + phoneNumberList +
                ", xpGrpcMethodEnum=" + xpGrpcMethodEnum +
                '}';
    }
}

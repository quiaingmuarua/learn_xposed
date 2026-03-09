package com.demo.java.xposed.rcs.apiCaller.cache;


import com.example.sekiro.messages.model.XpGroupInfo;
import com.demo.java.xposed.utils.LogUtils;
import com.google.gson.Gson;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

public class CachedGroupInfo {
    private static final Map<String, XpGroupInfo> groupMap = new HashMap<>();

    private static  String curGroupId;

    public static XpGroupInfo getOrCreate(String groupId) {
        return groupMap.computeIfAbsent(groupId, XpGroupInfo::new);
    }


    public static XpGroupInfo getFromConversationId(String conversationId) {
        if (conversationId == null) return null;
        for (XpGroupInfo info : groupMap.values()) {
            if (conversationId.equals(info.getConversationId())) {
                return info;
            }
        }
        return null;
    }


    public static void setRrsConferencePropertiesBytes(String groupId, byte[] rcsConferencePropertiesHex) {
        getOrCreate(groupId).setRrsConferencePropertiesBytes(rcsConferencePropertiesHex);
    }

    public static byte[] getRrsConferencePropertiesBytes(String groupId) {
        return getOrCreate(groupId).getRrsConferencePropertiesBytes();
    }

    public static void addDeliveredMember(String groupId, String member) {
        getOrCreate(groupId).addDeliveredMember(member);
    }


    public static void addSentMembers(String groupId,List<String> members) {
        LogUtils.show("addSentMembers groupId = " + groupId + "members = " + members +" size" + members.size());
        // 使用 TreeSet 来存储唯一的成员
        getOrCreate(groupId).addSentMembers( (members.stream()
                .map(String::trim) // 去除每个手机号的前后空格
                .collect(Collectors.toSet())))
               ; // 收集到一个 Set 中
    }



    public static Map<String, Set<String>> getDeliveredGroupMembersMap() {
        Map<String, Set<String>> map = new HashMap<>();
        for (Map.Entry<String, XpGroupInfo> entry : groupMap.entrySet()) {
            map.put(entry.getKey(), new TreeSet<>(entry.getValue().getDeliveredGroupMembers()));
        }
        return map;
    }

    public static Map<String, Set<String>> getSentGroupMembersMap() {
        Map<String, Set<String>> map = new HashMap<>();
        for (Map.Entry<String, XpGroupInfo> entry : groupMap.entrySet()) {
            map.put(entry.getKey(), new TreeSet<>(entry.getValue().getSentGroupMembers()));
        }
        return map;
    }

    public static void clearAllDeliveredMembers() {
        groupMap.values().forEach(XpGroupInfo::clearDeliveredMembers);
    }

    public static void updateCurGroupId(String groupId) {
        curGroupId = groupId;
    }

    public static  String getCurGroupId() {
        return curGroupId;
    }


    public static void clearAllSentMembers() {
        groupMap.values().forEach(XpGroupInfo::clearSentMembers);
    }

    public static String toCachJsonString(){

        Gson gson=new Gson();
        return gson.toJson(groupMap);
    }

}

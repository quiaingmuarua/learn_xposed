package com.example.messages.model;


import java.util.Set;
import java.util.TreeSet;

public class XpGroupInfo {

    private final String groupId;
    private final Set<String> deliveredGroupMembers = new TreeSet<>();
    private final Set<String> sentGroupMembers = new TreeSet<>();
    private byte[] rcsConferencePropertiesBytes;

    private final Set<String> groupMembers = new TreeSet<>();

    private  String conversationId;


    public XpGroupInfo(String groupId) {
        this.groupId = groupId;
    }

    public String getGroupId() {
        return groupId;
    }

    public Set<String> getDeliveredGroupMembers() {
        return deliveredGroupMembers;
    }

    public Set<String> getSentGroupMembers() {
        return sentGroupMembers;
    }

    public void addDeliveredMember(String member) {
        deliveredGroupMembers.add(member.trim());
    }

    public void addSentMembers(Set<String> members) {
        members.stream().map(String::trim).forEach(sentGroupMembers::add);
    }

    public void clearDeliveredMembers() {
        deliveredGroupMembers.clear();
    }

    public void clearSentMembers() {
        sentGroupMembers.clear();
    }

    public byte[] getRrsConferencePropertiesBytes() {
        return rcsConferencePropertiesBytes;
    }

    public void setRrsConferencePropertiesBytes(byte[] rcsConferencePropertiesHex) {
        this.rcsConferencePropertiesBytes = rcsConferencePropertiesHex;
    }


    public void  addGroupMember(String member){

        this.groupMembers.add(member);
    }

    public void addGroupMembers(Set<String> members){
        this.groupMembers.addAll(members);
    }

    public Set<String> getGroupMember(){
        return groupMembers;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }

    public String getConversationId() {
        return conversationId;
    }
}

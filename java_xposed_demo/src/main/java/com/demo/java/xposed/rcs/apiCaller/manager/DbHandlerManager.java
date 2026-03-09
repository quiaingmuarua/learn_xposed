package com.demo.java.xposed.rcs.apiCaller.manager;

import com.demo.java.xposed.utils.LogUtils;
import com.example.sekiro.messages.cache.CacheMessageInfo;
import com.example.sekiro.messages.model.DbQueryParams;
import com.example.sekiro.messages.shared.CommandException;
import com.example.sekiro.messages.shared.ErrorCode;

import java.util.ArrayList;
import java.util.List;

import de.robv.android.xposed.XposedHelpers;

public class DbHandlerManager {


    public static List<String> deleteConversation(DbQueryParams dbQueryParams, ClassLoader classLoader) throws Exception {
        try {

            LogUtils.show("DbHandlerManager handleQueryLastTable dbQueryParams = " + dbQueryParams.getConversationId());

            deleteAction(classLoader,"conversations","(conversations._id = ?)",new String[]{dbQueryParams.getConversationId()});
            deleteAction(classLoader,"messages","(messages._id IN (SELECT messages._id FROM messages WHERE ((messages.conversation_id = ?) AND (messages.received_timestamp <= ?))))",new String[]{dbQueryParams.getConversationId(),String.valueOf(System.currentTimeMillis())});
        }catch (Exception e){
            LogUtils.printStackErrInfo("handleQueryLastTable Exception = ",e);
        }
        return new ArrayList<>();


    }

    public static String deleteAllConversations(DbQueryParams dbQueryParams, ClassLoader classLoader) throws Exception {
        deleteAction(classLoader,"conversations","(conversations._id > ?)",new String[]{"0"});
        deleteAction(classLoader,"messages","(messages._id > ?)",new String[]{"0"});
        return  "ok";
    }


    public static void deleteAction(ClassLoader classLoader,String tableName,String where,String[] whereArgs) throws Exception {
        Object databaseInterfaceImpl = CacheMessageInfo.getInstance().getDatabaseInterfaceImpl();
        if(databaseInterfaceImpl==null){
         throw new CommandException(ErrorCode.PARSE_ERROR,"databaseInterfaceImpl is null");
        }
        Class<?> DeleteParametersClass = classLoader.loadClass("csrk");
        Class<?> cstuClass = classLoader.loadClass("cstu");
        Object cstu= XposedHelpers.newInstance(cstuClass,"delete"+tableName);
        Object deleteParameters= XposedHelpers.newInstance(DeleteParametersClass,tableName,false,cstu,"$primary");
        XposedHelpers.callMethod(databaseInterfaceImpl,"a",tableName,where,whereArgs,deleteParameters);
    }






}

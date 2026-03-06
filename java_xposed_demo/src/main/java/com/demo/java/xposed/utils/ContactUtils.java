package com.demo.java.xposed.utils;

import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.Context;
import android.content.OperationApplicationException;
import android.net.Uri;
import android.os.RemoteException;
import android.provider.ContactsContract;

import java.util.ArrayList;

public class ContactUtils {

    public static void addContact(Context context ,String phoneNumber) throws RemoteException, OperationApplicationException {

        //gen random displayName
        String displayName=RandomUtils.randomAlphabetic(5);
        addContact(context,displayName,phoneNumber);

    }

    public static void addContact(Context context, String displayName, String phoneNumber) throws RemoteException, OperationApplicationException {

            ArrayList<ContentProviderOperation> ops = new ArrayList<>();

            int rawContactInsertIndex = ops.size();

            ops.add(ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
                    .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                    .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
                    .build());

            // 添加姓名
            ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactInsertIndex)
                    .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                    .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, displayName)
                    .build());

            // 添加电话
            ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactInsertIndex)
                    .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                    .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, phoneNumber)
                    .withValue(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE)
                    .build());

            ContentResolver resolver = context.getContentResolver();
            resolver.applyBatch(ContactsContract.AUTHORITY, ops);
    }


    public static void deleteAllContacts(Context context) {

        ContentResolver resolver = context.getContentResolver();
        Uri uri = ContactsContract.RawContacts.CONTENT_URI;

        // 删除所有 RawContacts（包括相关 Data 会级联删除）
        int deletedCount = resolver.delete(uri, null, null);
        LogUtils.show("deleteAllContacts", "Deleted contacts count: " + deletedCount);

    }
}

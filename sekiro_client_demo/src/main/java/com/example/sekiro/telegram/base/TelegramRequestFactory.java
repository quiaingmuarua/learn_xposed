package com.example.sekiro.telegram.base;

import com.example.sekiro.telegram.model.ImportContactItem;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class TelegramRequestFactory {

    private final TelegramEnv env;

    public TelegramRequestFactory(TelegramEnv env) {
        this.env = env;
    }

    public Object createResolvePhoneRequest(String phoneNumber) throws Exception {
        Class<?> clazz = env.loadClass("org.telegram.tgnet.TLRPC$TL_contacts_resolvePhone");
        Object req = clazz.getDeclaredConstructor().newInstance();

        Field phoneField = clazz.getDeclaredField("phone");
        phoneField.setAccessible(true);
        phoneField.set(req, phoneNumber);

        return req;
    }

    public Object createImportContactsRequest(List<ImportContactItem> items) throws Exception {
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("contacts is empty");
        }

        Class<?> reqClass = env.loadClass("org.telegram.tgnet.TLRPC$TL_contacts_importContacts");
        Class<?> inputPhoneContactClass = env.loadClass("org.telegram.tgnet.TLRPC$TL_inputPhoneContact");

        Object req = reqClass.getDeclaredConstructor().newInstance();

        Field contactsField = reqClass.getDeclaredField("contacts");
        contactsField.setAccessible(true);

        List<Object> contacts = new ArrayList<>();

        for (int i = 0; i < items.size(); i++) {
            ImportContactItem item = items.get(i);
            Object contact = inputPhoneContactClass.getDeclaredConstructor().newInstance();

            setField(inputPhoneContactClass, contact, "client_id", buildClientId(item, i));
            setField(inputPhoneContactClass, contact, "phone", safe(item.getPhone()));
            setField(inputPhoneContactClass, contact, "first_name", safe(item.getFirstName()));
            setField(inputPhoneContactClass, contact, "last_name", safe(item.getLastName()));

            contacts.add(contact);
        }

        contactsField.set(req, contacts);
        return req;
    }

    private long buildClientId(ImportContactItem item, int index) {
        long base = item.getClientId();
        if (base == 0L) {
            base = System.currentTimeMillis();
        }
        return base | (((long) index) << 32);
    }

    private void setField(Class<?> clazz, Object target, String fieldName, Object value) throws Exception {
        Field field = clazz.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }
}
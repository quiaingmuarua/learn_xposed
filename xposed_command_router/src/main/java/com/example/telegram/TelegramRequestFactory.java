package com.example.telegram;

import com.example.command.core.CommandContext;
import com.example.telegram.model.TelegramContact;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class TelegramRequestFactory {

    private final CommandContext context;

    public TelegramRequestFactory(CommandContext context) {
        this.context = context;
    }

    public Object createResolvePhoneRequest(String phoneNumber) throws Exception {
        Class<?> clazz = context.loadClass("org.telegram.tgnet.TLRPC$TL_contacts_resolvePhone");
        Object req = clazz.getDeclaredConstructor().newInstance();

        Field phoneField = clazz.getDeclaredField("phone");
        phoneField.setAccessible(true);
        phoneField.set(req, phoneNumber);

        return req;
    }

    public Object createImportContactsRequest(List<TelegramContact> items) throws Exception {
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("contacts is empty");
        }

        Class<?> reqClass = context.loadClass("org.telegram.tgnet.TLRPC$TL_contacts_importContacts");
        Class<?> inputPhoneContactClass = context.loadClass("org.telegram.tgnet.TLRPC$TL_inputPhoneContact");

        Object req = reqClass.getDeclaredConstructor().newInstance();

        Field contactsField = reqClass.getDeclaredField("contacts");
        contactsField.setAccessible(true);

        List<Object> contacts = new ArrayList<>();

        for (int i = 0; i < items.size(); i++) {
            TelegramContact item = items.get(i);
            if (!item.canImport()) {
                continue;
            }

            Object contact = inputPhoneContactClass.getDeclaredConstructor().newInstance();

            setField(inputPhoneContactClass, contact, "client_id", buildClientId(item, i));
            setField(inputPhoneContactClass, contact, "phone", safe(item.getPhone()));
            setField(inputPhoneContactClass, contact, "first_name", safe(item.getFirstName()));
            setField(inputPhoneContactClass, contact, "last_name", safe(item.getLastName()));

            contacts.add(contact);
        }

        if (contacts.isEmpty()) {
            throw new IllegalArgumentException("contacts is empty");
        }

        contactsField.set(req, contacts);
        return req;
    }

    public Object createDeleteContactsRequest(List<TelegramContact> items) throws Exception {
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("delete contacts is empty");
        }

        Class<?> reqClass = context.loadClass("org.telegram.tgnet.TLRPC$TL_contacts_deleteContacts");
        Class<?> inputUserClass = context.loadClass("org.telegram.tgnet.TLRPC$TL_inputUser");

        Object req = reqClass.getDeclaredConstructor().newInstance();

        Field idField = reqClass.getDeclaredField("id");
        idField.setAccessible(true);

        List<Object> inputUsers = new ArrayList<>();

        for (TelegramContact item : items) {
            if (!item.canDelete()) {
                continue;
            }

            Object inputUser = inputUserClass.getDeclaredConstructor().newInstance();

            setField(inputUserClass, inputUser, "user_id", item.getUserId());
            setField(inputUserClass, inputUser, "access_hash", item.getAccessHash());

            inputUsers.add(inputUser);
        }

        if (inputUsers.isEmpty()) {
            throw new IllegalArgumentException("delete contacts is empty");
        }

        idField.set(req, inputUsers);
        return req;
    }

    private long buildClientId(TelegramContact item, int index) {
        long base = item.getClientId();
        if (base == 0L) {
            base = System.currentTimeMillis() & 0xFFFFFFFFL;
        }
        return (((long) index) << 32) | (base & 0xFFFFFFFFL);
    }

    private void setField(Class<?> clazz, Object target, String fieldName, Object value) throws Exception {
        Field field = clazz.getField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }
}
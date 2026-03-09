package com.example.sekiro.telegram.base;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collection;
import java.util.Map;

public class TelegramResponseSerializer {

    public String toJson(Object tlObject) throws JSONException {
        TLJsonLike.Options opt = new TLJsonLike.Options();
        opt.maxDepth = 10;
        opt.maxCollectionSize = 80;
        opt.maxBytesPreview = 128;

        Map<String, Object> map = TLJsonLike.toMap(tlObject, opt);
        return toJsonObject(map).toString();
    }

    private JSONObject toJsonObject(Map<String, Object> map) throws JSONException {
        JSONObject obj = new JSONObject();

        for (Map.Entry<String, Object> entry : map.entrySet()) {
            obj.put(entry.getKey(), convert(entry.getValue()));
        }

        return obj;
    }

    private Object convert(Object value) throws JSONException {
        if (value instanceof Map) {
            return toJsonObject((Map<String, Object>) value);
        } else if (value instanceof Collection) {
            JSONArray arr = new JSONArray();
            for (Object v : (Collection<?>) value) {
                arr.put(convert(v));
            }
            return arr;
        } else {
            return value;
        }
    }
}
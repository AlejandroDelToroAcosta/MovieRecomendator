package org.tscd.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class JsonUtils {
    public static int getSafeInt(JsonObject obj, String key) {
        return obj.has(key) && !obj.get(key).isJsonNull() ? obj.get(key).getAsInt() : 0;
    }

    private static int getSafeString(JsonObject obj, String key) {
        return obj.has(key) && !obj.get(key).isJsonNull() ? obj.get(key).getAsInt() : 0;
    }
    public static double getSafeDouble(JsonObject obj, String key) {
        if (obj == null) return 0.0;
        return obj.has(key) && !obj.get(key).isJsonNull() ? obj.get(key).getAsDouble() : 0.0;
    }
    public static JsonArray getSafeArray(JsonObject obj, String key) {
        if (obj == null) return new JsonArray();
        return obj.has(key) && obj.get(key).isJsonArray() ? obj.getAsJsonArray(key) : new JsonArray();
    }

}

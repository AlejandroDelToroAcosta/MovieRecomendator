package org.tscd.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JsonUtilsTest {

    @Test
    void testGetSafeInt_presentAndAbsentKeys() {
        JsonObject obj = new JsonObject();
        obj.addProperty("value", 10);
        obj.add("nullValue", null);

        assertEquals(10, JsonUtils.getSafeInt(obj, "value"));
        assertEquals(0, JsonUtils.getSafeInt(obj, "missing"));
        assertEquals(0, JsonUtils.getSafeInt(obj, "nullValue"));
    }

    @Test
    void testGetSafeDouble_presentAndAbsentKeys() {
        JsonObject obj = new JsonObject();
        obj.addProperty("value", 5.5);

        assertEquals(5.5, JsonUtils.getSafeDouble(obj, "value"));
        assertEquals(0.0, JsonUtils.getSafeDouble(obj, "missing"));
        assertEquals(0.0, JsonUtils.getSafeDouble(null, "any"));
    }

    @Test
    void testGetSafeArray_presentAndAbsentKeys() {
        JsonObject obj = new JsonObject();
        JsonArray array = new JsonArray();
        array.add(1);
        obj.add("array", array);

        JsonArray result = JsonUtils.getSafeArray(obj, "array");
        assertEquals(1, result.size());

        JsonArray missing = JsonUtils.getSafeArray(obj, "missing");
        assertEquals(0, missing.size());

        JsonArray nullObj = JsonUtils.getSafeArray(null, "any");
        assertEquals(0, nullObj.size());
    }
}

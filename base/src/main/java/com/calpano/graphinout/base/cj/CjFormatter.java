package com.calpano.graphinout.base.cj;

import com.calpano.graphinout.base.graphml.CjGraphmlMapping;
import com.calpano.graphinout.foundation.json.JsonConstants;
import com.calpano.graphinout.foundation.json.stream.impl.Json2StringWriter;
import com.calpano.graphinout.foundation.json.value.IJsonArrayMutable;
import com.calpano.graphinout.foundation.json.value.IJsonContainer;
import com.calpano.graphinout.foundation.json.value.IJsonObjectMutable;
import com.calpano.graphinout.foundation.json.value.IJsonValue;
import com.calpano.graphinout.foundation.json.value.java.JavaJsonArray;
import com.calpano.graphinout.foundation.json.value.java.JavaJsonObject;
import com.calpano.graphinout.foundation.json.value.java.JavaJsonValues;

import java.util.ArrayList;
import java.util.List;

import static com.calpano.graphinout.foundation.util.Nullables.ifPresentAccept;

public class CjFormatter {

    static final String SCHEMA = """
            "$schema":"https://calpano.github.io/connected-json/_attachments/cj-schema.json","$id":"https://j-s-o-n.org/schema/connected-json/5.0.0",""";

    public static String canonicalize(String cjJson) {
        IJsonValue jsonValue = JavaJsonValues.ofJsonString(cjJson);
        traverse(List.of(), jsonValue);
        Json2StringWriter w = new Json2StringWriter();
        jsonValue.fire(w);
        return w.jsonString();
    }

    /**
     * @param cjJson
     * @return
     */
    public static String stripCjHeader(String cjJson) {
        return canonicalize(cjJson);
    }

    private static void transformArrayPost(List<Object> steps, IJsonArrayMutable arrayValue) {

    }

    private static void transformArrayPre(List<Object> steps, IJsonArrayMutable arrayValue) {

    }

    private static void transformObjectPost(List<Object> steps, IJsonObjectMutable o) {
        ifPresentAccept(o.get(CjConstants.DATA), data -> {
            if (data.isContainer()) {
                IJsonContainer c = data.asContainer();
                if (c.isEmpty()) {
                    o.removeProperty(CjConstants.DATA);
                }
            }
        });
    }

    private static void transformObjectPre(List<Object> steps, IJsonObjectMutable o) {
        // strip connectedJson document metadata
        if (o.hasProperty(CjConstants.ROOT__CONNECTED_JSON)) {
            o.removeProperty(CjConstants.ROOT__CONNECTED_JSON);
        }
        // strip JSON schema refs
        if (o.hasProperty(JsonConstants.DOLLAR_ID)) {
            o.removeProperty(JsonConstants.DOLLAR_ID);
        }
        if (o.hasProperty(JsonConstants.DOLLAR_SCHEMA)) {
            o.removeProperty(JsonConstants.DOLLAR_SCHEMA);
        }
        ifPresentAccept(o.get(CjGraphmlMapping.CjDataProperty.CustomXmlAttributes.cjPropertyKey), value -> (IJsonObjectMutable) value.asObject().asMutable(), customXmlAttributes -> {
            customXmlAttributes.keys().stream().filter(k -> k.startsWith("parse.")).toList() //
                    .forEach(customXmlAttributes::removeProperty);
            if (customXmlAttributes.isEmpty()) {
                o.removeProperty(CjGraphmlMapping.CjDataProperty.CustomXmlAttributes.cjPropertyKey);
            }
        });
    }

    /**
     * @param steps     ordered from root to leaf
     * @param jsonValue
     * @return
     */
    private static void traverse(List<Object> steps, IJsonValue jsonValue) {
        if (jsonValue.isPrimitive()) return;
        else if (jsonValue.isObject()) {
            JavaJsonObject o = (JavaJsonObject) jsonValue;
            transformObjectPre(steps, o);
            o.forEach((k, v) -> {
                List<Object> s = new ArrayList<>(steps);
                s.add(k);
                traverse(s, v);
            });
            transformObjectPost(steps, o);
        } else if (jsonValue.isArray()) {
            JavaJsonArray a = (JavaJsonArray) jsonValue;
            transformArrayPre(steps, a);
            a.forEach((v, i) -> {
                List<Object> s = new ArrayList<>(steps);
                s.add(i);
                traverse(s, v);
            });
            transformArrayPost(steps, a);
        } else throw new IllegalArgumentException("Not a known JSON type: " + jsonValue.getClass());
    }

}

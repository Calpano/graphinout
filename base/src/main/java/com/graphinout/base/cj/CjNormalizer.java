package com.graphinout.base.cj;

import com.graphinout.base.graphml.CjGraphmlMapping;
import com.graphinout.foundation.json.JsonConstants;
import com.graphinout.foundation.json.stream.impl.Json2StringWriter;
import com.graphinout.foundation.json.value.IJsonArrayMutable;
import com.graphinout.foundation.json.value.IJsonContainer;
import com.graphinout.foundation.json.value.IJsonObjectMutable;
import com.graphinout.foundation.json.value.IJsonValue;
import com.graphinout.foundation.json.value.JsonPaths;
import com.graphinout.foundation.json.value.java.JavaJsonValues;

import java.util.List;

import static com.graphinout.foundation.util.Nullables.ifPresentAccept;

public class CjNormalizer {

    private static final JsonTransformer.IJsonTransformHandler HANDLER = new JsonTransformer.IJsonTransformHandler() {
        public void transformArrayPost(List<Object> steps, IJsonArrayMutable arrayValue) {

        }

        public void transformArrayPre(List<Object> steps, IJsonArrayMutable arrayValue) {

        }

        public void transformObjectPost(List<Object> steps, IJsonObjectMutable o) {
            // remove empty "data" {} objects
            ifPresentAccept(o.get(CjConstants.DATA), data -> {
                if (data.isContainer()) {
                    IJsonContainer c = data.asContainer();
                    if (c.isEmpty()) {
                        o.removeProperty(CjConstants.DATA);
                    }
                }
            });
            // remove "graph"/"data"/"parse.*" keys (graphml parseinfo)
            if (JsonPaths.endsWith(steps, //
                    s -> s.equals(CjConstants.GRAPHS),
                    s -> s instanceof Integer,
                    s -> s.equals(CjConstants.DATA))) {
                // remove all keys starting with "parse."
                o.removePropertyIf(k -> k.startsWith("parse."));
            }
        }

        public void transformObjectPre(List<Object> steps, IJsonObjectMutable o) {
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
    };
    private final String resultJson;

    public CjNormalizer(String cjJson) {
        //TODO parse to CjDoc, but this requires reader-cj, which depends still on reader-graphml for the gio bridge

        IJsonValue jsonValue = JavaJsonValues.ofJsonString(cjJson);
        // is mutated in-place
        new JsonTransformer(HANDLER).traverse(jsonValue);

        Json2StringWriter w = new Json2StringWriter();
        jsonValue.fire(w);
        this.resultJson = w.jsonString();
    }

    public static String canonicalize(String cjJson) {
        CjNormalizer normalizer = new CjNormalizer(cjJson);
        return normalizer.resultJson;
    }


}

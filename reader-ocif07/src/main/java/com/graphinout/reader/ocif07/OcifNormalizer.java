package com.graphinout.reader.ocif07;

import com.graphinout.base.json.JavaJsons;
import com.graphinout.foundation.pure.input.ContentErrors;
import com.graphinout.foundation.pure.json.JsonTransformer;
import com.graphinout.foundation.pure.json.document.IJsonArrayMutable;
import com.graphinout.foundation.pure.json.document.IJsonObjectMutable;
import com.graphinout.foundation.pure.json.document.IJsonValue;
import com.graphinout.foundation.pure.json.formatter.JsonCompactFormatter;
import com.graphinout.reader.ocif07.document.impl.OcifDocument;

import java.util.List;

public class OcifNormalizer {

    private static final JsonTransformer.IJsonTransformHandler HANDLER = new JsonTransformer.IJsonTransformHandler() {
        public void transformArrayPost(List<Object> steps, IJsonArrayMutable arrayValue) {
        }

        public void transformArrayPre(List<Object> steps, IJsonArrayMutable arrayValue) {
        }

        public void transformObjectPost(List<Object> steps, IJsonObjectMutable o) {
        }

        public void transformObjectPre(List<Object> steps, IJsonObjectMutable o) {
            // some files have this set, some dont
            o.removeProperty("ocif");
        }
    };
    private final String resultJson;

    public OcifNormalizer(String ocifJson, int maxLineLength) {
        if (ocifJson.isBlank()) {
            this.resultJson = "";
            return;
        }

        // ignoring normalisation errors
        IJsonValue jsonValue = JavaJsons.ofJsonString(ocifJson);
        OcifDocument ocifDoc = Json2OcifDoc.toOcifDocument(jsonValue, ContentErrors.createNoop());

        // is mutated/normalized in-place
        new JsonTransformer(HANDLER).traverse(jsonValue);
        Object jaJson = OcifDoc2Json.toJaJson(ocifDoc);
        this.resultJson = JsonCompactFormatter.formatCompact(jaJson, maxLineLength);
    }

    public static String canonicalize(String ocifJson, int maxLineLength) {
        OcifNormalizer normalizer = new OcifNormalizer(ocifJson,maxLineLength);
        return normalizer.resultJson;
    }


}

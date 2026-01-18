package com.graphinout.base.cj.document;

import com.graphinout.base.json.JsonReaderImpl;
import com.graphinout.foundation.pure.json.document.IJsonValue;
import com.graphinout.foundation.pure.json.writer.impl.Json2StringWriter;

import javax.annotation.concurrent.Immutable;

@Immutable
public interface ICjEdgeType {

    static ICjEdgeType fromJsonString(String json) {
        IJsonValue value = JsonReaderImpl.readToJsonValue(json);
        return ICjEdgeType.of(value.asString());
    }

    static ICjEdgeType of(String type) {
        return () -> type;
    }

    static String toJsonString(ICjEdgeType edgeType) {
        Json2StringWriter w = new Json2StringWriter();
        w.onString(edgeType.type());
        return w.jsonString();
    }

    String type();

}

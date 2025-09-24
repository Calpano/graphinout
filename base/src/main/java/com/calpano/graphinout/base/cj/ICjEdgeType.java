package com.calpano.graphinout.base.cj;

import com.calpano.graphinout.foundation.json.stream.impl.Json2StringWriter;
import com.calpano.graphinout.foundation.json.stream.impl.JsonReaderImpl;
import com.calpano.graphinout.foundation.json.value.IJsonObject;
import com.calpano.graphinout.foundation.json.value.IJsonValue;

public interface ICjEdgeType {

    static ICjEdgeType fromJsonString( String json ) {
        IJsonValue value = JsonReaderImpl.readToJsonValue(json);
        IJsonObject object = value.asObject();
        CjEdgeTypeSource source = CjEdgeTypeSource.valueOf(object.get("source").asString());
        String type = object.get("type").asString();
        return ICjEdgeType.of(source, type);
    }

    static String toJsonString(ICjEdgeType edgeType) {
        Json2StringWriter w = new Json2StringWriter();
        w.objectStart();
        w.onKey( "source");
        w.onString( edgeType.source().name());
        w.onKey("type");
        w.onString(edgeType.type());
        w.objectEnd();
        return w.jsonString();
    }

    static ICjEdgeType of(CjEdgeTypeSource source, String type) {
        return new ICjEdgeType() {
            @Override
            public CjEdgeTypeSource source() {
                return source;
            }

            @Override
            public String type() {
                return type;
            }
        };
    }

    CjEdgeTypeSource source();

    String type();

}

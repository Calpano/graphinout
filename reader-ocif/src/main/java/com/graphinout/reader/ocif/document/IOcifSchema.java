package com.graphinout.reader.ocif.document;

import com.graphinout.foundation.pure.input.ContentError;
import com.graphinout.foundation.pure.json.document.IJsonObject;
import com.graphinout.foundation.pure.json.document.IJsonObjectMutable;
import com.graphinout.foundation.pure.json.document.IJsonValue;
import com.graphinout.reader.ocif.OCIF;
import com.graphinout.reader.ocif.document.impl.OcifSchema;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.function.Consumer;

import static com.graphinout.foundation.pure.functional.Nullables.ifPresentAccept;
import static com.graphinout.reader.ocif.Ocifs.factory;

/**
 * An OCIF schema is defined by its URI.
 */
public interface IOcifSchema extends IDecorateJsonObject {

    static IOcifSchema of(String uri) {
        OcifSchema schema = new OcifSchema();
        schema.uri(uri);
        return schema;
    }

    static IJsonObject schemaToJson(IOcifSchema schema) {
        IJsonObjectMutable o = factory().createObjectMutable();
        o.setString(OCIF.Schema.URI, schema.uri());
        ifPresentAccept(schema.location(), v -> o.setString(OCIF.Schema.LOCATION, v));
        ifPresentAccept(schema.name(), v -> o.setString(OCIF.Schema.NAME, v));
        return o;
    }

    static @NonNull IOcifSchema toOcifSchema(IJsonObject soj, Consumer<ContentError> errorHandler) {
        OcifSchema sch = new OcifSchema();
        ifPresentAccept(soj.get(OCIF.Schema.URI), IJsonValue::asString, sch::uri);
        ifPresentAccept(soj.get(OCIF.Schema.SCHEMA), IJsonValue::asObject, sch::schema);
        ifPresentAccept(soj.get(OCIF.Schema.LOCATION), IJsonValue::asString, sch::location);
        ifPresentAccept(soj.get(OCIF.Schema.NAME), IJsonValue::asString, sch::name);
        return sch;
    }

    @Nullable String location();

    @Nullable String name();

    @Nullable IJsonObject schema();

    @NonNull String uri();

}

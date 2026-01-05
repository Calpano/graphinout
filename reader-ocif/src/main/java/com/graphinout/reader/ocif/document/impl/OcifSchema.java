package com.graphinout.reader.ocif.document.impl;

import com.graphinout.foundation.pure.json.document.IJsonObject;
import com.graphinout.foundation.pure.json.document.IJsonValue;
import com.graphinout.reader.ocif.document.IOcifSchema;
import com.graphinout.reader.ocif.document.IOcifSchemaMutable;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Map;
import java.util.Set;

import static com.graphinout.reader.ocif.OCIF.Schema.LOCATION;
import static com.graphinout.reader.ocif.OCIF.Schema.NAME;
import static com.graphinout.reader.ocif.OCIF.Schema.SCHEMA;
import static com.graphinout.reader.ocif.OCIF.Schema.URI;
import static com.graphinout.reader.ocif.Ocifs.factory;

/**
 * OCIF Schema declaration.
 * <p>
 * Spec excerpts (schema.json $defs.schema):
 * <ul>
 *   <li>uri (string, required): Identifier (and location) of the schema.</li>
 *   <li>schema (object): The actual JSON schema as a JSON object.</li>
 *   <li>location (string): Storage location for the schema.</li>
 *   <li>name (string): Optional short name for the schema.</li>
 * </ul>
 */
public class OcifSchema implements IOcifSchemaMutable {

    private String uri;
    /** embedded JSON schema */
    private IJsonObject schema;
    private String location;
    private String name;

    @Override
    public Set<String> definedKeys() {
        return Set.of(URI, SCHEMA, LOCATION, NAME);
    }

    @Override
    public @Nullable String location() {return location;}

    @Override
    public IOcifSchema location(String location) {
        this.location = location;
        return this;
    }

    @Override
    public Map<String, IJsonValue> map() {
        return factory().createObjectMutable() //
                .addMaybe(URI, uri) //
                .addMaybe(NAME, name) //
                .addMaybe(LOCATION, location) //
                .addMaybe(SCHEMA, schema) //
                .asMap();
    }

    @Override
    public @Nullable String name() {return name;}

    @Override
    public IOcifSchema name(String name) {
        this.name = name;
        return this;
    }

    @Override
    public @Nullable IJsonObject schema() {return schema;}

    @Override
    public IOcifSchema schema(IJsonObject schema) {
        this.schema = schema;
        return this;
    }

    @Override
    public IOcifSchema uri(String uri) {
        this.uri = uri;
        return this;
    }

    @Override
    public @NonNull String uri() {return uri;}

}

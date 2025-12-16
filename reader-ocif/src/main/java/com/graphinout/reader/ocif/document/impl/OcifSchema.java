package com.graphinout.reader.ocif.document.impl;

import com.graphinout.foundation.pure.json.document.IJsonObject;
import com.graphinout.reader.ocif.document.IOcifSchema;
import com.graphinout.reader.ocif.document.IOcifSchemaMutable;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

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
    public @Nullable String location() {return location;}

    @Override
    public @Nullable String name() {return name;}

    @Override
    public @Nullable IJsonObject schema() {return schema;}

    @Override
    public IOcifSchema setLocation(String location) {
        this.location = location;
        return this;
    }

    @Override
    public IOcifSchema setName(String name) {
        this.name = name;
        return this;
    }

    @Override
    public IOcifSchema setSchema(IJsonObject schema) {
        this.schema = schema;
        return this;
    }

    @Override
    public IOcifSchema setUri(String uri) {
        this.uri = uri;
        return this;
    }

    @Override
    public @NonNull String uri() {return uri;}

}

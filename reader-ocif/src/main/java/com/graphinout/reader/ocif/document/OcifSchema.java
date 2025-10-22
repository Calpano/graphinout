package com.graphinout.reader.ocif.document;

import com.graphinout.foundation.json.value.IJsonObject;

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
public class OcifSchema {

    private String uri;
    private IJsonObject schema; // embedded JSON schema
    private String location;
    private String name;

    public String getUri() { return uri; }
    public OcifSchema setUri(String uri) { this.uri = uri; return this; }

    public IJsonObject getSchema() { return schema; }
    public OcifSchema setSchema(IJsonObject schema) { this.schema = schema; return this; }

    public String getLocation() { return location; }
    public OcifSchema setLocation(String location) { this.location = location; return this; }

    public String getName() { return name; }
    public OcifSchema setName(String name) { this.name = name; return this; }
}

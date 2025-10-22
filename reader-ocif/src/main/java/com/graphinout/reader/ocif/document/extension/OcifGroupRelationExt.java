package com.graphinout.reader.ocif.document.extension;

import com.graphinout.foundation.json.value.IJsonArray;
import com.graphinout.foundation.json.value.IJsonObject;

/**
 * Group Relation Extension.
 * <p>
 * Spec: reader-ocif/src/spec-v0.6/spec.md → "Relation Extensions → Group Relation Extension"
 * Name: @ocif/rel/group
 * URI: https://spec.canvasprotocol.org/v0.6/extensions/group-rel.json
 * <p>
 * Properties (in relation.data):
 * - members (array of ID strings, required)
 * - cascadeDelete (boolean, optional)
 * </p>
 */
public class OcifGroupRelationExt implements IOcifExtension {
    public static final String TYPE = "@ocif/rel/group";
    public static final String URI = "https://spec.canvasprotocol.org/v0.6/extensions/group-rel.json";

    private IJsonArray members; // array of string IDs
    private Boolean cascadeDelete; // default app-dependent (spec suggests ability)
    private IJsonObject extras;

    @Override public String typeName() { return TYPE; }
    @Override public String typeUri() { return URI; }

    public IJsonArray getMembers() { return members; }
    public OcifGroupRelationExt setMembers(IJsonArray members) { this.members = members; return this; }

    public Boolean getCascadeDelete() { return cascadeDelete; }
    public OcifGroupRelationExt setCascadeDelete(Boolean cascadeDelete) { this.cascadeDelete = cascadeDelete; return this; }

    public IJsonObject getExtras() { return extras; }
    public OcifGroupRelationExt setExtras(IJsonObject extras) { this.extras = extras; return this; }
}

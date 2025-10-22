package com.graphinout.reader.ocif.document.extension;

import com.graphinout.foundation.json.value.IJsonObject;

/**
 * Parent-Child Relation Extension.
 * <p>
 * Spec: reader-ocif/src/spec-v0.6/spec.md → "Relation Extensions → Parent-Child Relation Extension"
 * Name: @ocif/rel/parent-child
 * URI: https://spec.canvasprotocol.org/v0.6/extensions/parent-child-rel.json
 * <p>
 * Properties:
 * - parent (ID, optional)
 * - child (ID, required)
 * - inherit (boolean, optional)
 * - cascadeDelete (boolean, optional)
 * </p>
 */
public class OcifParentChildRelationExt implements IOcifExtension {
    public static final String TYPE = "@ocif/rel/parent-child";
    public static final String URI = "https://spec.canvasprotocol.org/v0.6/extensions/parent-child-rel.json";

    private String parent;
    private String child;
    private Boolean inherit;
    private Boolean cascadeDelete;
    private IJsonObject extras;

    @Override public String typeName() { return TYPE; }
    @Override public String typeUri() { return URI; }

    public String getParent() { return parent; }
    public OcifParentChildRelationExt setParent(String parent) { this.parent = parent; return this; }

    public String getChild() { return child; }
    public OcifParentChildRelationExt setChild(String child) { this.child = child; return this; }

    public Boolean getInherit() { return inherit; }
    public OcifParentChildRelationExt setInherit(Boolean inherit) { this.inherit = inherit; return this; }

    public Boolean getCascadeDelete() { return cascadeDelete; }
    public OcifParentChildRelationExt setCascadeDelete(Boolean cascadeDelete) { this.cascadeDelete = cascadeDelete; return this; }

    public IJsonObject getExtras() { return extras; }
    public OcifParentChildRelationExt setExtras(IJsonObject extras) { this.extras = extras; return this; }
}

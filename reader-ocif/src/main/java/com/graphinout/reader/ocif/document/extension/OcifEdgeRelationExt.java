package com.graphinout.reader.ocif.document.extension;

import com.graphinout.foundation.json.value.IJsonObject;

/**
 * Edge Relation Extension.
 * <p>
 * Spec: reader-ocif/src/spec-v0.6/spec.md → "Relation Extensions → Edge Relation Extension"
 * Name: @ocif/rel/edge
 * URI: https://spec.canvasprotocol.org/v0.6/extensions/edge-rel.json
 * <p>
 * Properties:
 * - start (ID, required)
 * - end (ID, required)
 * - directed (boolean, default true)
 * - rel (string, optional): represented relation type
 * - node (ID, optional): visual node representing this relation
 * </p>
 */
public class OcifEdgeRelationExt implements IOcifExtension {
    public static final String TYPE = "@ocif/rel/edge";
    public static final String URI = "https://spec.canvasprotocol.org/v0.6/extensions/edge-rel.json";

    private String start;
    private String end;
    private Boolean directed;
    private String rel;
    private String node;
    private IJsonObject extras;

    @Override public String typeName() { return TYPE; }
    @Override public String typeUri() { return URI; }

    public String getStart() { return start; }
    public OcifEdgeRelationExt setStart(String start) { this.start = start; return this; }

    public String getEnd() { return end; }
    public OcifEdgeRelationExt setEnd(String end) { this.end = end; return this; }

    public Boolean getDirected() { return directed; }
    public OcifEdgeRelationExt setDirected(Boolean directed) { this.directed = directed; return this; }

    public String getRel() { return rel; }
    public OcifEdgeRelationExt setRel(String rel) { this.rel = rel; return this; }

    public String getNode() { return node; }
    public OcifEdgeRelationExt setNode(String node) { this.node = node; return this; }

    public IJsonObject getExtras() { return extras; }
    public OcifEdgeRelationExt setExtras(IJsonObject extras) { this.extras = extras; return this; }
}

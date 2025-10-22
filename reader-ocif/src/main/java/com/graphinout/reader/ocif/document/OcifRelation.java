package com.graphinout.reader.ocif.document;

import com.graphinout.foundation.json.value.IJsonArray;
import com.graphinout.foundation.json.value.IJsonObject;
import com.graphinout.reader.ocif.document.extension.IOcifExtension;

import java.util.ArrayList;
import java.util.List;

/**
 * OCIF Relation.
 * <p>
 * Spec excerpts (schema.json $defs.relation):
 * <ul>
 *   <li>id (string, required): A unique identifier for the relation.</li>
 *   <li>data (array): Additional data for the relation.</li>
 *   <li>node (string): ID of a visual node, which represents this relation.</li>
 * </ul>
 * The spec.md section "Relations" describes the various core and extension relations
 * (edge, parent-child, hyperedge, group). Implementations can store additional
 * fields in {@link #extras} to support extensions.
 */
public class OcifRelation {

    private String id;
    private IJsonArray data;
    private String node; // visual node representing relation
    private IJsonObject extras; // e.g., type, from, to, endpoints, parent/child
    private final List<IOcifExtension> extensions = new ArrayList<>();

    public String getId() { return id; }
    public OcifRelation setId(String id) { this.id = id; return this; }

    public IJsonArray getData() { return data; }
    public OcifRelation setData(IJsonArray data) { this.data = data; return this; }

    public String getNode() { return node; }
    public OcifRelation setNode(String node) { this.node = node; return this; }

    public IJsonObject getExtras() { return extras; }
    public OcifRelation setExtras(IJsonObject extras) { this.extras = extras; return this; }

    /** Typed extensions parsed from the relation's data array. */
    public List<IOcifExtension> getExtensions() { return extensions; }
    public OcifRelation addExtension(IOcifExtension ext) { if (ext != null) this.extensions.add(ext); return this; }
}

package com.graphinout.reader.ocif.document.extension;

import com.graphinout.foundation.json.value.IJsonObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Hyperedge Relation Extension.
 * <p>
 * Spec: reader-ocif/src/spec-v0.6/spec.md → "Relation Extensions → Hyperedge Relation Extension"
 * Name: @ocif/rel/hyperedge
 * URI: https://spec.canvasprotocol.org/v0.6/extensions/hyperedge-rel.json
 * <p>
 * Properties:
 * - endpoints (array of Endpoint, required)
 * - weight (number, optional)
 * - rel (string, optional): represented relation type
 * </p>
 */
public class OcifHyperedgeRelationExt implements IOcifExtension {
    public static final String TYPE = "@ocif/rel/hyperedge";
    public static final String URI = "https://spec.canvasprotocol.org/v0.6/extensions/hyperedge-rel.json";

    public static class Endpoint {
        private String id;        // ID of attached entity (node or relation)
        private String direction; // in|out|undir
        private Double weight;    // optional
        private IJsonObject extras;

        public String getId() { return id; }
        public Endpoint setId(String id) { this.id = id; return this; }

        public String getDirection() { return direction; }
        public Endpoint setDirection(String direction) { this.direction = direction; return this; }

        public Double getWeight() { return weight; }
        public Endpoint setWeight(Double weight) { this.weight = weight; return this; }

        public IJsonObject getExtras() { return extras; }
        public Endpoint setExtras(IJsonObject extras) { this.extras = extras; return this; }
    }

    private List<Endpoint> endpoints = new ArrayList<>();
    private Double weight; // overall weight
    private String rel;    // represented relation type
    private IJsonObject extras;

    @Override public String typeName() { return TYPE; }
    @Override public String typeUri() { return URI; }

    public List<Endpoint> getEndpoints() { return endpoints; }
    public OcifHyperedgeRelationExt setEndpoints(List<Endpoint> endpoints) { this.endpoints = endpoints; return this; }
    public OcifHyperedgeRelationExt addEndpoint(Endpoint ep) { this.endpoints.add(ep); return this; }

    public Double getWeight() { return weight; }
    public OcifHyperedgeRelationExt setWeight(Double weight) { this.weight = weight; return this; }

    public String getRel() { return rel; }
    public OcifHyperedgeRelationExt setRel(String rel) { this.rel = rel; return this; }

    public IJsonObject getExtras() { return extras; }
    public OcifHyperedgeRelationExt setExtras(IJsonObject extras) { this.extras = extras; return this; }
}

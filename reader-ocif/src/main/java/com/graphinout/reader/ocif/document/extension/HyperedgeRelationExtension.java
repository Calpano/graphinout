package com.graphinout.reader.ocif.document.extension;

import com.graphinout.foundation.pure.json.document.IJsonObject;
import com.graphinout.reader.ocif.OCIF;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Hyperedge Relation Extension.
 */
public class HyperedgeRelationExtension extends OcifExtension {

    public static class Endpoint {

        /** ID of attached entity (node or relation) */
        private String id;
        /** in|out|undir */
        private String direction;
        /** optional */
        private Double weight;
        private IJsonObject extras;

        public String direction() {return direction;}

        public IJsonObject extras() {return extras;}

        public String id() {return id;}

        public Endpoint setDirection(String direction) {
            this.direction = direction;
            return this;
        }

        public Endpoint setExtras(IJsonObject extras) {
            this.extras = extras;
            return this;
        }

        public Endpoint setId(String id) {
            this.id = id;
            return this;
        }

        public Endpoint setWeight(Double weight) {
            this.weight = weight;
            return this;
        }

        public Double weight() {return weight;}

    }

    public static final String TYPE_NAME = "@ocif/rel/hyperedge";
    public static final String TYPE_URI = "https://spec.canvasprotocol.org/v0.6/extensions/hyperedge-rel.json";
    private List<Endpoint> endpoints = new ArrayList<>();
    /** overall weight */
    private Double weight;
    /** represented relation type */
    private String rel;
    private IJsonObject extras;

    public HyperedgeRelationExtension() {
        super(TYPE_URI, TYPE_NAME);
    }

    public static @NonNull IOcifExtension of(@NonNull IJsonObject obj) {
        HyperedgeRelationExtension ext = new HyperedgeRelationExtension();
        // endpoints: array of objects with id, direction, weight
        if (obj.hasProperty(OCIF.Common.ENDPOINTS)) {
            var epsVal = obj.get(OCIF.Common.ENDPOINTS);
            if (epsVal != null && epsVal.isArray()) {
                var epsArr = epsVal.asArray();
                for (int i = 0; i < epsArr.size(); i++) {
                    var eobj = epsArr.get_(i).asObject();
                    Endpoint ep = new Endpoint();
                    if (eobj.hasProperty(OCIF.Common.ID)) {
                        String id = eobj.get(OCIF.Common.ID).asString();
                        ep.setId(id);
                    }
                    if (eobj.hasProperty(OCIF.Common.DIRECTION)) {
                        String dir = eobj.get(OCIF.Common.DIRECTION).asString();
                        ep.setDirection(dir);
                    }
                    if (eobj.hasProperty(OCIF.Common.WEIGHT)) {
                        var w = eobj.get(OCIF.Common.WEIGHT).asNumber();
                        ep.setWeight(w.doubleValue());
                    }
                    ext.addEndpoint(ep);
                }
            }
        }
        if (obj.hasProperty(OCIF.Common.WEIGHT)) {
            var w = obj.get(OCIF.Common.WEIGHT).asNumber();
            ext.setWeight(w.doubleValue());
        }
        obj.getIfString(OCIF.Common.REL, ext::setRel);
        return ext;
    }

    public HyperedgeRelationExtension addEndpoint(Endpoint ep) {
        this.endpoints.add(ep);
        return this;
    }

    @Override
    public Set<String> definedKeys() {
        return Set.of(OCIF.Common.ENDPOINTS, OCIF.Common.WEIGHT, OCIF.Common.REL);
    }

    public List<Endpoint> endpoints() {return endpoints;}

    public IJsonObject extras() {return extras;}

    public String rel() {return rel;}

    public HyperedgeRelationExtension setEndpoints(List<Endpoint> endpoints) {
        this.endpoints = endpoints;
        return this;
    }

    public HyperedgeRelationExtension setExtras(IJsonObject extras) {
        this.extras = extras;
        return this;
    }

    public HyperedgeRelationExtension setRel(String rel) {
        this.rel = rel;
        return this;
    }

    public HyperedgeRelationExtension setWeight(Double weight) {
        this.weight = weight;
        return this;
    }

    public Double weight() {return weight;}

}

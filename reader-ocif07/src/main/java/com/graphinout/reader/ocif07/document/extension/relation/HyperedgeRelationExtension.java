package com.graphinout.reader.ocif07.document.extension.relation;

import com.graphinout.base.cj.document.CjDirection;
import com.graphinout.foundation.pure.json.document.IJsonObject;
import com.graphinout.foundation.pure.json.document.IJsonObjectMutable;
import com.graphinout.foundation.pure.json.document.IJsonValue;
import com.graphinout.reader.ocif07.OCIF;
import com.graphinout.reader.ocif07.document.extension.IOcifExtension;
import com.graphinout.reader.ocif07.document.extension.OcifExtension;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static com.graphinout.foundation.pure.functional.Nullables.ifPresentAccept;
import static com.graphinout.reader.ocif07.Ocifs.factory;

/**
 * Hyperedge Relation Extension.
 */
public class HyperedgeRelationExtension extends OcifExtension implements IOcifRelationExtension, com.graphinout.reader.ocif07.document.extension.node.IOcifNodeExtension {

    public static class Endpoint {

        public static final String OCIF_IN = "in";
        public static final String OCIF_OUT = "out";
        public static final String OCIF_UNDIR = "undir";
        /** ID of attached entity (node or relation) */
        private String id;
        /** in|out|undir */
        private String direction;
        /** optional */
        private Double weight;

        /**
         * OCIF representation of a Connected JSON direction
         * @param direction
         * @return
         */
        public static String ocifDirection(CjDirection direction) {
            return switch (direction) {
                case IN -> OCIF_IN;
                case OUT -> OCIF_OUT;
                case UNDIR -> OCIF_UNDIR;
            };
        }

        /** Connected JSON representation of the direction */
        public CjDirection cjDirection() {
            return switch (direction) {
                case OCIF_IN -> CjDirection.IN;
                case OCIF_OUT -> CjDirection.OUT;
                case OCIF_UNDIR -> CjDirection.UNDIR;
                default -> throw new IllegalStateException("Unknown direction: " + direction);
            };
        }

        public String direction() {return direction;}

        public String id() {return id;}

        public Endpoint direction(String direction) {
            this.direction = direction;
            return this;
        }

        public Endpoint id(String id) {
            this.id = id;
            return this;
        }

        public Endpoint w(Double weight) {
            this.weight = weight;
            return this;
        }

        public Double weight() {return weight;}

    }

    public static final String TYPE_NAME = "@ocif/hyperedge";
    public static final String TYPE_URI = "https://spec.canvasprotocol.org/v0.7.0/extensions/hyperedge.json";
    /** @deprecated v0.6 name kept for backward-compatible reading */
    @Deprecated public static final String TYPE_NAME_V0_6 = "@ocif/rel/hyperedge";
    private List<Endpoint> endpoints = new ArrayList<>();
    /** overall weight */
    private Double weight;
    /** represented relation type */
    private String rel;


    public HyperedgeRelationExtension() {
        super(TYPE_URI, TYPE_NAME);
    }

    public static @NonNull IOcifExtension of(@NonNull IJsonObject obj) {
        HyperedgeRelationExtension ext = new HyperedgeRelationExtension();
        // endpoints: array of objects with id, direction, weight
        obj.ifPresent(OCIF.Common.ENDPOINTS, IJsonValue::asArray, epsArr -> {
            for (int i = 0; i < epsArr.size(); i++) {
                var eobj = epsArr.get_(i).asObject();
                Endpoint ep = new Endpoint();
                eobj.ifPresent(OCIF.Common.ID, IJsonValue::asString, ep::id);
                eobj.ifPresent(OCIF.Common.DIRECTION, IJsonValue::asString, ep::direction);
                eobj.ifPresent(OCIF.Common.WEIGHT, IJsonValue::asNumber, w -> ep.w(w.doubleValue()));
                ext.addEndpoint(ep);
            }
        });
        obj.ifPresent(OCIF.Common.WEIGHT, IJsonValue::asNumber, Number::doubleValue, ext::setWeight);
        obj.ifPresent(OCIF.Common.REL, IJsonValue::asString, ext::setRel);
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


    public String rel() {return rel;}

    public HyperedgeRelationExtension setEndpoints(List<Endpoint> endpoints) {
        this.endpoints = endpoints;
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

    @Override
    public @NonNull IJsonObject toJson() {
        IJsonObjectMutable o = factory().createObjectMutable();
        o.setString(TYPE, TYPE_NAME);
        if (!endpoints.isEmpty()) {
            o.addArray(OCIF.Common.ENDPOINTS, arr -> {
                for (Endpoint ep : endpoints) {
                    arr.addObject(eo -> {
                        eo.addMaybe(OCIF.Common.ID, ep.id());
                        eo.addMaybe(OCIF.Common.DIRECTION, ep.direction());
                        eo.addMaybe(OCIF.Common.WEIGHT, ep.weight());
                    });
                }
            });
        }
        ifPresentAccept(weight, v -> o.setNumber(OCIF.Common.WEIGHT, v));
        ifPresentAccept(rel, v -> o.setString(OCIF.Common.REL, v));
        return o;
    }

}

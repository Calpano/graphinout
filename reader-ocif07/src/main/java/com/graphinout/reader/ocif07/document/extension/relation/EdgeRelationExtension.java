package com.graphinout.reader.ocif07.document.extension.relation;

import com.graphinout.foundation.pure.json.document.IJsonObject;
import com.graphinout.foundation.pure.json.document.IJsonObjectMutable;
import com.graphinout.reader.ocif07.OCIF;
import com.graphinout.reader.ocif07.document.extension.OcifExtension;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Set;

import static com.graphinout.foundation.pure.functional.Nullables.ifPresentAccept;
import static com.graphinout.foundation.pure.functional.Nullables.nonNull;
import static com.graphinout.foundation.pure.input.ContentErrorException.contentError;
import static com.graphinout.reader.ocif07.Ocifs.factory;

/**
 * Edge Relation Extension.
 * <p>
 * Spec: reader-ocif/src/spec-v0.6/spec.md → "Relation Extensions → Edge Relation Extension" Name: @ocif/rel/edge
 * TYPE_URI:
 * <a href="https://spec.canvasprotocol.org/v0.6/extensions/edge-rel.json">spec</a>
 * <p>
 * Properties:
 * <li> start (ID, required)
 * <li> end (ID, required)
 * <li> directed (boolean, default true)
 * <li> rel (string, optional): represented relation type
 * <li> node (ID, optional): visual node representing this relation
 * </p>
 */
public class EdgeRelationExtension extends OcifExtension implements IOcifRelationExtension, com.graphinout.reader.ocif07.document.extension.node.IOcifNodeExtension {

    public static final String TYPE_NAME = "@ocif/edge";
    public static final String TYPE_URI = "https://spec.canvasprotocol.org/v0.7.0/extensions/edge.json";
    /** @deprecated v0.6 name kept for backward-compatible reading */
    @Deprecated public static final String TYPE_NAME_V0_6 = "@ocif/rel/edge";

    private String start;
    private String end;
    /** default true */
    private boolean directed;
    private String rel;
    /** a visual OCIF node representing this relation visually */
    private String node;

    public EdgeRelationExtension() {
        super(TYPE_URI, TYPE_NAME);
    }

    /**
     * Must have 'start' and 'end' properties.
     *
     * @param o
     * @return
     */
    public static EdgeRelationExtension of(IJsonObject o) {
        EdgeRelationExtension ext = new EdgeRelationExtension();
        ext.start = o.getAsNonNullStringOrThrow(OCIF.Common.START, v -> //
                contentError("Missing 'start' property"), v -> //
                contentError("Property 'start' is not a string but " + v.jsonType()));
        ext.end = o.getAsNonNullStringOrThrow(OCIF.Common.END, v -> //
                contentError("Missing 'end' property"), v -> //
                contentError("Property 'end' is not a string but " + v.jsonType()));
        ext.directed = nonNull(o.getNullOrBoolean(OCIF.Common.DIRECTED, v -> //
                contentError("Property 'directed' is not a boolean but " + v.jsonType())), true);
        ext.rel = o.getNullOrString(OCIF.Common.REL, v -> //
                contentError("Property 'rel' is not a string but " + v.jsonType()));
        ext.node = o.getNullOrString(OCIF.Common.NODE, v -> //
                contentError("Property 'node' is not an ID but " + v.jsonType()));
        return ext;
    }

    @Override
    public Set<String> definedKeys() {
        return Set.of(OCIF.Common.START, OCIF.Common.END, OCIF.Common.DIRECTED, OCIF.Common.REL, OCIF.Common.NODE);
    }

    public boolean directed() {return directed;}

    public void directed(boolean directed) {
        this.directed = directed;
    }

    public String end() {return end;}

    public EdgeRelationExtension end(String end) {
        this.end = end;
        return this;
    }

    public @Nullable String node() {return node;}

    public EdgeRelationExtension node(String node) {
        this.node = node;
        return this;
    }

    public @Nullable String rel() {return rel;}

    public EdgeRelationExtension rel(String rel) {
        this.rel = rel;
        return this;
    }

    public String start() {return start;}

    public EdgeRelationExtension start(String start) {
        this.start = start;
        return this;
    }

    @Override
    public @NonNull IJsonObject toJson() {
        IJsonObjectMutable o = factory().createObjectMutable();
        o.setString(TYPE, TYPE_NAME);
        o.setString(OCIF.Common.START, start);
        o.setString(OCIF.Common.END, end);
        o.setBoolean(OCIF.Common.DIRECTED, directed);
        ifPresentAccept(rel, v -> o.setString(OCIF.Common.REL, v));
        ifPresentAccept(node, v -> o.setString(OCIF.Common.NODE, v));
        return o;
    }

}

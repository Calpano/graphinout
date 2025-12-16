package com.graphinout.reader.ocif.document.extension;

import com.graphinout.foundation.pure.json.document.IJsonObject;
import com.graphinout.reader.ocif.OCIF;
import org.jspecify.annotations.Nullable;

import java.util.Set;

import static com.graphinout.foundation.pure.input.ContentErrorException.contentError;

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
public class EdgeRelationExtension extends OcifExtension {

    public static final String TYPE_NAME = "@ocif/rel/edge";
    public static final String TYPE_URI = "https://spec.canvasprotocol.org/v0.6/extensions/edge-rel.json";
    private String start;
    private String end;
    /** default true */
    private Boolean directed;
    private String rel;
    private String node;
    private IJsonObject extras;

    public EdgeRelationExtension() {
        super(TYPE_URI, TYPE_NAME);
    }

    public static EdgeRelationExtension of(IJsonObject o) {
        EdgeRelationExtension ext = new EdgeRelationExtension();
        ext.start = o.getAsNonNullStringOrThrow(OCIF.Common.START, v -> contentError("Missing 'start' property"), v -> contentError("Property 'start' is not a string but " + v.jsonType()));
        ext.end = o.getAsNonNullStringOrThrow(OCIF.Common.END, v -> contentError("Missing 'end' property"), v -> contentError("Property 'end' is not a string but " + v.jsonType()));
        ext.directed = o.getNullOrBoolean(OCIF.Common.DIRECTED, v -> contentError("Property 'directed' is not a boolean but " + v.jsonType()));
        if (ext.directed == null) {
            ext.directed = true; // Default value
        }
        ext.rel = o.getNullOrString(OCIF.Common.REL, v -> contentError("Property 'rel' is not a string but " + v.jsonType()));
        ext.node = o.getNullOrString(OCIF.Common.NODE, v -> contentError("Property 'node' is not an ID but " + v.jsonType()));
        return ext;
    }

    @Override
    public Set<String> definedKeys() {
        return Set.of(OCIF.Common.START, OCIF.Common.END, OCIF.Common.DIRECTED, OCIF.Common.REL, OCIF.Common.NODE);
    }

    public boolean directed() {return directed;}

    public String end() {return end;}

    public IJsonObject extras() {return extras;}

    public @Nullable String node() {return node;}

    public @Nullable String rel() {return rel;}

    public String start() {return start;}

}

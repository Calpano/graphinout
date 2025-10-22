package com.graphinout.reader.ocif.document.extension;

import com.graphinout.foundation.json.value.IJsonArray;
import com.graphinout.foundation.json.value.IJsonObject;
import com.graphinout.foundation.json.value.IJsonValue;

/**
 * Node Transforms Extension.
 * <p>
 * Spec: reader-ocif/src/spec-v0.6/spec.md → "Node Extensions → Node Transforms Extension"
 * Name: @ocif/node/transforms
 * URI: https://spec.canvasprotocol.org/v0.6/extensions/transforms-node.json
 * <p>
 * Properties:
 * - scale (number | number[2] | number[3])
 * - rotation (number degrees)
 * - rotationAxis (number[3])
 * - offset (number | number[2] | number[3])
 * </p>
 */
public class OcifNodeTransformsExt implements IOcifExtension {
    public static final String TYPE = "@ocif/node/transforms";
    public static final String URI = "https://spec.canvasprotocol.org/v0.6/extensions/transforms-node.json";

    private IJsonValue scale;      // number or array
    private Double rotation;       // degrees
    private IJsonArray rotationAxis; // [x,y,z]
    private IJsonValue offset;     // number or array
    private IJsonObject extras;

    @Override public String typeName() { return TYPE; }
    @Override public String typeUri() { return URI; }

    public IJsonValue getScale() { return scale; }
    public OcifNodeTransformsExt setScale(IJsonValue scale) { this.scale = scale; return this; }

    public Double getRotation() { return rotation; }
    public OcifNodeTransformsExt setRotation(Double rotation) { this.rotation = rotation; return this; }

    public IJsonArray getRotationAxis() { return rotationAxis; }
    public OcifNodeTransformsExt setRotationAxis(IJsonArray rotationAxis) { this.rotationAxis = rotationAxis; return this; }

    public IJsonValue getOffset() { return offset; }
    public OcifNodeTransformsExt setOffset(IJsonValue offset) { this.offset = offset; return this; }

    public IJsonObject getExtras() { return extras; }
    public OcifNodeTransformsExt setExtras(IJsonObject extras) { this.extras = extras; return this; }
}

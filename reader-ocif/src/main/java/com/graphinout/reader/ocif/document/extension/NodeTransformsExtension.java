package com.graphinout.reader.ocif.document.extension;

import com.graphinout.foundation.pure.json.document.IJsonArray;
import com.graphinout.foundation.pure.json.document.IJsonObject;
import com.graphinout.foundation.pure.json.document.IJsonValue;
import com.graphinout.reader.ocif.OCIF;
import org.jspecify.annotations.NonNull;

import java.util.Set;

/**
 * Node Transforms Extension.
 * <p>
 * Spec: reader-ocif/src/spec-v0.6/spec.md → "Node Extensions → Node Transforms Extension" Name: @ocif/node/transforms
 * TYPE_URI: https://spec.canvasprotocol.org/v0.6/extensions/transforms-node.json
 * <p>
 * Properties:
 * <li>scale (number | number[2] | number[3])
 * <li>rotation (number degrees)
 * <li>rotationAxis (number[3])
 * <li>offset (number | number[2] | number[3])
 * </p>
 */
public class NodeTransformsExtension extends OcifExtension {

    public static final String TYPE_NAME = "@ocif/node/transforms";
    public static final String TYPE_URI = "https://spec.canvasprotocol.org/v0.6/extensions/transforms-node.json";
    /** number or array */
    private IJsonValue scale;
    /** degrees */
    private Double rotation;
    /** [x,y,z] */
    private IJsonArray rotationAxis;
    /** number or array */
    private IJsonValue offset;
    

    public NodeTransformsExtension() {
        super(TYPE_URI, TYPE_NAME);
    }

    public static @NonNull IOcifExtension of(@NonNull IJsonObject obj) {
        NodeTransformsExtension ext = new NodeTransformsExtension();
        obj.getMaybe(OCIF.Common.SCALE, ext::setScale);
        obj.getMaybeAs(OCIF.Node.ROTATION, IJsonValue::asNumber, n -> ext.setRotation(n.doubleValue()));
        obj.getMaybeAs(OCIF.Common.ROTATION_AXIS, IJsonValue::asArray, ext::setRotationAxis);
        obj.getMaybe(OCIF.Common.OFFSET, ext::setOffset);
        return ext;
    }

    @Override
    public Set<String> definedKeys() {
        return Set.of(OCIF.Common.SCALE, OCIF.Node.ROTATION, OCIF.Common.ROTATION_AXIS, OCIF.Common.OFFSET);
    }

    

    public IJsonValue offset() {return offset;}

    public Double rotation() {return rotation;}

    public IJsonArray rotationAxis() {return rotationAxis;}

    public IJsonValue scale() {return scale;}

    public NodeTransformsExtension setOffset(IJsonValue offset) {
        this.offset = offset;
        return this;
    }

    public NodeTransformsExtension setRotation(Double rotation) {
        this.rotation = rotation;
        return this;
    }

    public NodeTransformsExtension setRotationAxis(IJsonArray rotationAxis) {
        this.rotationAxis = rotationAxis;
        return this;
    }

    public NodeTransformsExtension setScale(IJsonValue scale) {
        this.scale = scale;
        return this;
    }

}

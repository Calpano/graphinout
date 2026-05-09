package com.graphinout.reader.ocif07.document.extension.node;

import com.graphinout.foundation.pure.json.document.IJsonObject;
import com.graphinout.foundation.pure.json.document.IJsonObjectMutable;
import com.graphinout.foundation.pure.json.document.IJsonValue;
import com.graphinout.reader.ocif07.OCIF;
import com.graphinout.reader.ocif07.document.extension.IOcifExtension;
import com.graphinout.reader.ocif07.document.extension.OcifExtension;
import com.graphinout.reader.ocif07.document.types.OcifAngle;
import com.graphinout.reader.ocif07.document.types.OcifVector23D;
import org.jspecify.annotations.NonNull;

import java.util.Set;

import static com.graphinout.foundation.pure.functional.Nullables.ifPresentAccept;
import static com.graphinout.reader.ocif07.Ocifs.factory;

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
 *
 * @deprecated v0.7.0: rotation, rotationAxis and scale are now core Node properties. Use {@code IOcifNode#rotation()},
 * {@code IOcifNode#rotationAxis()} and {@code IOcifNode#scale()} instead.
 */
@Deprecated
public class NodeTransformsExtension extends OcifExtension implements IOcifNodeExtension {

    public static final String TYPE_NAME = "@ocif/node/transforms";
    public static final String TYPE_URI = "https://spec.canvasprotocol.org/v0.6/extensions/transforms-node.json";
    /** number or array */
    private OcifVector23D scale;
    /** degrees */
    private OcifAngle rotation;
    /** [x,y,z] */
    private OcifVector23D rotationAxis;
    /** number or array */
    private OcifVector23D offset;


    public NodeTransformsExtension() {
        super(TYPE_URI, TYPE_NAME);
    }

    public static @NonNull IOcifExtension of(@NonNull IJsonObject obj) {
        NodeTransformsExtension ext = new NodeTransformsExtension();
        obj.getMaybeAs(OCIF.Common.SCALE,
                OcifVector23D::of,
                ext::setScale);
        obj.getMaybeAs(OCIF.Node.ROTATION,
                OcifAngle::of,
                ext::setRotation
        );
        obj.getMaybeAs(OCIF.Common.ROTATION_AXIS,
                IJsonValue::asArray,
                OcifVector23D::of,
                ext::setRotationAxis);
        obj.getMaybeAs(OCIF.Common.OFFSET,
                OcifVector23D::of,
                ext::setOffset);
        return ext;
    }

    @Override
    public Set<String> definedKeys() {
        return Set.of(OCIF.Common.SCALE, OCIF.Node.ROTATION, OCIF.Common.ROTATION_AXIS, OCIF.Common.OFFSET);
    }


    public OcifVector23D offset() {return offset;}

    public OcifAngle rotation() {return rotation;}

    public OcifVector23D rotationAxis() {return rotationAxis;}

    public OcifVector23D scale() {return scale;}

    public NodeTransformsExtension setOffset(OcifVector23D offset) {
        this.offset = offset;
        return this;
    }

    public NodeTransformsExtension setRotation(OcifAngle rotation) {
        this.rotation = rotation;
        return this;
    }

    public NodeTransformsExtension setRotationAxis(OcifVector23D rotationAxis) {
        this.rotationAxis = rotationAxis;
        return this;
    }

    public NodeTransformsExtension setScale(OcifVector23D scale) {
        this.scale = scale;
        return this;
    }

    @Override
    public @NonNull IJsonObject toJson() {
        IJsonObjectMutable o = factory().createObjectMutable();
        o.setString(TYPE, TYPE_NAME);
        ifPresentAccept(scale, v -> o.add(OCIF.Common.SCALE, v.toJson()));
        ifPresentAccept(rotation, v -> o.add(OCIF.Node.ROTATION, v.toJson()));
        ifPresentAccept(rotationAxis, v -> o.add(OCIF.Common.ROTATION_AXIS, v.toJson()));
        ifPresentAccept(offset, v -> o.add(OCIF.Common.OFFSET, v.toJson()));
        return o;
    }

}

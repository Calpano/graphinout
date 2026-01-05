package com.graphinout.reader.ocif.document.extension.node;

import com.graphinout.foundation.pure.json.document.IJsonObject;
import com.graphinout.foundation.pure.json.document.IJsonValue;
import com.graphinout.reader.ocif.OCIF;
import com.graphinout.reader.ocif.document.extension.IOcifExtension;
import com.graphinout.reader.ocif.document.extension.OcifExtension;
import com.graphinout.reader.ocif.document.types.OcifVector23D;
import org.jspecify.annotations.NonNull;

import java.util.Set;

/**
 * Anchored Node Extension.
 * <p>
 * Spec: reader-ocif/src/spec-v0.6/spec.md → "Node Extensions → Anchored Node Extension" Name: @ocif/node/anchored
 * <p>
 * Properties:
 * <li>topLeftAnchor (number[2]|number[3])
 * <li>bottomRightAnchor (number[2]|number[3])
 * <li>topLeftOffset (number[2]|number[3])
 * <li>bottomRightOffset (number[2]|number[3])
 * </p>
 */
public class AnchoredNodeExtension extends OcifExtension implements IOcifNodeExtension {

    public static final String TYPE_NAME = "@ocif/node/anchored";
    public static final String TYPE_URI = "https://spec.canvasprotocol.org/v0.6/extensions/anchored-node.json";
    private OcifVector23D topLeftAnchor;
    private OcifVector23D bottomRightAnchor;
    private OcifVector23D topLeftOffset;
    private OcifVector23D bottomRightOffset;

    public AnchoredNodeExtension() {
        super(TYPE_URI, TYPE_NAME);
    }

    public static @NonNull IOcifExtension of(@NonNull IJsonObject obj) {
        AnchoredNodeExtension ext = new AnchoredNodeExtension();

        obj.getMaybeAs(OCIF.Common.TOP_LEFT_ANCHOR, IJsonValue::asArray, OcifVector23D::of, ext::setTopLeftAnchor);

        obj.getMaybeAs(OCIF.Common.BOTTOM_RIGHT_ANCHOR, IJsonValue::asArray, OcifVector23D::of, ext::setBottomRightAnchor);

        obj.getMaybeAs(OCIF.Common.TOP_LEFT_OFFSET, IJsonValue::asArray, OcifVector23D::of, ext::setTopLeftOffset);

        obj.getMaybeAs(OCIF.Common.BOTTOM_RIGHT_OFFSET, IJsonValue::asArray, OcifVector23D::of, ext::setBottomRightOffset);

        return ext;
    }

    public OcifVector23D bottomRightAnchor() {return bottomRightAnchor;}

    public OcifVector23D bottomRightOffset() {return bottomRightOffset;}

    @Override
    public Set<String> definedKeys() {
        return Set.of(OCIF.Common.TOP_LEFT_ANCHOR, OCIF.Common.BOTTOM_RIGHT_ANCHOR, OCIF.Common.TOP_LEFT_OFFSET, OCIF.Common.BOTTOM_RIGHT_OFFSET);
    }

    public AnchoredNodeExtension setBottomRightAnchor(OcifVector23D bottomRightAnchor) {
        this.bottomRightAnchor = bottomRightAnchor;
        return this;
    }

    public AnchoredNodeExtension setBottomRightOffset(OcifVector23D bottomRightOffset) {
        this.bottomRightOffset = bottomRightOffset;
        return this;
    }

    public AnchoredNodeExtension setTopLeftAnchor(OcifVector23D topLeftAnchor) {
        this.topLeftAnchor = topLeftAnchor;
        return this;
    }

    public AnchoredNodeExtension setTopLeftOffset(OcifVector23D topLeftOffset) {
        this.topLeftOffset = topLeftOffset;
        return this;
    }

    public OcifVector23D topLeftAnchor() {return topLeftAnchor;}

    public OcifVector23D topLeftOffset() {return topLeftOffset;}

}

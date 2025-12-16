package com.graphinout.reader.ocif.document.extension;

import com.graphinout.foundation.pure.json.document.IJsonArray;
import com.graphinout.foundation.pure.json.document.IJsonObject;
import com.graphinout.foundation.pure.json.document.IJsonValue;
import com.graphinout.reader.ocif.OCIF;
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
public class AnchoredNodeExtension extends OcifExtension {

    public static final String TYPE_NAME = "@ocif/node/anchored";
    public static final String TYPE_URI = "https://spec.canvasprotocol.org/v0.6/extensions/anchored-node.json";
    private IJsonArray topLeftAnchor;
    private IJsonArray bottomRightAnchor;
    private IJsonArray topLeftOffset;
    private IJsonArray bottomRightOffset;
    public AnchoredNodeExtension() {
        super(TYPE_URI, TYPE_NAME);
    }

    public static @NonNull IOcifExtension of(@NonNull IJsonObject obj) {
        AnchoredNodeExtension ext = new AnchoredNodeExtension();
        obj.getMaybeAs(OCIF.Common.TOP_LEFT_ANCHOR, IJsonValue::asArray, ext::setTopLeftAnchor);
        obj.getMaybeAs(OCIF.Common.BOTTOM_RIGHT_ANCHOR, IJsonValue::asArray, ext::setBottomRightAnchor);
        obj.getMaybeAs(OCIF.Common.TOP_LEFT_OFFSET, IJsonValue::asArray, ext::setTopLeftOffset);
        obj.getMaybeAs(OCIF.Common.BOTTOM_RIGHT_OFFSET, IJsonValue::asArray, ext::setBottomRightOffset);
        return ext;
    }

    public IJsonArray bottomRightAnchor() {return bottomRightAnchor;}

    public IJsonArray bottomRightOffset() {return bottomRightOffset;}

    @Override
    public Set<String> definedKeys() {
        return Set.of(
                OCIF.Common.TOP_LEFT_ANCHOR,
                OCIF.Common.BOTTOM_RIGHT_ANCHOR,
                OCIF.Common.TOP_LEFT_OFFSET,
                OCIF.Common.BOTTOM_RIGHT_OFFSET
        );
    }

    public AnchoredNodeExtension setBottomRightAnchor(IJsonArray bottomRightAnchor) {
        this.bottomRightAnchor = bottomRightAnchor;
        return this;
    }

    public AnchoredNodeExtension setBottomRightOffset(IJsonArray bottomRightOffset) {
        this.bottomRightOffset = bottomRightOffset;
        return this;
    }

    public AnchoredNodeExtension setTopLeftAnchor(IJsonArray topLeftAnchor) {
        this.topLeftAnchor = topLeftAnchor;
        return this;
    }

    public AnchoredNodeExtension setTopLeftOffset(IJsonArray topLeftOffset) {
        this.topLeftOffset = topLeftOffset;
        return this;
    }

    public IJsonArray topLeftAnchor() {return topLeftAnchor;}

    public IJsonArray topLeftOffset() {return topLeftOffset;}

}

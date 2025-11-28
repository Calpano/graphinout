package com.graphinout.reader.ocif.document.extension;

import com.graphinout.foundation.json.value.IJsonArray;
import com.graphinout.foundation.json.value.IJsonObject;

/**
 * Anchored Node Extension.
 * <p>
 * Spec: reader-ocif/src/spec-v0.6/spec.md → "Node Extensions → Anchored Node Extension"
 * Name: @ocif/node/anchored
 * URI: https://spec.canvasprotocol.org/v0.6/extensions/anchored-node.json
 * <p>
 * Properties:
 * - topLeftAnchor (number[2]|number[3])
 * - bottomRightAnchor (number[2]|number[3])
 * - topLeftOffset (number[2]|number[3])
 * - bottomRightOffset (number[2]|number[3])
 * </p>
 */
public class OcifAnchoredNodeExt implements IOcifExtension {
    public static final String TYPE = "@ocif/node/anchored";
    public static final String URI = "https://spec.canvasprotocol.org/v0.6/extensions/anchored-node.json";

    private IJsonArray topLeftAnchor;
    private IJsonArray bottomRightAnchor;
    private IJsonArray topLeftOffset;
    private IJsonArray bottomRightOffset;
    private IJsonObject extras;

    @Override public String typeName() { return TYPE; }
    @Override public String typeUri() { return URI; }

    public IJsonArray getTopLeftAnchor() { return topLeftAnchor; }
    public OcifAnchoredNodeExt setTopLeftAnchor(IJsonArray topLeftAnchor) { this.topLeftAnchor = topLeftAnchor; return this; }

    public IJsonArray getBottomRightAnchor() { return bottomRightAnchor; }
    public OcifAnchoredNodeExt setBottomRightAnchor(IJsonArray bottomRightAnchor) { this.bottomRightAnchor = bottomRightAnchor; return this; }

    public IJsonArray getTopLeftOffset() { return topLeftOffset; }
    public OcifAnchoredNodeExt setTopLeftOffset(IJsonArray topLeftOffset) { this.topLeftOffset = topLeftOffset; return this; }

    public IJsonArray getBottomRightOffset() { return bottomRightOffset; }
    public OcifAnchoredNodeExt setBottomRightOffset(IJsonArray bottomRightOffset) { this.bottomRightOffset = bottomRightOffset; return this; }

    public IJsonObject getExtras() { return extras; }
    public OcifAnchoredNodeExt setExtras(IJsonObject extras) { this.extras = extras; return this; }
}

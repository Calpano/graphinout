package com.graphinout.reader.ocif.document.extension;

import com.graphinout.foundation.json.value.IJsonObject;

/**
 * Rectangle Node Extension.
 * <p>
 * Spec: reader-ocif/src/spec-v0.6/spec.md → "Node Extensions → Rectangle Extension"
 * Name: @ocif/node/rect
 * URI: https://spec.canvasprotocol.org/v0.6/extensions/rect-node.json
 * <p>
 * Properties:
 * - strokeWidth (number): line width
 * - strokeColor (Color string): stroke color
 * - fillColor (Color string): fill color (none means fully transparent)
 * </p>
 */
public class OcifRectNodeExt implements IOcifExtension {
    public static final String TYPE = "@ocif/node/rect";
    public static final String URI = "https://spec.canvasprotocol.org/v0.6/extensions/rect-node.json";

    private Double strokeWidth; // default 1
    private String strokeColor; // default #FFFFFF
    private String fillColor;   // default none
    private IJsonObject extras;

    @Override public String typeName() { return TYPE; }
    @Override public String typeUri() { return URI; }

    public Double getStrokeWidth() { return strokeWidth; }
    public OcifRectNodeExt setStrokeWidth(Double strokeWidth) { this.strokeWidth = strokeWidth; return this; }

    public String getStrokeColor() { return strokeColor; }
    public OcifRectNodeExt setStrokeColor(String strokeColor) { this.strokeColor = strokeColor; return this; }

    public String getFillColor() { return fillColor; }
    public OcifRectNodeExt setFillColor(String fillColor) { this.fillColor = fillColor; return this; }

    public IJsonObject getExtras() { return extras; }
    public OcifRectNodeExt setExtras(IJsonObject extras) { this.extras = extras; return this; }
}

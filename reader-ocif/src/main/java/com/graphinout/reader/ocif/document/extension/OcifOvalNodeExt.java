package com.graphinout.reader.ocif.document.extension;

import com.graphinout.foundation.json.value.IJsonObject;

/**
 * Oval Node Extension.
 * <p>
 * Spec: reader-ocif/src/spec-v0.6/spec.md → "Node Extensions → Oval Extension"
 * Name: @ocif/node/oval
 * URI: https://spec.canvasprotocol.org/v0.6/extensions/oval-node.json
 * <p>
 * Has the same properties as Rectangle; rendering differs (ellipse within bounding box).
 * </p>
 */
public class OcifOvalNodeExt implements IOcifExtension {
    public static final String TYPE = "@ocif/node/oval";
    public static final String URI = "https://spec.canvasprotocol.org/v0.6/extensions/oval-node.json";

    private Double strokeWidth; // default 1
    private String strokeColor; // default #FFFFFF
    private String fillColor;   // default none
    private IJsonObject extras;

    @Override public String typeName() { return TYPE; }
    @Override public String typeUri() { return URI; }

    public Double getStrokeWidth() { return strokeWidth; }
    public OcifOvalNodeExt setStrokeWidth(Double strokeWidth) { this.strokeWidth = strokeWidth; return this; }

    public String getStrokeColor() { return strokeColor; }
    public OcifOvalNodeExt setStrokeColor(String strokeColor) { this.strokeColor = strokeColor; return this; }

    public String getFillColor() { return fillColor; }
    public OcifOvalNodeExt setFillColor(String fillColor) { this.fillColor = fillColor; return this; }

    public IJsonObject getExtras() { return extras; }
    public OcifOvalNodeExt setExtras(IJsonObject extras) { this.extras = extras; return this; }
}

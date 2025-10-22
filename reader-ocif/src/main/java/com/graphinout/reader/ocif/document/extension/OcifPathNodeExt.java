package com.graphinout.reader.ocif.document.extension;

import com.graphinout.foundation.json.value.IJsonObject;

/**
 * Path Node Extension.
 * <p>
 * Spec: reader-ocif/src/spec-v0.6/spec.md → "Node Extensions → Path Extension"
 * Name: @ocif/node/path
 * URI: https://spec.canvasprotocol.org/v0.6/extensions/path-node.json
 * <p>
 * Properties:
 * - strokeWidth (number): line width
 * - strokeColor (Color string)
 * - fillColor (Color string)
 * - path (string, required): SVG-like path data string
 * </p>
 */
public class OcifPathNodeExt implements IOcifExtension {
    public static final String TYPE = "@ocif/node/path";
    public static final String URI = "https://spec.canvasprotocol.org/v0.6/extensions/path-node.json";

    private Double strokeWidth; // default 1
    private String strokeColor; // default #FFFFFF
    private String fillColor;   // default none
    private String path;        // required
    private IJsonObject extras;

    @Override public String typeName() { return TYPE; }
    @Override public String typeUri() { return URI; }

    public Double getStrokeWidth() { return strokeWidth; }
    public OcifPathNodeExt setStrokeWidth(Double strokeWidth) { this.strokeWidth = strokeWidth; return this; }

    public String getStrokeColor() { return strokeColor; }
    public OcifPathNodeExt setStrokeColor(String strokeColor) { this.strokeColor = strokeColor; return this; }

    public String getFillColor() { return fillColor; }
    public OcifPathNodeExt setFillColor(String fillColor) { this.fillColor = fillColor; return this; }

    public String getPath() { return path; }
    public OcifPathNodeExt setPath(String path) { this.path = path; return this; }

    public IJsonObject getExtras() { return extras; }
    public OcifPathNodeExt setExtras(IJsonObject extras) { this.extras = extras; return this; }
}

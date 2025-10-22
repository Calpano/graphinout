package com.graphinout.reader.ocif.document.extension;

import com.graphinout.foundation.json.value.IJsonArray;
import com.graphinout.foundation.json.value.IJsonObject;

/**
 * Arrow Node Extension.
 * <p>
 * Spec: reader-ocif/src/spec-v0.6/spec.md → "Node Extensions → Arrow Extension"
 * Name: @ocif/node/arrow
 * URI: https://spec.canvasprotocol.org/v0.6/extensions/arrow-node.json
 * <p>
 * Properties:
 * - strokeWidth (number): line width
 * - strokeColor (Color string)
 * - start (number[2|3], required)
 * - end (number[2|3], required)
 * - startMarker (string): none|arrowhead
 * - endMarker (string): none|arrowhead
 * </p>
 */
public class OcifArrowNodeExt implements IOcifExtension {
    public static final String TYPE = "@ocif/node/arrow";
    public static final String URI = "https://spec.canvasprotocol.org/v0.6/extensions/arrow-node.json";

    private Double strokeWidth; // default 1
    private String strokeColor; // default #FFFFFF
    private IJsonArray start;   // [x,y,(z)]
    private IJsonArray end;     // [x,y,(z)]
    private String startMarker; // none|arrowhead
    private String endMarker;   // none|arrowhead
    private IJsonObject extras;

    @Override public String typeName() { return TYPE; }
    @Override public String typeUri() { return URI; }

    public Double getStrokeWidth() { return strokeWidth; }
    public OcifArrowNodeExt setStrokeWidth(Double strokeWidth) { this.strokeWidth = strokeWidth; return this; }

    public String getStrokeColor() { return strokeColor; }
    public OcifArrowNodeExt setStrokeColor(String strokeColor) { this.strokeColor = strokeColor; return this; }

    public IJsonArray getStart() { return start; }
    public OcifArrowNodeExt setStart(IJsonArray start) { this.start = start; return this; }

    public IJsonArray getEnd() { return end; }
    public OcifArrowNodeExt setEnd(IJsonArray end) { this.end = end; return this; }

    public String getStartMarker() { return startMarker; }
    public OcifArrowNodeExt setStartMarker(String startMarker) { this.startMarker = startMarker; return this; }

    public String getEndMarker() { return endMarker; }
    public OcifArrowNodeExt setEndMarker(String endMarker) { this.endMarker = endMarker; return this; }

    public IJsonObject getExtras() { return extras; }
    public OcifArrowNodeExt setExtras(IJsonObject extras) { this.extras = extras; return this; }
}

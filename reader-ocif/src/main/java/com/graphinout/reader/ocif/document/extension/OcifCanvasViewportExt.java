package com.graphinout.reader.ocif.document.extension;

import com.graphinout.foundation.json.value.IJsonArray;
import com.graphinout.foundation.json.value.IJsonObject;

/**
 * Canvas Viewport extension.
 * <p>
 * Spec: reader-ocif/src/spec-v0.6/spec.md → "Canvas Extensions → Canvas Viewport"
 * Name: @ocif/canvas/viewport
 * URI: https://spec.canvasprotocol.org/v0.6/extensions/viewport-canvas.json
 * <p>
 * Properties:
 * - position (number[2|3], required): top-left corner of the viewport
 * - size (number[2|3], required): width/height(/depth) of the viewport
 * </p>
 */
public class OcifCanvasViewportExt implements IOcifExtension {
    public static final String TYPE = "@ocif/canvas/viewport";
    public static final String URI = "https://spec.canvasprotocol.org/v0.6/extensions/viewport-canvas.json";

    private IJsonArray position; // [x,y,(z)]
    private IJsonArray size;     // [w,h,(d)]
    private IJsonObject extras;  // future-proofing for spec evolution

    @Override public String typeName() { return TYPE; }
    @Override public String typeUri() { return URI; }

    public IJsonArray getPosition() { return position; }
    public OcifCanvasViewportExt setPosition(IJsonArray position) { this.position = position; return this; }

    public IJsonArray getSize() { return size; }
    public OcifCanvasViewportExt setSize(IJsonArray size) { this.size = size; return this; }

    public IJsonObject getExtras() { return extras; }
    public OcifCanvasViewportExt setExtras(IJsonObject extras) { this.extras = extras; return this; }
}

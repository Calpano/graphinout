package com.graphinout.reader.ocif.document.extension;

import com.graphinout.foundation.pure.json.document.IJsonArray;
import com.graphinout.foundation.pure.json.document.IJsonObject;
import com.graphinout.reader.ocif.OCIF;
import org.jspecify.annotations.NonNull;

import java.util.Set;

/**
 * Canvas Viewport extension.
 * <p>
 * Spec: reader-ocif/src/spec-v0.6/spec.md → "Canvas Extensions → Canvas Viewport" Name: @ocif/canvas/viewport
 * TYPE_URI:
 * <a href="https://spec.canvasprotocol.org/v0.6/extensions/viewport-canvas.json">TYPE_URI</a>
 */
public class CanvasViewportExtension extends OcifExtension {

    public static final String TYPE_NAME = "@ocif/canvas/viewport";
    public static final String TYPE_URI = "https://spec.canvasprotocol.org/v0.6/extensions/viewport-canvas.json";

    /** [x,y,(z)] position (number[2|3], required): top-left corner of the viewport */
    private IJsonArray position;
    /** [w,h,(d)] size (number[2|3], required): width/height(/depth) of the viewport */
    private IJsonArray size;

    public CanvasViewportExtension() {
        super(TYPE_URI, TYPE_NAME);
    }

    public static @NonNull IOcifExtension of(@NonNull IJsonObject obj) {
        CanvasViewportExtension ext = new CanvasViewportExtension();
        ext.position = obj.get(OCIF.Node.POSITION).asArray();
        ext.size = obj.get(OCIF.Node.SIZE).asArray();
        return ext;
    }

    @Override
    public Set<String> definedKeys() {
        return Set.of(OCIF.Node.POSITION, OCIF.Node.SIZE);
    }

    public IJsonArray position() {return position;}

    public CanvasViewportExtension setPosition(IJsonArray position) {
        this.position = position;
        return this;
    }

    public CanvasViewportExtension setSize(IJsonArray size) {
        this.size = size;
        return this;
    }

    public IJsonArray size() {return size;}


}

package com.graphinout.reader.ocif07.document.extension.canvas;

import com.graphinout.foundation.pure.json.document.IJsonObject;
import com.graphinout.foundation.pure.json.document.IJsonObjectMutable;
import com.graphinout.reader.ocif07.OCIF;
import com.graphinout.reader.ocif07.document.extension.IOcifExtension;
import com.graphinout.reader.ocif07.document.extension.OcifExtension;
import com.graphinout.reader.ocif07.document.types.OcifVector23D;
import org.jspecify.annotations.NonNull;

import java.util.Set;

import static com.graphinout.foundation.pure.functional.Nullables.ifPresentAccept;
import static com.graphinout.reader.ocif07.Ocifs.factory;

/**
 * Canvas Viewport extension.
 * <p>
 * Spec: reader-ocif/src/spec-v0.6/spec.md → "Canvas Extensions → Canvas Viewport" Name: @ocif/canvas/viewport
 * TYPE_URI:
 * <a href="https://spec.canvasprotocol.org/v0.6/extensions/viewport-canvas.json">TYPE_URI</a>
 */
public class CanvasViewportExtension extends OcifExtension implements IOcifCanvasExtension {

    public static final String TYPE_NAME = "@ocif/canvas-viewport";
    public static final String TYPE_URI = "https://spec.canvasprotocol.org/v0.7.0/extensions/canvas-viewport.json";
    /** @deprecated v0.6 name kept for backward-compatible reading */
    @Deprecated public static final String TYPE_NAME_V0_6 = "@ocif/canvas/viewport";

    /** [x,y,(z)] position (number[2|3], required): top-left corner of the viewport */
    private OcifVector23D position;
    /** [w,h,(d)] size (number[2|3], required): width/height(/depth) of the viewport */
    private OcifVector23D size;

    public CanvasViewportExtension() {
        super(TYPE_URI, TYPE_NAME);
    }

    public static @NonNull IOcifExtension of(@NonNull IJsonObject obj) {
        CanvasViewportExtension ext = new CanvasViewportExtension();
        ext.position = OcifVector23D.of(obj.get(OCIF.Node.POSITION), OcifVector23D.ZERO_2D);
        ext.size = OcifVector23D.of(obj.get(OCIF.Node.SIZE), new OcifVector23D(new double[]{100., 100.}));
        return ext;
    }

    @Override
    public Set<String> definedKeys() {
        return Set.of(OCIF.Node.POSITION, OCIF.Node.SIZE);
    }

    public OcifVector23D position() {return position;}

    public CanvasViewportExtension setPosition(OcifVector23D position) {
        this.position = position;
        return this;
    }

    public CanvasViewportExtension setSize(OcifVector23D size) {
        this.size = size;
        return this;
    }

    public OcifVector23D size() {return size;}

    @Override
    public @NonNull IJsonObject toJson() {
        IJsonObjectMutable o = factory().createObjectMutable();
        o.setString(TYPE, TYPE_NAME);
        ifPresentAccept(position, v -> o.add(OCIF.Node.POSITION, v.toJson()));
        ifPresentAccept(size, v -> o.add(OCIF.Node.SIZE, v.toJson()));
        return o;
    }

}

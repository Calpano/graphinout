package com.graphinout.reader.ocif.document.extension;

import com.graphinout.foundation.pure.json.document.IJsonObject;
import com.graphinout.foundation.pure.json.document.IJsonValue;
import com.graphinout.reader.ocif.OCIF;
import com.graphinout.reader.ocif.document.types.Color;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Set;

/**
 * Rectangle Node Extension.
 * <p>
 * Spec: reader-ocif/src/spec-v0.6/spec.md → "Node Extensions → Rectangle Extension" Name: @ocif/node/rect TYPE_URI:
 * https://spec.canvasprotocol.org/v0.6/extensions/rect-node.json
 * <p>
 * Properties:
 * <li>strokeWidth (number): line width
 * <li>strokeColor (Color string): stroke color
 * <li>fillColor (Color string): fill color (none means fully transparent)
 * </p>
 */
public class RectangleExtension extends OcifExtension {

    public static final String TYPE_NAME = "@ocif/node/rect";
    public static final String TYPE_URI = "https://spec.canvasprotocol.org/v0.6/extensions/rect-node.json";
    /** default 1 */
    private Double strokeWidth;
    /** default #FFFFFF */
    private Color strokeColor;
    /** default none */
    private Color fillColor;

    public RectangleExtension() {
        super(TYPE_URI, TYPE_NAME);
    }

    public static @NonNull IOcifExtension of(@NonNull IJsonObject obj) {
        RectangleExtension ext = new RectangleExtension();
        obj.ifPresent(OCIF.Common.STROKE_WIDTH, IJsonValue::asNumber, Number::doubleValue, ext::setStrokeWidth);
        obj.ifPresent(OCIF.Common.FILL_COLOR, Color::of, ext::setFillColor);
        obj.ifPresent(OCIF.Common.STROKE_COLOR, Color::of, ext::setStrokeColor);
        return ext;
    }

    public Set<String> definedKeys() {
        return Set.of(OCIF.Common.TYPE, OCIF.Common.STROKE_WIDTH, OCIF.Common.STROKE_COLOR, OCIF.Common.FILL_COLOR);
    }

    public @Nullable Color fillColor() {return fillColor;}

    public RectangleExtension setFillColor(Color fillColor) {
        this.fillColor = fillColor;
        return this;
    }

    public RectangleExtension setStrokeColor(Color strokeColor) {
        this.strokeColor = strokeColor;
        return this;
    }

    public RectangleExtension setStrokeWidth(Double strokeWidth) {
        this.strokeWidth = strokeWidth;
        return this;
    }

    public @Nullable Color strokeColor() {return strokeColor;}

    public @Nullable Double strokeWidth() {return strokeWidth;}

}

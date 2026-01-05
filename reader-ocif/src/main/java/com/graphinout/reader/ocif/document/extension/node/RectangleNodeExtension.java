package com.graphinout.reader.ocif.document.extension.node;

import com.graphinout.foundation.pure.collections.jajson.JaJson;
import com.graphinout.foundation.pure.json.document.IJsonObject;
import com.graphinout.foundation.pure.json.document.IJsonValue;
import com.graphinout.reader.ocif.OCIF;
import com.graphinout.reader.ocif.document.extension.IOcifExtension;
import com.graphinout.reader.ocif.document.extension.OcifExtension;
import com.graphinout.reader.ocif.document.types.OcifColor;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Map;
import java.util.Set;

import static com.graphinout.reader.ocif.OCIF.Common.FILL_COLOR;
import static com.graphinout.reader.ocif.OCIF.Common.STROKE_COLOR;
import static com.graphinout.reader.ocif.OCIF.Common.STROKE_WIDTH;

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
public class RectangleNodeExtension extends OcifExtension implements IOcifNodeExtension {

    public static final String TYPE_NAME = "@ocif/node/rect";
    public static final String TYPE_URI = "https://spec.canvasprotocol.org/v0.6/extensions/rect-node.json";
    /** default 1 */
    private Double strokeWidth;
    /** default #FFFFFF */
    private OcifColor strokeColor;
    /** default none */
    private OcifColor fillColor;

    public RectangleNodeExtension() {
        super(TYPE_URI, TYPE_NAME);
    }

    public static @NonNull IOcifExtension of(@NonNull IJsonObject obj) {
        RectangleNodeExtension ext = new RectangleNodeExtension();
        obj.ifPresent(STROKE_WIDTH, IJsonValue::asNumber, Number::doubleValue, ext::setStrokeWidth);
        obj.ifPresent(FILL_COLOR, OcifColor::of, ext::setFillColor);
        obj.ifPresent(STROKE_COLOR, OcifColor::of, ext::setStrokeColor);
        return ext;
    }

    public Set<String> definedKeys() {
        return Set.of(OCIF.Common.TYPE, STROKE_WIDTH, STROKE_COLOR, FILL_COLOR);
    }

    public @Nullable OcifColor fillColor() {return fillColor;}

    public RectangleNodeExtension setFillColor(OcifColor fillColor) {
        this.fillColor = fillColor;
        return this;
    }

    public RectangleNodeExtension setStrokeColor(OcifColor strokeColor) {
        this.strokeColor = strokeColor;
        return this;
    }

    public RectangleNodeExtension setStrokeWidth(Double strokeWidth) {
        this.strokeWidth = strokeWidth;
        return this;
    }

    public @Nullable OcifColor strokeColor() {return strokeColor;}

    public @Nullable Double strokeWidth() {return strokeWidth;}

    public Map<String, Object> toMap() {
        return JaJson.createMap()//
                .putNonNull(TYPE, typeUri())//
                .putMaybe(STROKE_WIDTH, strokeWidth())//
                .putMaybe(STROKE_COLOR, strokeColor())//
                .putMaybe(FILL_COLOR, fillColor()).build();
    }

}

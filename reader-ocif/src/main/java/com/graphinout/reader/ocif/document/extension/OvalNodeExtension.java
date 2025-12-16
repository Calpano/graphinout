package com.graphinout.reader.ocif.document.extension;

import com.graphinout.foundation.pure.json.document.IJsonObject;
import com.graphinout.foundation.pure.json.document.IJsonValue;
import com.graphinout.reader.ocif.OCIF;
import org.jspecify.annotations.NonNull;

import java.util.Set;

/**
 * Oval Node Extension.
 * <p>
 * Spec: reader-ocif/src/spec-v0.6/spec.md → "Node Extensions → Oval Extension" Name: @ocif/node/oval TYPE_URI:
 * https://spec.canvasprotocol.org/v0.6/extensions/oval-node.json
 * <p>
 * Has the same properties as Rectangle; rendering differs (ellipse within bounding box).
 * </p>
 */
public class OvalNodeExtension extends OcifExtension {

    public static final String TYPE_NAME = "@ocif/node/oval";
    public static final String TYPE_URI = "https://spec.canvasprotocol.org/v0.6/extensions/oval-node.json";
    /** default 1 */
    private Double strokeWidth;
    /** default #FFFFFF */
    private String strokeColor;
    /** default none */
    private String fillColor;
    
    public OvalNodeExtension() {
        super(TYPE_URI, TYPE_NAME);
    }

    public static @NonNull IOcifExtension of(@NonNull IJsonObject obj) {
        OvalNodeExtension ext = new OvalNodeExtension();
        obj.getMaybeAs(OCIF.Common.STROKE_WIDTH, IJsonValue::asNumber, n -> ext.setStrokeWidth(n.doubleValue()));
        obj.getIfString(OCIF.Common.STROKE_COLOR, ext::setStrokeColor);
        obj.getIfString(OCIF.Common.FILL_COLOR, ext::setFillColor);
        return ext;
    }

    @Override
    public Set<String> definedKeys() {
        return Set.of(OCIF.Common.STROKE_WIDTH, OCIF.Common.STROKE_COLOR, OCIF.Common.FILL_COLOR);
    }

    

    public String fillColor() {return fillColor;}

    public OvalNodeExtension setFillColor(String fillColor) {
        this.fillColor = fillColor;
        return this;
    }

    public OvalNodeExtension setStrokeColor(String strokeColor) {
        this.strokeColor = strokeColor;
        return this;
    }

    public OvalNodeExtension setStrokeWidth(Double strokeWidth) {
        this.strokeWidth = strokeWidth;
        return this;
    }

    public String strokeColor() {return strokeColor;}

    public Double strokeWidth() {return strokeWidth;}

}

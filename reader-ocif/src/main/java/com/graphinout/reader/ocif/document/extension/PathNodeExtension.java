package com.graphinout.reader.ocif.document.extension;

import com.graphinout.foundation.pure.json.document.IJsonObject;
import com.graphinout.foundation.pure.json.document.IJsonValue;
import com.graphinout.reader.ocif.OCIF;
import org.jspecify.annotations.NonNull;

import java.util.Set;

/**
 * Path Node Extension.
 * <p>
 * Spec: reader-ocif/src/spec-v0.6/spec.md → "Node Extensions → Path Extension" Name: @ocif/node/path TYPE_URI:
 * https://spec.canvasprotocol.org/v0.6/extensions/path-node.json
 * <p>
 * Properties:
 * <li>strokeWidth (number): line width
 * <li>strokeColor (Color string)
 * <li>fillColor (Color string)
 * <li>path (string, required): SVG-like path data string
 * </p>
 */
public class PathNodeExtension extends OcifExtension {

    public static final String TYPE_NAME = "@ocif/node/path";
    public static final String TYPE_URI = "https://spec.canvasprotocol.org/v0.6/extensions/path-node.json";
    /** default 1 */
    private Double strokeWidth;
    /** default #FFFFFF */
    private String strokeColor;
    /** default none */
    private String fillColor;
    /** required */
    private String path;
    
    public PathNodeExtension() {
        super(TYPE_URI, TYPE_NAME);
    }

    public static @NonNull IOcifExtension of(@NonNull IJsonObject obj) {
        PathNodeExtension ext = new PathNodeExtension();
        obj.getMaybeAs(OCIF.Common.STROKE_WIDTH, IJsonValue::asNumber, n -> ext.setStrokeWidth(n.doubleValue()));
        obj.getIfString(OCIF.Common.STROKE_COLOR, ext::setStrokeColor);
        obj.getIfString(OCIF.Common.FILL_COLOR, ext::setFillColor);
        obj.getIfString(OCIF.Common.PATH, ext::setPath);
        return ext;
    }

    @Override
    public Set<String> definedKeys() {
        return Set.of(OCIF.Common.STROKE_WIDTH, OCIF.Common.STROKE_COLOR, OCIF.Common.FILL_COLOR, OCIF.Common.PATH);
    }

    

    public String fillColor() {return fillColor;}

    public String path() {return path;}

    public PathNodeExtension setFillColor(String fillColor) {
        this.fillColor = fillColor;
        return this;
    }

    public PathNodeExtension setPath(String path) {
        this.path = path;
        return this;
    }

    public PathNodeExtension setStrokeColor(String strokeColor) {
        this.strokeColor = strokeColor;
        return this;
    }

    public PathNodeExtension setStrokeWidth(Double strokeWidth) {
        this.strokeWidth = strokeWidth;
        return this;
    }

    public String strokeColor() {return strokeColor;}

    public Double strokeWidth() {return strokeWidth;}

}

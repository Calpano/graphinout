package com.graphinout.reader.ocif07.document.extension.node;

import com.graphinout.foundation.pure.json.document.IJsonObject;
import com.graphinout.foundation.pure.json.document.IJsonObjectMutable;
import com.graphinout.foundation.pure.json.document.IJsonValue;
import com.graphinout.reader.ocif07.OCIF;
import com.graphinout.reader.ocif07.document.extension.IOcifExtension;
import com.graphinout.reader.ocif07.document.extension.OcifExtension;
import com.graphinout.reader.ocif07.document.types.OcifColor;
import org.jspecify.annotations.NonNull;

import java.util.Set;

import static com.graphinout.foundation.pure.functional.Nullables.ifPresentAccept;
import static com.graphinout.reader.ocif07.Ocifs.factory;

/**
 * Oval Node Extension.
 * <p>
 * Spec: reader-ocif/src/spec-v0.6/spec.md → "Node Extensions → Oval Extension" Name: @ocif/node/oval TYPE_URI:
 * https://spec.canvasprotocol.org/v0.6/extensions/oval-node.json
 * <p>
 * Has the same properties as Rectangle; rendering differs (ellipse within bounding box).
 * </p>
 */
public class OvalNodeExtension extends OcifExtension implements IOcifNodeExtension {

    public static final String TYPE_NAME = "@ocif/oval";
    public static final String TYPE_URI = "https://spec.canvasprotocol.org/v0.7.0/extensions/oval.json";
    /** @deprecated v0.6 name kept for backward-compatible reading */
    @Deprecated public static final String TYPE_NAME_V0_6 = "@ocif/node/oval";
    /** default 1 */
    private Double strokeWidth;
    /** default #FFFFFF */
    private OcifColor strokeColor;
    /** default none */
    private OcifColor fillColor;

    public OvalNodeExtension() {
        super(TYPE_URI, TYPE_NAME);
    }

    public static @NonNull IOcifExtension of(@NonNull IJsonObject obj) {
        OvalNodeExtension ext = new OvalNodeExtension();
        obj.getMaybeAs(OCIF.Common.STROKE_WIDTH, IJsonValue::asNumber, n -> ext.setStrokeWidth(n.doubleValue()));
        obj.getMaybeAs(OCIF.Common.STROKE_COLOR, OcifColor::of, ext::setStrokeColor);
        obj.getMaybeAs(OCIF.Common.FILL_COLOR, OcifColor::of, ext::setFillColor);
        return ext;
    }

    @Override
    public Set<String> definedKeys() {
        return Set.of(OCIF.Common.STROKE_WIDTH, OCIF.Common.STROKE_COLOR, OCIF.Common.FILL_COLOR);
    }


    public OcifColor fillColor() {return fillColor;}

    public OvalNodeExtension setFillColor(OcifColor fillColor) {
        this.fillColor = fillColor;
        return this;
    }

    public OvalNodeExtension setStrokeColor(OcifColor strokeColor) {
        this.strokeColor = strokeColor;
        return this;
    }

    public OvalNodeExtension setStrokeWidth(Double strokeWidth) {
        this.strokeWidth = strokeWidth;
        return this;
    }

    public OcifColor strokeColor() {return strokeColor;}

    public Double strokeWidth() {return strokeWidth;}

    @Override
    public @NonNull IJsonObject toJson() {
        IJsonObjectMutable o = factory().createObjectMutable();
        o.setString(TYPE, TYPE_NAME);
        ifPresentAccept(strokeWidth, v -> o.setNumber(OCIF.Common.STROKE_WIDTH, v));
        ifPresentAccept(strokeColor, v -> o.add(OCIF.Common.STROKE_COLOR, v.toJson()));
        ifPresentAccept(fillColor, v -> o.add(OCIF.Common.FILL_COLOR, v.toJson()));
        return o;
    }

}

package com.graphinout.reader.ocif.document.extension.node;

import com.graphinout.foundation.pure.json.document.IJsonArray;
import com.graphinout.foundation.pure.json.document.IJsonObject;
import com.graphinout.foundation.pure.json.document.IJsonValue;
import com.graphinout.reader.ocif.OCIF;
import com.graphinout.reader.ocif.document.extension.IOcifExtension;
import com.graphinout.reader.ocif.document.extension.OcifExtension;
import com.graphinout.reader.ocif.document.types.OcifColor;
import org.jspecify.annotations.NonNull;

import java.util.Set;

/**
 * Arrow Node Extension.
 * <p>
 * Spec: reader-ocif/src/spec-v0.6/spec.md → "Node Extensions → Arrow Extension"
 * <p>
 * Properties:
 * <li>strokeWidth (number): line width
 * <li>strokeColor (Color string)
 * <li>start (number[2|3], required)
 * <li>end (number[2|3], required)
 * <li>startMarker (string): none|arrowhead
 * <li>endMarker (string): none|arrowhead
 * </p>
 */
public class ArrowNodeExtension extends OcifExtension implements IOcifNodeExtension {

    public static final String TYPE_NAME = "@ocif/node/arrow";
    public static final String TYPE_URI = "https://spec.canvasprotocol.org/v0.6/extensions/arrow-node.json";
    /** default 1 */
    private Double strokeWidth;
    /** default #FFFFFF */
    private OcifColor strokeColor;
    /** [x,y,(z)] */
    private IJsonArray start;
    /** [x,y,(z)] */
    private IJsonArray end;
    /** none|arrowhead */
    private String startMarker;
    /** none|arrowhead */
    private String endMarker;

    public ArrowNodeExtension() {
        super(TYPE_URI, TYPE_NAME);
    }

    public static @NonNull IOcifExtension of(@NonNull IJsonObject obj) {
        ArrowNodeExtension ext = new ArrowNodeExtension();

        obj.getMaybeAs(OCIF.Common.STROKE_WIDTH, IJsonValue::asNumber, n -> ext.setStrokeWidth(n.doubleValue()));

        obj.getMaybeAs(OCIF.Common.STROKE_COLOR,
                OcifColor::of,
                ext::setStrokeColor);

        obj.getMaybeAs(OCIF.Common.START, IJsonValue::asArray, ext::setStart);

        obj.getMaybeAs(OCIF.Common.END, IJsonValue::asArray, ext::setEnd);

        obj.getIfString(OCIF.Common.START_MARKER, ext::setStartMarker);

        obj.getIfString(OCIF.Common.END_MARKER, ext::setEndMarker);
        return ext;
    }

    @Override
    public Set<String> definedKeys() {
        return Set.of(OCIF.Common.STROKE_WIDTH, OCIF.Common.STROKE_COLOR, OCIF.Common.START, OCIF.Common.END, OCIF.Common.START_MARKER, OCIF.Common.END_MARKER);
    }

    public IJsonArray end() {return end;}

    public String endMarker() {return endMarker;}

    public ArrowNodeExtension setEnd(IJsonArray end) {
        this.end = end;
        return this;
    }

    public ArrowNodeExtension setEndMarker(String endMarker) {
        this.endMarker = endMarker;
        return this;
    }

    public ArrowNodeExtension setStart(IJsonArray start) {
        this.start = start;
        return this;
    }

    public ArrowNodeExtension setStartMarker(String startMarker) {
        this.startMarker = startMarker;
        return this;
    }

    public ArrowNodeExtension setStrokeColor(OcifColor strokeColor) {
        this.strokeColor = strokeColor;
        return this;
    }

    public ArrowNodeExtension setStrokeWidth(Double strokeWidth) {
        this.strokeWidth = strokeWidth;
        return this;
    }

    public IJsonArray start() {return start;}

    public String startMarker() {return startMarker;}

    public OcifColor strokeColor() {return strokeColor;}

    public Double strokeWidth() {return strokeWidth;}

}

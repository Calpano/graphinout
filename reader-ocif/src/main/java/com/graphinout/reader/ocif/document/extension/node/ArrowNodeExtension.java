package com.graphinout.reader.ocif.document.extension.node;

import com.graphinout.foundation.pure.json.document.IJsonArray;
import org.jspecify.annotations.Nullable;
import com.graphinout.foundation.pure.json.document.IJsonObject;
import com.graphinout.foundation.pure.json.document.IJsonObjectMutable;
import com.graphinout.foundation.pure.json.document.IJsonValue;
import com.graphinout.reader.ocif.OCIF;
import com.graphinout.reader.ocif.document.extension.IOcifExtension;
import com.graphinout.reader.ocif.document.extension.OcifExtension;
import com.graphinout.reader.ocif.document.types.OcifColor;
import org.jspecify.annotations.NonNull;

import java.util.Set;

import static com.graphinout.foundation.pure.functional.Nullables.ifPresentAccept;
import static com.graphinout.reader.ocif.Ocifs.factory;

/**
 * Arrow Node Extension.
 * <p>
 * Spec: https://github.com/ocwg/ocif-spec/blob/main/spec/v0.6/spec.md → "Node Extensions → Arrow Extension"
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

    public static final String TYPE_NAME = "@ocif/arrow";
    public static final String TYPE_URI = "https://spec.canvasprotocol.org/v0.7.1/extensions/arrow.json";
    /** @deprecated v0.6 name kept for backward-compatible reading */
    @Deprecated public static final String TYPE_NAME_V0_6 = "@ocif/node/arrow";
    /** default 1 */
    private @Nullable Double strokeWidth;
    /** default #FFFFFF */
    private @Nullable OcifColor strokeColor;
    /** [x,y,(z)] */
    private IJsonArray start;
    /** [x,y,(z)] */
    private IJsonArray end;
    /** none|arrowhead */
    private @Nullable String startMarker;
    /** none|arrowhead */
    private @Nullable String endMarker;

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

    public @Nullable String endMarker() {return endMarker;}

    public ArrowNodeExtension setEnd(IJsonArray end) {
        this.end = end;
        return this;
    }

    public ArrowNodeExtension setEndMarker(@Nullable String endMarker) {
        this.endMarker = endMarker;
        return this;
    }

    public ArrowNodeExtension setStart(IJsonArray start) {
        this.start = start;
        return this;
    }

    public ArrowNodeExtension setStartMarker(@Nullable String startMarker) {
        this.startMarker = startMarker;
        return this;
    }

    public ArrowNodeExtension setStrokeColor(@Nullable OcifColor strokeColor) {
        this.strokeColor = strokeColor;
        return this;
    }

    public ArrowNodeExtension setStrokeWidth(@Nullable Double strokeWidth) {
        this.strokeWidth = strokeWidth;
        return this;
    }

    public IJsonArray start() {return start;}

    public @Nullable String startMarker() {return startMarker;}

    public @Nullable OcifColor strokeColor() {return strokeColor;}

    public @Nullable Double strokeWidth() {return strokeWidth;}

    @Override
    public @NonNull IJsonObject toJson() {
        IJsonObjectMutable o = factory().createObjectMutable();
        o.setString(TYPE, TYPE_NAME);
        ifPresentAccept(strokeWidth, v -> o.setNumber(OCIF.Common.STROKE_WIDTH, v));
        ifPresentAccept(strokeColor, v -> o.add(OCIF.Common.STROKE_COLOR, v.toJson()));
        ifPresentAccept(start, v -> o.add(OCIF.Common.START, v));
        ifPresentAccept(end, v -> o.add(OCIF.Common.END, v));
        ifPresentAccept(startMarker, v -> o.setString(OCIF.Common.START_MARKER, v));
        ifPresentAccept(endMarker, v -> o.setString(OCIF.Common.END_MARKER, v));
        return o;
    }

}

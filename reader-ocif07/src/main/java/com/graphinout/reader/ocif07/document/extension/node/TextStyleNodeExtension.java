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

public class TextStyleNodeExtension extends OcifExtension implements IOcifNodeExtension {

    public static final String TYPE_NAME = "@ocif/textstyle";
    public static final String TYPE_URI = "https://spec.canvasprotocol.org/v0.7.0/extensions/textstyle.json";
    /** @deprecated v0.6 name kept for backward-compatible reading */
    @Deprecated public static final String TYPE_NAME_V0_6 = "@ocif/node/textstyle";
    /** e.g., 12 */
    private Double fontSizePx;
    /** e.g., sans-serif */
    private String fontFamily;
    /** e.g., #000000 */
    private OcifColor color;
    /** left|right|center|justify */
    private String align;
    private boolean bold;
    private boolean italic;


    public TextStyleNodeExtension() {
        super(TYPE_URI, TYPE_NAME);
    }

    public static @NonNull IOcifExtension of(@NonNull IJsonObject obj) {
        TextStyleNodeExtension ext = new TextStyleNodeExtension();
        obj.getMaybeAs(OCIF.Common.FONT_SIZE_PX, IJsonValue::asNumber, n -> ext.setFontSizePx(n.doubleValue()));
        obj.getIfString(OCIF.Common.FONT_FAMILY, ext::setFontFamily);
        obj.getMaybeAs(OCIF.Common.COLOR, OcifColor::of, ext::setColor);
        obj.getIfString(OCIF.Common.ALIGN, ext::setAlign);
        obj.getMaybeAs(OCIF.Common.BOLD, IJsonValue::asBooleanOrNull, b -> {if (b != null) ext.setBold(b);});
        obj.getMaybeAs(OCIF.Common.ITALIC, IJsonValue::asBooleanOrNull, b -> {if (b != null) ext.setItalic(b);});
        return ext;
    }

    public String align() {return align;}

    public Boolean bold() {return bold;}

    public OcifColor color() {return color;}

    @Override
    public Set<String> definedKeys() {
        return Set.of(OCIF.Common.FONT_SIZE_PX, OCIF.Common.FONT_FAMILY, OCIF.Common.COLOR, OCIF.Common.ALIGN, OCIF.Common.BOLD, OCIF.Common.ITALIC);
    }



    public String fontFamily() {return fontFamily;}

    public Double fontSizePx() {return fontSizePx;}

    public Boolean italic() {return italic;}

    public TextStyleNodeExtension setAlign(String align) {
        this.align = align;
        return this;
    }

    public TextStyleNodeExtension setBold(Boolean bold) {
        this.bold = bold;
        return this;
    }

    public TextStyleNodeExtension setColor(OcifColor color) {
        this.color = color;
        return this;
    }

    public TextStyleNodeExtension setFontFamily(String fontFamily) {
        this.fontFamily = fontFamily;
        return this;
    }

    public TextStyleNodeExtension setFontSizePx(Double fontSizePx) {
        this.fontSizePx = fontSizePx;
        return this;
    }

    public TextStyleNodeExtension setItalic(Boolean italic) {
        this.italic = italic;
        return this;
    }

    @Override
    public @NonNull IJsonObject toJson() {
        IJsonObjectMutable o = factory().createObjectMutable();
        o.setString(TYPE, TYPE_NAME);
        ifPresentAccept(fontSizePx, v -> o.setNumber(OCIF.Common.FONT_SIZE_PX, v));
        ifPresentAccept(fontFamily, v -> o.setString(OCIF.Common.FONT_FAMILY, v));
        ifPresentAccept(color, v -> o.add(OCIF.Common.COLOR, v.toJson()));
        ifPresentAccept(align, v -> o.setString(OCIF.Common.ALIGN, v));
        o.setBoolean(OCIF.Common.BOLD, bold);
        o.setBoolean(OCIF.Common.ITALIC, italic);
        return o;
    }

}

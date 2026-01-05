package com.graphinout.reader.ocif.document.extension.node;

import com.graphinout.foundation.pure.json.document.IJsonObject;
import com.graphinout.foundation.pure.json.document.IJsonValue;
import com.graphinout.reader.ocif.OCIF;
import com.graphinout.reader.ocif.document.extension.IOcifExtension;
import com.graphinout.reader.ocif.document.extension.OcifExtension;
import com.graphinout.reader.ocif.document.types.OcifColor;
import org.jspecify.annotations.NonNull;

import java.util.Set;

public class TextStyleNodeExtension extends OcifExtension implements IOcifNodeExtension {

    public static final String TYPE_NAME = "@ocif/node/textstyle";
    public static final String TYPE_URI = "https://spec.canvasprotocol.org/v0.6/extensions/textstyle-node.json";
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

}

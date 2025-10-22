package com.graphinout.reader.ocif.document.extension;

import com.graphinout.foundation.json.value.IJsonObject;

/**
 * Text Style Node Extension.
 * <p>
 * Spec: reader-ocif/src/spec-v0.6/spec.md → "Node Extensions → Text Style Node Extension"
 * Name: @ocif/node/textstyle
 * URI: https://spec.canvasprotocol.org/v0.6/extensions/textstyle-node.json
 * <p>
 * Properties:
 * - fontSizePx (number)
 * - fontFamily (string)
 * - color (Color string)
 * - align (string: left|right|center|justify)
 * - bold (boolean)
 * - italic (boolean)
 * </p>
 */
public class OcifTextStyleNodeExt implements IOcifExtension {
    public static final String TYPE = "@ocif/node/textstyle";
    public static final String URI = "https://spec.canvasprotocol.org/v0.6/extensions/textstyle-node.json";

    private Double fontSizePx; // e.g., 12
    private String fontFamily; // e.g., sans-serif
    private String color;      // e.g., #000000
    private String align;      // left|right|center|justify
    private Boolean bold;
    private Boolean italic;
    private IJsonObject extras;

    @Override public String typeName() { return TYPE; }
    @Override public String typeUri() { return URI; }

    public Double getFontSizePx() { return fontSizePx; }
    public OcifTextStyleNodeExt setFontSizePx(Double fontSizePx) { this.fontSizePx = fontSizePx; return this; }

    public String getFontFamily() { return fontFamily; }
    public OcifTextStyleNodeExt setFontFamily(String fontFamily) { this.fontFamily = fontFamily; return this; }

    public String getColor() { return color; }
    public OcifTextStyleNodeExt setColor(String color) { this.color = color; return this; }

    public String getAlign() { return align; }
    public OcifTextStyleNodeExt setAlign(String align) { this.align = align; return this; }

    public Boolean getBold() { return bold; }
    public OcifTextStyleNodeExt setBold(Boolean bold) { this.bold = bold; return this; }

    public Boolean getItalic() { return italic; }
    public OcifTextStyleNodeExt setItalic(Boolean italic) { this.italic = italic; return this; }

    public IJsonObject getExtras() { return extras; }
    public OcifTextStyleNodeExt setExtras(IJsonObject extras) { this.extras = extras; return this; }
}

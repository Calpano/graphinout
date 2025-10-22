package com.graphinout.reader.ocif.document.extension;

import com.graphinout.foundation.json.value.IJsonObject;

/**
 * Theme Node Extension.
 * <p>
 * Spec: reader-ocif/src/spec-v0.6/spec.md → "Node Extensions → Theme Node Extension" and "Theme Selection"
 * Name: @ocif/node/theme
 * URI: https://spec.canvasprotocol.org/v0.6/extensions/theme-node.json
 * <p>
 * This extension both defines named themes (as nested extension data under named keys)
 * and optionally selects one via the "select-theme" property. The default selection is null (no theme).
 * </p>
 */
public class OcifThemeNodeExt implements IOcifExtension {
    public static final String TYPE = "@ocif/node/theme";
    public static final String URI = "https://spec.canvasprotocol.org/v0.6/extensions/theme-node.json";

    /** Map-like object holding top-level theme names as properties; each value typically contains a "data" array. */
    private IJsonObject themes;
    /** Optional: Name of the theme to select; null selects the default (no theme). Uses JSON key "select-theme". */
    private String selectTheme;
    private IJsonObject extras;

    @Override public String typeName() { return TYPE; }
    @Override public String typeUri() { return URI; }

    public IJsonObject getThemes() { return themes; }
    public OcifThemeNodeExt setThemes(IJsonObject themes) { this.themes = themes; return this; }

    public String getSelectTheme() { return selectTheme; }
    public OcifThemeNodeExt setSelectTheme(String selectTheme) { this.selectTheme = selectTheme; return this; }

    public IJsonObject getExtras() { return extras; }
    public OcifThemeNodeExt setExtras(IJsonObject extras) { this.extras = extras; return this; }
}

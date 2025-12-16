package com.graphinout.reader.ocif.document.extension;

import com.graphinout.foundation.pure.json.document.IJsonObject;
import com.graphinout.foundation.pure.json.document.IJsonValue;
import com.graphinout.reader.ocif.OCIF;
import org.jspecify.annotations.NonNull;

import java.util.Set;

/**
 * Theme Node Extension.
 * <p>
 * Spec: reader-ocif/src/spec-v0.6/spec.md → "Node Extensions → Theme Node Extension" and "Theme Selection" Name:
 *
 * @ocif/node/theme TYPE_URI: https://spec.canvasprotocol.org/v0.6/extensions/theme-node.json
 * <p>
 * This extension both defines named themes (as nested extension data under named keys) and optionally selects one via
 * the "select-theme" property. The default selection is null (no theme).
 * </p>
 */
public class ThemeNodeExtension extends OcifExtension {

    public static final String TYPE_NAME = "@ocif/node/theme";
    public static final String TYPE_URI = "https://spec.canvasprotocol.org/v0.6/extensions/theme-node.json";
    /** Map-like object holding top-level theme names as properties; each value typically contains a "data" array. */
    private IJsonObject themes;
    /** Optional: Name of the theme to select; null selects the default (no theme). Uses JSON key "select-theme". */
    private String selectTheme;
    
    public ThemeNodeExtension() {
        super(TYPE_URI, TYPE_NAME);
    }

    public static @NonNull IOcifExtension of(@NonNull IJsonObject obj) {
        ThemeNodeExtension ext = new ThemeNodeExtension();
        obj.getIfString(OCIF.Common.SELECT_THEME, ext::setSelectTheme);
        obj.getMaybeAs(OCIF.Common.THEMES, IJsonValue::asObjectOrNull, v -> {if (v != null) ext.setThemes(v);});
        return ext;
    }

    @Override
    public Set<String> definedKeys() {
        return Set.of(OCIF.Common.THEMES, OCIF.Common.SELECT_THEME);
    }

    

    public String selectTheme() {return selectTheme;}

    public ThemeNodeExtension setSelectTheme(String selectTheme) {
        this.selectTheme = selectTheme;
        return this;
    }

    public ThemeNodeExtension setThemes(IJsonObject themes) {
        this.themes = themes;
        return this;
    }

    public IJsonObject themes() {return themes;}

}

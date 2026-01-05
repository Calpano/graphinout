package com.graphinout.reader.ocif.document.extension.node;

import com.graphinout.foundation.pure.collections.jajson.JaJson;
import com.graphinout.foundation.pure.json.document.IJsonObject;
import com.graphinout.foundation.pure.json.document.IJsonObjectMutable;
import com.graphinout.foundation.pure.json.document.IJsonValue;
import com.graphinout.reader.ocif.document.extension.IOcifExtension;
import com.graphinout.reader.ocif.document.extension.OcifExtension;
import org.jspecify.annotations.NonNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static com.graphinout.foundation.pure.functional.Nullables.ifPresentAccept;
import static com.graphinout.reader.ocif.OCIF.Common.SELECT_THEME;
import static com.graphinout.reader.ocif.OCIF.Common.THEMES;
import static com.graphinout.reader.ocif.Ocifs.factory;

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
public class ThemeNodeExtension extends OcifExtension implements IOcifNodeExtension {

    public static final String TYPE_NAME = "@ocif/node/theme";
    public static final String TYPE_URI = "https://spec.canvasprotocol.org/v0.6/extensions/theme-node.json";
    /** Map-like object holding top-level theme names as properties; each value typically contains a "data" array. */
    private Map<String, Object> themes = new HashMap<>();
    /** Optional: Name of the theme to select; null selects the default (no theme). Uses JSON key "select-theme". */
    private String selectTheme;

    public ThemeNodeExtension() {
        super(TYPE_URI, TYPE_NAME);
    }

    public static @NonNull IOcifExtension of(@NonNull IJsonObject obj) {
        ThemeNodeExtension ext = new ThemeNodeExtension();
        obj.getIfString(SELECT_THEME, ext::setSelectTheme);
        obj.getMaybeAs(THEMES, IJsonValue::asObjectOrNull, o -> {if (o != null) ext.themes(o.toJaJsonMap());});
        return ext;
    }

    @Override
    public Set<String> definedKeys() {
        return Set.of(THEMES, SELECT_THEME);
    }


    public String selectTheme() {return selectTheme;}

    public ThemeNodeExtension setSelectTheme(String selectTheme) {
        this.selectTheme = selectTheme;
        return this;
    }

    public Map<String, Object> themes() {return themes;}

    public ThemeNodeExtension themes(Map<String, Object> themes) {
        this.themes = themes;
        return this;
    }

    public Map<String, Object> toMap() {
        return JaJson.createMap()//
                .putNonNull(TYPE, typeUri())//
                .putMaybe(SELECT_THEME, selectTheme())//
                .putMaybe(THEMES, themes()).build();
    }

    @Override
    public @NonNull IJsonObject toJson() {
        IJsonObjectMutable o = factory().createObjectMutable();
        o.setString(TYPE, TYPE_NAME);
        ifPresentAccept(selectTheme, v -> o.setString(SELECT_THEME, v));
        if (!themes.isEmpty()) {
            o.addObject(THEMES, themesObj -> themesObj.addAllFromJaJson(themes));
        }
        return o;
    }

}

package com.graphinout.reader.ocif.document.extension.representation;

import com.graphinout.foundation.pure.json.document.IJsonObject;
import com.graphinout.foundation.pure.json.document.IJsonValue;
import com.graphinout.reader.ocif.document.extension.IOcifExtension;
import com.graphinout.reader.ocif.document.extension.OcifExtension;
import com.graphinout.reader.ocif.document.extension.canvas.IOcifCanvasExtension;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Set;

import static com.graphinout.foundation.pure.functional.Nullables.mapOrNull;
import static java.util.stream.Collectors.joining;

/**
 * TODO add to CJ docs, maybe link from OCIF docs as example
 * <p>
 * An OCIF Representation Extension to represent the Connected JSON (CJ) language.
 * See <a href="https://calpano.github.io/connected-json/spec-cj.html">CJ spec</a>.
 */
public class CjLanguageRepresentationExtension extends OcifExtension implements IOcifCanvasExtension {

    public static final String TYPE_NAME = "@connected-json/language";
    public static final String TYPE_URI = "https://j-s-o-n.org/ocif-language/schema.json";
    public static final String LANGUAGE = "language";

    public CjLanguageRepresentationExtension() {
        super(TYPE_URI, TYPE_NAME);
    }

    public static @NonNull IOcifExtension of(@NonNull IJsonObject obj) {
        CjLanguageRepresentationExtension data = new CjLanguageRepresentationExtension();
        obj.forEach(data::set);
        return data;
    }

    public CjLanguageRepresentationExtension copy() {
        CjLanguageRepresentationExtension data = new CjLanguageRepresentationExtension();
        map().forEach(data::set);
        return data;
    }

    @Override
    public Set<String> definedKeys() {
        return Set.of(LANGUAGE);
    }

    public boolean isEmpty() {
        return map().isEmpty();
    }

    public void language(@NonNull String language) {
        set(LANGUAGE, language);
    }

    public @Nullable String language() {
        return mapOrNull(map(), m -> m.get(LANGUAGE), IJsonValue::asString);
    }

    @Override
    public String toString() {
        return "CjLanguageRepresentationExtension{" + map().entrySet().stream().map(e -> e.getKey() + "='" + e.getValue() + "'").collect(joining(", ")) + "}";
    }

}

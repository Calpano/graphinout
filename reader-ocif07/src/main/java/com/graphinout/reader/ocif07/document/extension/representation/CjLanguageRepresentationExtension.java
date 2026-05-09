package com.graphinout.reader.ocif07.document.extension.representation;

import com.graphinout.base.cj.CjConstants;
import com.graphinout.foundation.pure.json.document.IJsonObject;
import com.graphinout.foundation.pure.json.document.IJsonObjectMutable;
import com.graphinout.reader.ocif07.document.extension.IOcifExtension;
import com.graphinout.reader.ocif07.document.extension.OcifExtension;
import com.graphinout.reader.ocif07.document.extension.canvas.IOcifCanvasExtension;
import org.jspecify.annotations.NonNull;

import java.util.Set;

import static com.graphinout.foundation.pure.functional.Nullables.ifPresentAccept;
import static com.graphinout.reader.ocif07.Ocifs.factory;

/**
 * TODO add to CJ docs, maybe link from OCIF docs as example
 * <p>
 * An OCIF Representation Extension to represent the Connected JSON (CJ) language.
 * See <a href="https://calpano.github.io/connected-json/spec-cj.html">CJ spec</a>.
 */
public class CjLanguageRepresentationExtension extends OcifExtension implements IOcifCanvasExtension {

    public static final String TYPE_NAME = "@connected-json/language";
    public static final String TYPE_URI = "https://j-s-o-n.org/ocif-language/schema.json";
    private @NonNull String language;

    public CjLanguageRepresentationExtension(@NonNull String language) {
        super(TYPE_URI, TYPE_NAME);
        this.language = language;
    }

    public static @NonNull IOcifExtension of(@NonNull IJsonObject obj) {
        return new CjLanguageRepresentationExtension(obj.getString(CjConstants.LANGUAGE));
    }

    public CjLanguageRepresentationExtension copy() {
        return new CjLanguageRepresentationExtension(language());
    }

    @Override
    public Set<String> definedKeys() {
        return Set.of(CjConstants.LANGUAGE);
    }

    public boolean isEmpty() {
        return false;
    }

    public void language(@NonNull String language) {
        this.language = language;
    }

    public @NonNull String language() {
        return language;
    }

    @Override
    public @NonNull IJsonObject toJson() {
        IJsonObjectMutable o = factory().createObjectMutable();
        o.setString(TYPE, TYPE_NAME);
        ifPresentAccept(language(), v -> o.setString(CjConstants.LANGUAGE, v));
        return o;
    }

    @Override
    public String toString() {
        return "CjLanguageRepresentationExtension{" + language() + "}";
    }

}

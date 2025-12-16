package com.graphinout.reader.ocif.document.extension;

import com.graphinout.foundation.pure.json.document.IJsonObject;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Set;

/**
 * TODO add to spec
 * <p>
 * A generic JSON data extension.
 */
public class DataExtension extends OcifExtension {

    public static final String TYPE_NAME = "@ocif/data";
    public static final String TYPE_URI = "https://spec.canvasprotocol.org/v0.6/extensions/data.json";

    public DataExtension() {
        super(TYPE_URI, TYPE_NAME);
    }

    public static @NonNull IOcifExtension of(@NonNull IJsonObject obj) {
        DataExtension data = new DataExtension();
        obj.forEach(data::set);
        return data;
    }

    @Override
    public Set<String> definedKeys() {
        return Set.of();
    }

    @Override
    public @Nullable String typeName() {
        return TYPE_NAME;
    }

    @Override
    public @NonNull String typeUri() {
        return TYPE_URI;
    }

}

package com.graphinout.reader.ocif.document.extension;

import com.graphinout.foundation.pure.json.document.IJsonValue;
import com.graphinout.reader.ocif.document.impl.DecoratedJsonObject;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Wraps a {@link IJsonValue} from a {@code data[]} array.
 */
public abstract class OcifExtension extends DecoratedJsonObject implements IOcifExtension {

    private final @NonNull String typeUri;
    private final @Nullable String typeName;

    public OcifExtension(@NonNull String typeUri, @Nullable String typeName) {
        this.typeUri = typeUri;
        this.typeName = typeName;
    }

    @Override
    public @Nullable String typeName() {
        return typeName;
    }

    @Override
    public @NonNull String typeUri() {
        return typeUri;
    }


}

package com.graphinout.base.input;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Set;

/**
 * An {@link InputSource} that additionally carries key/value parameters (e.g. a base URI) for the reader.
 */
public interface ParameterizedInputSource extends InputSource {

    @Nullable String getValue(String key);

    @NonNull Set<@NonNull String> keys();

}

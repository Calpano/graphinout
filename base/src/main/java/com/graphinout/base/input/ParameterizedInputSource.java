package com.graphinout.base.input;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Set;

public interface ParameterizedInputSource extends InputSource {

    @Nullable String getValue(String key);

    @NonNull Set<@NonNull String> keys();

}

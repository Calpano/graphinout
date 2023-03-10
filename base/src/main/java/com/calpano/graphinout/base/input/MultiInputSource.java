package com.calpano.graphinout.base.input;

import java.util.Set;

public interface MultiInputSource extends InputSource {

    SingleInputSource getNamedSource(String name);

    default boolean isSingle() {
        return false;
    }

    /**
     * e.g. the canonical directory or archive name or file name prefix
     */
    @Override
    String name();

    Set<String> names();


}
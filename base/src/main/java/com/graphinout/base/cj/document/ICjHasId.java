package com.graphinout.base.cj.document;

import org.jspecify.annotations.Nullable;

/**
 * Mixin for CJ elements that have an optional string id.
 */
public interface ICjHasId {

    @Nullable
    String id();

}

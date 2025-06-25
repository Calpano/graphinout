package com.calpano.graphinout.foundation.json;

import edu.umd.cs.findbugs.annotations.Nullable;

public interface SajWriter extends JsonEventStream {

    /** Convenience method only used for writing JSON */
    default void onBoolean(@Nullable Boolean b) throws JsonException {
        if (b == null) onNull();
        else onBoolean(b.booleanValue());
    }

    /** Convenience method only used for writing JSON */
    default void onFloat(@Nullable Float f) throws JsonException {
        if (f == null) onNull();
        else onFloat(f.floatValue());
    }

    /** Convenience method only used for writing JSON */
    default void onInteger(@Nullable Integer i) throws JsonException {
        if (i == null) onNull();
        else onInteger(i.intValue());
    }

    /** Convenience method only used for writing JSON */
    default void onLong(@Nullable Long l) throws JsonException {
        if (l == null) onNull();
        else onLong(l.longValue());
    }

}

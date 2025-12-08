package com.graphinout.foundation.pure.functional;

import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Cascading lookup to parent
 * <p>
 * Non-type safe: Users must know what they do.
 */
public class ChainedObjectMap {

    @Nullable final ChainedObjectMap parent;
    final Map<String, Supplier<Object>> map = new HashMap<>();

    public ChainedObjectMap(@Nullable ChainedObjectMap parent) {
        this.parent = parent;
    }

    public void registerLookup(String key, Supplier<Object> supplied) {
        map.put(key, supplied);
    }

    @SuppressWarnings("unchecked")
    public <T> Optional<T> value(String key) {
        // cascading lookup, nesting on the call stack until a value is found
        return Optionals.or(Optional.ofNullable(map.get(key)).map(Supplier::get), () -> Optional.ofNullable(parent).flatMap(parentMap -> parentMap.value(key))).map(object -> (T) object);
    }

}

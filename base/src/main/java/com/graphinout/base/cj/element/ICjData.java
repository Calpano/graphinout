package com.graphinout.base.cj.element;

import com.graphinout.foundation.json.path.IJsonContainerNavigationStep;
import com.graphinout.foundation.json.value.IJsonFactory;
import com.graphinout.foundation.json.value.IJsonValue;

import javax.annotation.Nullable;
import java.util.List;
import java.util.stream.Stream;

import static com.graphinout.foundation.util.Nullables.mapOrDefault;

public interface ICjData extends ICjElement {

    @Override
    default Stream<ICjElement> directChildren() {
        // CJ data has no sub-elements
        return Stream.empty();
    }

    IJsonFactory factory();

    default boolean has(List<IJsonContainerNavigationStep> path) {
        return mapOrDefault(jsonValue(), j -> j.has(path), false);
    }

    default boolean hasProperty(String propertyKey) {
        return has(IJsonContainerNavigationStep.pathOf(propertyKey));
    }

    /**
     * The current JSON content
     */
    @Nullable
    IJsonValue jsonValue();

}

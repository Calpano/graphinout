package com.calpano.graphinout.foundation.json.path;

import com.calpano.graphinout.foundation.json.JsonType;

import java.util.List;
import java.util.stream.Stream;

public interface IJsonContainerNavigationStep {

    /**
     * @param step String or Integer
     * @return a {@link IJsonObjectNavigationStep} (from String) or  {@link IJsonArrayNavigationStep} (from Integer)
     */
    static IJsonContainerNavigationStep of(Object step) {
        if (step instanceof String) {
            return IJsonObjectNavigationStep.of((String) step);
        } else if (step instanceof Integer) {
            return IJsonArrayNavigationStep.of((Integer) step);
        } else {
            throw new IllegalArgumentException("step must be String or Integer");
        }
    }

    static List<IJsonContainerNavigationStep> pathOf(Object... steps) {
        return Stream.of(steps).map(IJsonContainerNavigationStep::of).toList();
    }

    default IJsonArrayNavigationStep asArrayStep() throws ClassCastException {
        return (IJsonArrayNavigationStep) this;
    }

    default IJsonObjectNavigationStep asObjectStep() throws ClassCastException {
        return (IJsonObjectNavigationStep) this;
    }

    JsonType.ContainerType containerType();


}

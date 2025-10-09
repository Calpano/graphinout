package com.calpano.graphinout.foundation.json.path;

import com.calpano.graphinout.foundation.json.JsonType;

public interface IJsonArrayNavigationStep extends IJsonContainerNavigationStep {

    static IJsonArrayNavigationStep of(int index) {
        return new IJsonArrayNavigationStep() {
            @Override
            public int index() {
                return index;
            }

            @Override
            public String toString() {
                return "[" + index + "]";
            }

        };
    }

    default JsonType.ContainerType containerType() {
        return JsonType.ContainerType.Array;
    }

    int index();

}

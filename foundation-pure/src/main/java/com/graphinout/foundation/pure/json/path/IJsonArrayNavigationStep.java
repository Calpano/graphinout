package com.graphinout.foundation.pure.json.path;

import com.graphinout.foundation.pure.json.JsonType;

/**
 * A JSON path step into an array by integer index.
 */
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

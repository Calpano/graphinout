package com.calpano.graphinout.foundation.json.path;

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

    int index();

}

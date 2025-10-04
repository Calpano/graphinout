package com.calpano.graphinout.foundation.json.path;

public interface IJsonObjectNavigationStep extends IJsonContainerNavigationStep {

    static IJsonObjectNavigationStep of(String propertyKey) {
        return new IJsonObjectNavigationStep() {
            @Override
            public String propertyKey() {return propertyKey;}

            @Override
            public String toString() {
                return "'" + propertyKey + "'";
            }
        };
    }

    String propertyKey();


}

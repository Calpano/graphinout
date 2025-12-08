package com.graphinout.foundation.pure.json.path;

import com.graphinout.foundation.pure.json.JsonType;

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

    default JsonType.ContainerType containerType() {
        return JsonType.ContainerType.Object;
    }

    String propertyKey();

}

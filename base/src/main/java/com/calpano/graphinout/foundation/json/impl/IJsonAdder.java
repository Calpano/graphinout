package com.calpano.graphinout.foundation.json.impl;

import com.calpano.graphinout.foundation.json.path.IJsonContainerNavigationStep;
import com.calpano.graphinout.foundation.json.path.IJsonObjectNavigationStep;
import com.calpano.graphinout.foundation.json.value.IJsonValue;

import java.util.List;

/** An API where you can add JSON data */
public interface IJsonAdder {

    /**
     * @param path create one via {@link IJsonObjectNavigationStep#pathOf(Object...)} -- may be empty
     * @param value to set
     */
    void addJson(List<IJsonContainerNavigationStep> path, IJsonValue value);



}

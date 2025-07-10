package com.calpano.graphinout.base.graphml;

import java.util.Map;

public interface XMLValue {

    /**
     * The full map of all attributes, built-in and 'extra attributes'
     */
    Map<String, String> getAttributes();

}

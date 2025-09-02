package com.calpano.graphinout.base.graphml;

import java.util.Map;

public interface IXmlElement {

    String tagName();

    /**
     * The full map of all attributes, built-in and 'extra attributes'
     */
    Map<String, String> xmlAttributes();

}

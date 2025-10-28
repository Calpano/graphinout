package com.graphinout.reader.graphml.elements;

import java.util.Map;

public interface IXmlElement {

    String tagName();

    /**
     * The full map of <em>all</em> attributes, built-in and 'extra attributes'
     */
    Map<String, String> xmlAttributes();

}

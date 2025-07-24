package com.calpano.graphinout.base.graphml;

import javax.annotation.Nullable;
import java.util.Map;

public interface IXmlElement {

    default @Nullable String attribute(String attName) {
        return attributes().get(attName);
    }

    /**
     * The full map of all attributes, built-in and 'extra attributes'
     */
    Map<String, String> attributes();

    String tagName();

}

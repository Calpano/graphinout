package com.calpano.graphinout.base.graphml;

import com.calpano.graphinout.base.graphml.impl.GraphmlLocator;

import java.net.URL;
import java.util.Map;

public interface IGraphmlLocator extends IXmlElement {

    String TAGNAME = "locator";
    String ATTRIBUTE_XMLNS_XLINK = "xmlns:xlink";
    String ATTRIBUTE_XLINK_HREF = "xlink:href";
    String ATTRIBUTE_XLINK_TYPE = "xlink:type";

    // Builder
    static GraphmlLocator.GraphmlLocatorBuilder builder() {
        return new GraphmlLocator.GraphmlLocatorBuilder();
    }

    @Override
    Map<String, String> attributes();

    @Override
    String tagName();

    URL xlinkHref();

}

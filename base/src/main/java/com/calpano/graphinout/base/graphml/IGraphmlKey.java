package com.calpano.graphinout.base.graphml;

import com.calpano.graphinout.base.graphml.impl.GraphmlKey;

import java.util.LinkedHashMap;
import java.util.Set;

public interface IGraphmlKey extends IGraphmlWithDescElement, IXmlElement {

    String TAGNAME = "key";
    String ATTRIBUTE_FOR = "for";
    String ATTRIBUTE_ATTR_NAME = "attr.name";
    String ATTRIBUTE_ATTR_TYPE = "attr.type";

    // Builder
    static GraphmlKey.GraphmlKeyBuilder builder() {
        return new GraphmlKey.GraphmlKeyBuilder();
    }

    String attrName();

    String attrType();

    @Override
    LinkedHashMap<String, String> attributes();

    @Override
    String tagName();

    @Override
    Set<String> builtInAttributeNames();

    IGraphmlDefault defaultValue();

    GraphmlKeyForType forType();

    String id();

}

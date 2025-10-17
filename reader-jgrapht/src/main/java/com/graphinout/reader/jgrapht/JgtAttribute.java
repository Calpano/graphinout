package com.graphinout.reader.jgrapht;

import org.jgrapht.nio.AttributeType;

public class JgtAttribute {

    final String attributeName;

    final AttributeType attributeType;
    final String attributeValue;

    public JgtAttribute(String attributeName, AttributeType attributeType, String attributeValue) {
        this.attributeName = attributeName;
        this.attributeType = attributeType;
        this.attributeValue = attributeValue;
    }

}

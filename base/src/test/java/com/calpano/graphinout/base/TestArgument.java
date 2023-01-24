package com.calpano.graphinout.base;

import lombok.Data;

@Data
public class TestArgument {

    private XMLValue actual;
    private String expectedStartTag;
    private String expectedValueTag;
    private String expectedEndTag;

    public TestArgument(XMLValue data, String expectedStartTag, String expectedValueTag, String expectedEndTag) {
        this.actual = data;
        this.expectedStartTag = expectedStartTag;
        this.expectedValueTag = expectedValueTag;
        this.expectedEndTag = expectedEndTag;
    }

    public String getExpectedFullTag(){
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(expectedStartTag);
        stringBuilder.append(expectedValueTag);
        stringBuilder.append(expectedEndTag);
        return stringBuilder.toString();
    }

}

package com.calpano.graphinout.base.graphml;

import com.calpano.graphinout.base.graphml.impl.GraphmlDefault;

import java.util.Set;

public interface IGraphmlDefault extends IXmlElement{

    String TAGNAME = "default";

    String ATTRIBUTE_DEFAULT_TYPE = "default.type";

    static GraphmlDefault.GraphmlDefaultBuilder builder() {
        return new GraphmlDefault.GraphmlDefaultBuilder();
    }

    /**
     * Complex type for the &lt;default&gt; element. 'default.type' is mixed, that is, data may contain #PCDATA.
     * <p>
     * The name of this attribute in default is <b>default.type</b>
     */
    String defaultType();

    String value();

    default Set<String> builtInAttributes() {
        return Set.of(ATTRIBUTE_DEFAULT_TYPE);
    }

    @Override
    default String tagName() {
        return "default";
    }


}

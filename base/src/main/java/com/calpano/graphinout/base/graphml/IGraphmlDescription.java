package com.calpano.graphinout.base.graphml;

import com.calpano.graphinout.base.graphml.impl.GraphmlDescription;
import com.calpano.graphinout.foundation.xml.XmlWriter;

import java.io.IOException;
import java.util.Set;

public interface IGraphmlDescription extends IXmlElement {

    static GraphmlDescription.GraphmlDescriptionBuilder builder() {
        return new GraphmlDescription.GraphmlDescriptionBuilder();
    }

    String value();

    default Set<String> builtInAttributes() {
        return Set.of();
    }

    @Override
    default String tagName() {
        return "desc";
    }

    void writeXml(XmlWriter xmlWriter) throws IOException;

}

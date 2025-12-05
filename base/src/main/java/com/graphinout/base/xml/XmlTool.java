package com.graphinout.base.xml;

import com.graphinout.foundation.xml.document.XmlElement;
import com.graphinout.foundation.xml.XML;

public class XmlTool {

    public static XML.XmlSpace fromElement(XmlElement element) {
        return XML.XmlSpace.fromAttributesValue(element.attribute(XML.XML_SPACE));
    }

}

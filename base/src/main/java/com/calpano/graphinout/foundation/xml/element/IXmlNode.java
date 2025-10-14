package com.calpano.graphinout.foundation.xml.element;

import com.calpano.graphinout.foundation.xml.CharactersKind;
import com.calpano.graphinout.foundation.xml.XML;
import com.calpano.graphinout.foundation.xml.XmlFragmentString;
import com.calpano.graphinout.foundation.xml.XmlWriter;

import java.io.IOException;

public interface IXmlNode extends IXmlContent {

    void fire(XmlWriter writer) throws IOException;


    /**
     * Empty-ness depends on type.
     *
     * <h3>Elements</h3>
     * <li>{@link XmlDocument}: Is never empty, as it must have a root element to be valid.</li>
     * <li>{@link XmlElement}: Is empty if it (1) has no element children at all and (2) contains no text child, which is not empty.</li>
     * <li>{@link XmlText}: Is empty if it has no {@link XmlText.Section}, which is not empty</li>
     * <li>{@link XmlText.Section}: Is empty if the stated whitespace rules (via {@link CharactersKind}) allows reducing it to the empty string.</li>
     *
     * <h3>Fragments</h3>
     * <li>{@link XmlDocumentFragment}: Is empty, if it contains no child, which is not empty.</li>
     * <li>{@link XmlFragmentString}: Is empty, if the stated whitespace rules (via {@link CharactersKind}) allows reducing it to the empty string.</li>
     */
    boolean hasEmptyContent(XML.XmlSpace xmlSpace);

}

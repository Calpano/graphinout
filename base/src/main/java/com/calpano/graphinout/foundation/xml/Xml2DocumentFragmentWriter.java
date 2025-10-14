package com.calpano.graphinout.foundation.xml;

import com.calpano.graphinout.foundation.xml.element.Xml2DocumentWriter;
import com.calpano.graphinout.foundation.xml.element.XmlDocument;
import com.calpano.graphinout.foundation.xml.element.XmlDocumentFragment;
import com.calpano.graphinout.foundation.xml.element.XmlElement;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.Objects;


/**
 * Can only be used once. Can hold an XML fragment, i.e. the content of an XML element.
 * <p>
 * FIXME test
 */
public class Xml2DocumentFragmentWriter extends DelegatingXmlWriter implements XmlWriter {

    public static final IXmlName ROOT_WRAPPER = IXmlName.of("rootWrapper");
    private final Xml2DocumentWriter xmlWriter = new Xml2DocumentWriter();

    public Xml2DocumentFragmentWriter() {
        super.addWriter(xmlWriter);
    }

    public void fragmentEnd() {
        try {
            elementEnd(ROOT_WRAPPER);
            documentEnd();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void fragmentStart() {
        xmlWriter.reset();
        // we need a fake root element to hold all incoming XML
        try {
            this.documentStart();
            this.elementStart(ROOT_WRAPPER);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean isEmpty() {
        return Objects.requireNonNull(xmlWriter.resultDoc()).rootElement().directChildren().findAny().isEmpty();
    }

    public @Nullable XmlDocumentFragment resultFragment(XML.XmlSpace xmlSpace) {
        XmlDocument doc = xmlWriter.resultDoc();
        if (doc == null) return null;
        XmlElement rootElement = doc.rootElement();
        return XmlDocumentFragment.of(rootElement, xmlSpace);
    }

}

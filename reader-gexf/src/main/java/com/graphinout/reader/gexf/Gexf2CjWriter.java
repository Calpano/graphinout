package com.graphinout.reader.gexf;

import com.graphinout.base.cj.document.impl.CjDocumentElement;
import com.graphinout.base.cj.document.impl.CjEdgeElement;
import com.graphinout.base.cj.document.impl.CjGraphElement;
import com.graphinout.base.cj.document.impl.CjNodeElement;
import com.graphinout.base.cj.stream.ICjStream;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class Gexf2CjWriter extends DefaultHandler {

    private final ICjStream cjStream;

    public Gexf2CjWriter(ICjStream cjStream) {
        this.cjStream = cjStream;
    }

    @Override
    public void startDocument() throws SAXException {
        cjStream.documentStart(new CjDocumentElement());
    }

    @Override
    public void endDocument() throws SAXException {
        cjStream.documentEnd();
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        if ("gexf".equals(qName)) {
            // root element, do nothing
        } else if ("graph".equals(qName)) {
            cjStream.graphStart(new CjGraphElement());
        } else if ("nodes".equals(qName)) {
            // wrapper, do nothing
        } else if ("node".equals(qName)) {
            CjNodeElement node = new CjNodeElement();
            node.id(attributes.getValue("id"));
            node.setLabel(l -> l.addEntry(le -> le.value(attributes.getValue("label"))));
            cjStream.nodeStart(node);
        } else if ("edges".equals(qName)) {
            // wrapper, do nothing
        } else if ("edge".equals(qName)) {
            CjEdgeElement edge = new CjEdgeElement();
            edge.addEndpoint(e -> e.node(attributes.getValue("source")));
            edge.addEndpoint(e -> e.node(attributes.getValue("target")));
            cjStream.edgeStart(edge);
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if ("graph".equals(qName)) {
            cjStream.graphEnd();
        } else if ("node".equals(qName)) {
            cjStream.nodeEnd();
        } else if ("edge".equals(qName)) {
            cjStream.edgeEnd();
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        // no character data in gexf
    }
}

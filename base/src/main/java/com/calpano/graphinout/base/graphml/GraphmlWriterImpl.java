package com.calpano.graphinout.base.graphml;



import com.calpano.graphinout.base.output.xml.XmlWriter;

import java.io.IOException;
import java.util.LinkedHashMap;

import static com.calpano.graphinout.base.graphml.GraphmlDocument.*;

public class GraphmlWriterImpl implements GraphmlWriter {

    final XmlWriter xmlWriter;

    public GraphmlWriterImpl(XmlWriter xmlWriter) {
        this.xmlWriter = xmlWriter;
    }

    @Override
    public void startDocument(GraphmlDocument doc) throws IOException {
        xmlWriter.startDocument();
        LinkedHashMap<String, String> attributes = new LinkedHashMap<>();
        attributes.put("xmlns", HEADER_XMLNS);
        attributes.put("xmlns:xsi", HEADER_XMLNS_XSI);
        attributes.put("xsi:schemaLocation", HEADER_XMLNS_XSI_SCHEMA_LOCATIOM);

        xmlWriter.startElement(GraphmlDocument.TAGNAME, attributes );
        if(doc.desc!=null) {
            doc.desc.writeXml(xmlWriter);
        }


    }

    @Override
    public void endDocument() throws IOException {
        xmlWriter.endElement(GraphmlDocument.TAGNAME );
    }

    @Override
    public void data(GraphmlKey data) throws IOException {

    }

    @Override
    public void startGraph() {

    }

    @Override
    public void endGraph() {

    }

    @Override
    public void startNode(GraphmlNode node) {

    }
}

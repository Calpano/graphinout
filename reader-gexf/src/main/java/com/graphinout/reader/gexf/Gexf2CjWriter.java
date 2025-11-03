package com.graphinout.reader.gexf;

import com.graphinout.base.cj.document.CjDirection;
import com.graphinout.base.cj.document.ICjGraphMutable;
import com.graphinout.base.cj.document.impl.CjDocumentElement;
import com.graphinout.base.cj.stream.ICjStream;
import com.graphinout.base.cj.writer.CjWriter2CjStream;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.Stack;

public class Gexf2CjWriter extends DefaultHandler {

    private final ICjStream cjStream;
    private final CjDocumentElement cjDocument;
    private ICjGraphMutable currentGraph;
    private boolean inNodes = false;
    private boolean inEdges = false;
    private Stack<String> elementStack = new Stack<>();

    public Gexf2CjWriter(ICjStream cjStream) {
        this.cjStream = cjStream;
        this.cjDocument = new CjDocumentElement();
    }

    @Override
    public void startDocument() throws SAXException {
        CjWriter2CjStream cjWriter2CjStream = new CjWriter2CjStream(cjStream);
        cjWriter2CjStream.documentStart();
    }

    @Override
    public void endDocument() throws SAXException {
        CjWriter2CjStream cjWriter2CjStream = new CjWriter2CjStream(cjStream);
        cjDocument.fire(cjWriter2CjStream);
        cjWriter2CjStream.documentEnd();
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        elementStack.push(qName);
        if ("graph".equals(qName)) {
            cjDocument.addGraph(g -> {
                currentGraph = g;
                String defaultedgetype = attributes.getValue("defaultedgetype");
                if ("directed".equalsIgnoreCase(defaultedgetype)) {
                    currentGraph.dataMutable(d -> d.addProperty("dot.type", "digraph"));
                } else {
                    currentGraph.dataMutable(d -> d.addProperty("dot.type", "graph"));
                }
            });
        } else if ("nodes".equals(qName)) {
            inNodes = true;
        } else if ("node".equals(qName) && inNodes) {
            String id = attributes.getValue("id");
            String label = attributes.getValue("label");
            currentGraph.addNode(n -> {
                n.id(id);
                if (label != null) {
                    n.setLabel(l -> l.addEntry(le -> le.value(label)));
                }
            });
        } else if ("edges".equals(qName)) {
            inEdges = true;
        } else if ("edge".equals(qName) && inEdges) {
            String source = attributes.getValue("source");
            String target = attributes.getValue("target");
            currentGraph.addEdge(e -> {
                e.addEndpoint(ep -> ep.node(source).direction(CjDirection.UNDIR));
                e.addEndpoint(ep -> ep.node(target).direction(CjDirection.UNDIR));
            });
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        elementStack.pop();
        if ("nodes".equals(qName)) {
            inNodes = false;
        } else if ("edges".equals(qName)) {
            inEdges = false;
        } else if ("graph".equals(qName)) {
            currentGraph = null;
        }
    }
}

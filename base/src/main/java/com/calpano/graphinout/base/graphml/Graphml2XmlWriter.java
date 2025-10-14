package com.calpano.graphinout.base.graphml;


import com.calpano.graphinout.foundation.xml.XML;
import com.calpano.graphinout.foundation.xml.XmlFragmentString;
import com.calpano.graphinout.foundation.xml.XmlWriter;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.Stack;

import static com.calpano.graphinout.foundation.util.Nullables.ifPresentAccept;
import static com.calpano.graphinout.foundation.util.Nullables.ifPresentAcceptThrowing;
import static org.slf4j.LoggerFactory.getLogger;

public class Graphml2XmlWriter implements GraphmlWriter {

    private static final Logger log = getLogger(Graphml2XmlWriter.class);

    final XmlWriter xmlWriter;
    private final Stack<IGraphmlGraph> graphStack = new Stack<>();

    public Graphml2XmlWriter(XmlWriter xmlWriter) {
        this.xmlWriter = xmlWriter;
    }

    @Override
    public void data(IGraphmlData data) throws IOException {
        xmlWriter.lineBreak();
        log.trace("writerData [{}]", data);
        XmlFragmentString xmlValue = data.xmlValue();
        Map<String, String> atts = data.xmlPlusGraphmlAttributesNormalized();
        if (xmlValue != null) {
            if (xmlValue.xmlSpace() == XML.XmlSpace.preserve) {
                atts.putIfAbsent(XML.XML_SPACE, xmlValue.xmlSpace().xmlAttValue);
            }
        }
        xmlWriter.elementStart(Graphml.xmlNameOf(GraphmlElements.DATA), data.xmlPlusGraphmlAttributesNormalized());
        if (xmlValue != null) {
            xmlWriter.raw(xmlValue.rawXml());
        }
        xmlWriter.elementEnd(Graphml.xmlNameOf(GraphmlElements.DATA));
    }

    @Override
    public void documentEnd() throws IOException {
        log.trace("endDocument");
        xmlWriter.elementEnd(Graphml.xmlNameOf(GraphmlElements.GRAPHML));
        xmlWriter.documentEnd();
    }

    @Override
    public void documentStart(IGraphmlDocument doc) throws IOException {
        assert doc != null;
        log.trace("startDocument [{}]", doc);
        xmlWriter.documentStart();
        xmlWriter.elementStart(Graphml.xmlNameOf(GraphmlElements.GRAPHML), doc.xmlPlusGraphmlAttributesNormalized());
        ifPresentAcceptThrowing(doc.desc(), desc -> desc.writeXml(xmlWriter));
        xmlWriter.lineBreak();
    }

    @Override
    public void edgeEnd() throws IOException {
        xmlWriter.elementEnd(Graphml.xmlNameOf(GraphmlElements.EDGE));
    }

    @Override
    public void edgeStart(IGraphmlEdge edge) throws IOException {
        boolean isDirectedInGraph = IGraphmlGraph.EdgeDefault.isDirected(graphStack.peek().edgeDefault());
        xmlWriter.lineBreak();
        Map<String, String> allAttributesNormalized = edge.xmlPlusGraphmlAttributesNormalized();
        if (edge.directed() != null) {
            boolean isDirectedInEdge = Objects.requireNonNull(edge.directed());
            if (isDirectedInGraph == isDirectedInEdge) {
                // attribute not needed
                allAttributesNormalized.remove(IGraphmlEdge.ATTRIBUTE_DIRECTED);
            }
        }
        xmlWriter.elementStart(Graphml.xmlNameOf(GraphmlElements.EDGE), allAttributesNormalized);
    }

    @Override
    public void graphEnd() throws IOException {
        log.trace("endGraph");
        xmlWriter.elementEnd(Graphml.xmlNameOf(GraphmlElements.GRAPH));
        assert !graphStack.isEmpty() : "'graph' start missing";
        graphStack.pop();
    }

    @Override
    public void graphStart(IGraphmlGraph graphmlGraph) throws IOException {
        xmlWriter.lineBreak();
        log.trace("startGraph [{}]", graphmlGraph);
        graphStack.push(graphmlGraph);
        xmlWriter.elementStart(Graphml.xmlNameOf(GraphmlElements.GRAPH), graphmlGraph.xmlPlusGraphmlAttributesNormalized());
        ifPresentAcceptThrowing(graphmlGraph.desc(), desc -> desc.writeXml(xmlWriter));
        IGraphmlLocator locator = graphmlGraph.locator();
        if (locator != null) {
            xmlWriter.elementStart(Graphml.xmlNameOf(GraphmlElements.LOCATOR), locator.xmlPlusGraphmlAttributesNormalized());
            xmlWriter.elementEnd(Graphml.xmlNameOf(GraphmlElements.LOCATOR));
        }
    }

    @Override
    public void hyperEdgeEnd() throws IOException {
        xmlWriter.elementEnd(Graphml.xmlNameOf(GraphmlElements.HYPER_EDGE));
    }

    @Override
    public void hyperEdgeStart(IGraphmlHyperEdge edge) throws IOException {
        xmlWriter.elementStart(Graphml.xmlNameOf(GraphmlElements.HYPER_EDGE), edge.xmlPlusGraphmlAttributesNormalized());
        ifPresentAcceptThrowing(edge.desc(), desc -> desc.writeXml(xmlWriter));
        if (edge.endpoints() != null) {
            for (IGraphmlEndpoint endpoint : edge.endpoints()) {
                xmlWriter.elementStart(Graphml.xmlNameOf(GraphmlElements.ENDPOINT), endpoint.xmlPlusGraphmlAttributesNormalized());
                xmlWriter.elementEnd(Graphml.xmlNameOf(GraphmlElements.ENDPOINT));
            }
        }
    }

    @Override
    public void key(IGraphmlKey graphmlKey) throws IOException {
        log.trace("key [{}]", graphmlKey);
        xmlWriter.lineBreak();
        Map<String, String> attributes = graphmlKey.xmlPlusGraphmlAttributesNormalized();
        if (attributes.get(IGraphmlKey.ATTRIBUTE_FOR) != null && attributes.get(IGraphmlKey.ATTRIBUTE_FOR).equals("all"))
            attributes.remove(IGraphmlKey.ATTRIBUTE_FOR);
        xmlWriter.elementStart(Graphml.xmlNameOf(GraphmlElements.KEY), attributes);
        ifPresentAccept(graphmlKey.desc(), desc -> {
            try {
                desc.writeXml(xmlWriter);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        ifPresentAccept(graphmlKey.defaultValue(), defaultValue -> {
            try {
                Map<String, String> attsMap = defaultValue.xmlPlusGraphmlAttributesNormalized();
                if (defaultValue.xmlValue().xmlSpace() == XML.XmlSpace.preserve) {
                    attsMap.putIfAbsent(XML.XML_SPACE, defaultValue.xmlValue().xmlSpace().xmlAttValue);
                }
                xmlWriter.elementStart(Graphml.xmlNameOf(GraphmlElements.DEFAULT), attsMap);
                xmlWriter.raw(defaultValue.xmlValue().rawXml());
                xmlWriter.elementEnd(Graphml.xmlNameOf(GraphmlElements.DEFAULT));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        xmlWriter.elementEnd(Graphml.xmlNameOf(GraphmlElements.KEY));
    }

    @Override
    public void nodeEnd() throws IOException {
        log.trace("endNode");
        xmlWriter.elementEnd(Graphml.xmlNameOf(GraphmlElements.NODE));
    }

    @Override
    public void nodeStart(IGraphmlNode node) throws IOException {
        xmlWriter.lineBreak();
        log.trace("startNode [{}]", node);
        xmlWriter.elementStart(
                Graphml.xmlNameOf(GraphmlElements.NODE)
                , node.xmlPlusGraphmlAttributesNormalized());
        ifPresentAcceptThrowing(node.desc(), desc -> desc.writeXml(xmlWriter));
        IGraphmlLocator locator = node.locator();
        if (locator != null) {
            xmlWriter.elementStart(Graphml.xmlNameOf(GraphmlElements.LOCATOR), locator.xmlPlusGraphmlAttributesNormalized());
            xmlWriter.elementEnd(Graphml.xmlNameOf(GraphmlElements.LOCATOR));
        }
    }

    @Override
    public void portEnd() throws IOException {
        log.debug("endPort .");
        xmlWriter.elementEnd(Graphml.xmlNameOf(GraphmlElements.PORT));
    }

    @Override
    public void portStart(IGraphmlPort port) throws IOException {
        log.debug("startPort [{}]", port);
        xmlWriter.elementStart(Graphml.xmlNameOf(GraphmlElements.PORT), port.xmlPlusGraphmlAttributesNormalized());
    }

}

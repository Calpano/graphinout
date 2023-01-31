package com.calpano.graphinout.base.output.xml;

import com.calpano.graphinout.base.output.GraphMlWriter;
import com.calpano.graphinout.base.output.OutputSink;
import com.calpano.graphinout.base.output.xml.file.XMLFileWriter;

public class GioGraphInOutXMLConstants {

    public static final String START_LOG = "#####--------start-------";
    public static final String END_LOG = "--------End-------#####";
    public static final String EDGE_ELEMENT_NAME = "edge";
    public static final String ENDPOINT_ELEMENT_NAME = "endpoint";
    public static final String EDGE_DATA_ELEMENT_NAME = "data";
    public static final String GRAPH_ELEMENT_NAME = "graph";
    public static final String GRAPHML_ELEMENT_NAME = "graphml";
    public static final String HYPER_EDGE_ELEMENT_NAME = "hyperedge";
    public static final String KEY_ELEMENT_NAME = "key";
    public static final String NODE_ELEMENT_NAME = "node";
    public static final String NODE_DATA_ELEMENT_NAME = "data";
    public static final String PORT_ELEMENT_NAME = "port";
    public static final String DESC_ELEMENT_NAME = "desc";
    public static final String DEFAULT_ELEMENT_NAME = "Default";

    public static GraphMlWriter ofXML(XmlWriter xmlWriter) {
        return new GraphMlXmlWriter(xmlWriter);
    }

    public static XmlWriter ofFile(OutputSink outputSink) {
        return new XMLFileWriter(outputSink);
    }

    public static GraphMlWriter ofXML(OutputSink outputSink) {
        return ofXML(ofFile(outputSink));
    }
}
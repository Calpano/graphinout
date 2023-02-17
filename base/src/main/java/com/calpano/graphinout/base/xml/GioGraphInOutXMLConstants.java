package com.calpano.graphinout.base.xml;

import com.calpano.graphinout.base.graphml.GraphmlWriter;
import com.calpano.graphinout.base.graphml.GraphmlWriterImpl;
import com.calpano.graphinout.base.output.OutputSink;

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

    public static GraphmlWriter of(XmlWriter xmlWriter) {
        return new GraphmlWriterImpl(xmlWriter);
    }

    public static GraphmlWriter of(OutputSink outputSink) {
        return of(new XmlWriterImpl(outputSink));
    }
}
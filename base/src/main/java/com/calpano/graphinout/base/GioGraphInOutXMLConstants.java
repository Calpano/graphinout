package com.calpano.graphinout.base;

import com.calpano.graphinout.base.graphml.GraphmlWriter;
import com.calpano.graphinout.base.graphml.GraphmlWriterImpl;
import com.calpano.graphinout.foundation.output.OutputSink;
import com.calpano.graphinout.foundation.xml.XmlWriter;
import com.calpano.graphinout.foundation.xml.XmlWriterImpl;

public class GioGraphInOutXMLConstants {

    public static final String START_LOG = "#####--------start-------";
    public static final String END_LOG = "--------End-------#####";
    public static final String EDGE = "edge";
    public static final String ENDPOINT = "endpoint";
    public static final String EDGE_DATA = "data";
    public static final String GRAPH = "graph";
    public static final String GRAPHML = "graphml";
    public static final String HYPER_EDGE = "hyperedge";
    public static final String KEY = "key";
    public static final String NODE = "node";
    public static final String NODE_DATA = "data";
    public static final String PORT = "port";
    public static final String DESC = "desc";
    public static final String DEFAULT = "Default";

    public static GraphmlWriter of(XmlWriter xmlWriter) {
        return new GraphmlWriterImpl(xmlWriter);
    }

    public static GraphmlWriter of(OutputSink outputSink) {
        return of(new XmlWriterImpl(outputSink));
    }
}

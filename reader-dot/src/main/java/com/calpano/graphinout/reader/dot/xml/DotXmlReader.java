package com.calpano.graphinout.reader.dot.xml;

import com.calpano.graphinout.base.gio.GioWriter;
import com.calpano.graphinout.base.input.InputSource;
import com.calpano.graphinout.base.reader.GioFileFormat;
import com.calpano.graphinout.base.reader.GioReader;

import javax.xml.stream.*;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * This class converts a DOT file to a GraphML file using streams
 */
public class DotXmlReader implements GioReader {

    public static final String DIGRAPH = "digraph";
    public static final String GRAPH = "graph";
    public static final String ID = "id";
    public static final String EDGEDEFAULT = "edgedefault";
    public static final String DIRECTED = "directed";
    public static final String NODE = "node";
    public static final String EDGE = "edge";
    public static final String SOURCE = "source";
    public static final String TARGET = "target";
    public static final String LABEL = "label";
    public static final String DATA = "data";
    public static final String KEY = "key";
    public static final String TAILPORT = "tailport";
    public static final String HEADPORT = "headport";

    @Override
    public GioFileFormat fileFormat() {
        return new GioFileFormat("dot-text", "DOT/GraphViz Format, text");
    }

    @Override
    public void read(InputSource inputSource, GioWriter writer) throws IOException {
        // TODO integrate here
    }

    /**
     * The main method converts the DOT file to GraphML file
     * and writes the result to the specified output file.
     *
     * @param inputFilePath  the DOT file source
     * @param outputFilePath the created GraphML file source
     * @throws Exception if an error occurs during the reading or writing
     */
    public void readDotFiles(final String inputFilePath, final String outputFilePath) throws Exception {

        FileInputStream input = new FileInputStream(inputFilePath);
        FileOutputStream output = new FileOutputStream(outputFilePath);
        XMLInputFactory inputFactory = XMLInputFactory.newInstance();
        XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
        XMLStreamReader reader = inputFactory.createXMLStreamReader(input);
        XMLStreamWriter writer = outputFactory.createXMLStreamWriter(output);

        graphmlHeaderWriter(writer);

        dotToGraphml(reader, writer);

        closeEverything(input, output, reader, writer);
    }

    private static void dotToGraphml(XMLStreamReader reader, XMLStreamWriter writer) throws XMLStreamException {
        while (reader.hasNext()) {
            int event = reader.next();
            switch (event) {
                case XMLStreamConstants.START_ELEMENT -> {
                    readGraphElement(reader, writer);
                    readNodes(reader, writer);
                    readEdges(reader, writer);
                }
                case XMLStreamConstants.END_ELEMENT -> endElementWriter(reader, writer);
            }
        }
    }

    private static void graphmlHeaderWriter(XMLStreamWriter writer) throws XMLStreamException {
        writer.writeStartDocument();
        writer.writeStartElement("graphml");
        writer.writeAttribute("xmlns", "http://graphml.graphdrawing.org/xmlns");
        writer.writeAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
        writer.writeAttribute("xsi:schemaLocation", "http://graphml.graphdrawing.org/xmlns http://graphml.graphdrawing.org/xmlns/1.0/graphml.xsd");
    }

    private static void readGraphElement(XMLStreamReader reader, XMLStreamWriter writer) throws XMLStreamException {
        String edgeDefault;
        String graphId;
        if (reader.getLocalName().equals(DIGRAPH)) {
            graphId = reader.getAttributeValue(null, ID);
            edgeDefault = reader.getAttributeValue(null, EDGEDEFAULT);
            if (edgeDefault == null) {
                edgeDefault = DIRECTED;
            }
            writer.writeStartElement(GRAPH);
            writer.writeAttribute(ID, graphId);
            writer.writeAttribute(EDGEDEFAULT, edgeDefault);
        }
    }

    private static void readNodes(XMLStreamReader reader, XMLStreamWriter writer) throws XMLStreamException {
        String id;
        if (reader.getLocalName().equals(NODE)) {
            id = reader.getAttributeValue(null, ID);
            writer.writeStartElement(NODE);
            writer.writeAttribute(ID, id);
            writer.writeStartElement(DATA);
            writer.writeAttribute(KEY, LABEL);
        }
    }

    private static void readEdges(XMLStreamReader reader, XMLStreamWriter writer) throws XMLStreamException {
        if (reader.getLocalName().equals(EDGE)) {
            String source = reader.getAttributeValue(null, SOURCE);
            String target = reader.getAttributeValue(null, TARGET);
            String label = reader.getAttributeValue(null, LABEL);
            String tailPort = reader.getAttributeValue(null, TAILPORT);
            String headPort = reader.getAttributeValue(null, HEADPORT);

            writer.writeStartElement(EDGE);
            writer.writeAttribute(SOURCE, source);
            writer.writeAttribute(TARGET, target);
            if (label != null) {
                writer.writeAttribute(LABEL, label);
            }
            if (tailPort != null) {
                writer.writeAttribute(TAILPORT, tailPort);
            }
            if (headPort != null) {
                writer.writeAttribute(HEADPORT, headPort);
            }
            writer.writeStartElement(DATA);
            writer.writeAttribute(KEY, LABEL);
            writer.writeCharacters(label);
        }
    }

    private static void endElementWriter(XMLStreamReader reader, XMLStreamWriter writer) throws XMLStreamException {
        if (reader.getLocalName().equals(DIGRAPH)) {
            writer.writeEndElement();
        }
        if (reader.getLocalName().equals(NODE)) {
            writer.writeEndElement();
        }
        if (reader.getLocalName().equals(EDGE)) {
            writer.writeEndElement();
        }
        writer.writeEndDocument();
    }

    private static void closeEverything(FileInputStream input, FileOutputStream output, XMLStreamReader reader, XMLStreamWriter writer) throws IOException, XMLStreamException {
        input.close();
        output.close();
        reader.close();
        writer.close();
    }
}
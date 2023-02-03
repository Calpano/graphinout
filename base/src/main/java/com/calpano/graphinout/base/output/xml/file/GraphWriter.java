package com.calpano.graphinout.base.output.xml.file;

import com.calpano.graphinout.base.output.OutputSink;
import com.calpano.graphinout.base.output.xml.GioGraphInOutXMLConstants;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Map;

@Slf4j
class GraphWriter extends ChainXmlWriter {

    private ElementWriter nodeHandler;
    private ElementWriter edgeHandler;


    public GraphWriter(OutputSink outputSink) {
        super(outputSink);
        nodeHandler = new ElementWriter(outputSink);
        edgeHandler = new ElementWriter(new InMemoryOutputSink());


    }

    @Override
    public void startElement(String name, Map<String, String> attributes) throws IOException {
        log.debug("startElement {} {}.", name, attributes);

        if (nextWriter != null) {
            nextWriter.startElement(name, attributes);
        } else if (GioGraphInOutXMLConstants.NODE_ELEMENT_NAME.equals(name)) {
            nextWriter = nodeHandler;
            nextWriter.startDocument();
            nextWriter.startElement(name, attributes);
        } else if (GioGraphInOutXMLConstants.EDGE_ELEMENT_NAME.equals(name) || GioGraphInOutXMLConstants.HYPER_EDGE_ELEMENT_NAME.equals(name)) {
            nextWriter = edgeHandler;
            nextWriter.startDocument();
            nextWriter.startElement(name, attributes);
        } else makeStartElement(name, attributes, writer);
        elementPush(name);
    }


    @Override
    public void endElement(String name) throws IOException {
        log.debug("endElement {}.", name);
        if (!elementPop().equals(name)) {
            //to
            // throw new GioException(GioExceptionMessage.temporary_exemption);
        }
        if (nextWriter != null) {
            nextWriter.endElement(name);
            if (nextWriter.elementEmpty()) {
                nextWriter = null;
            }
        } else {
            for (String line : edgeHandler.readAllElements())
                characterData(line);
            makeEndElement(name, writer);

        }
    }
}

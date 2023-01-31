package com.calpano.graphinout.base.output.xml.file;

import com.calpano.graphinout.base.output.OutputSink;
import com.calpano.graphinout.base.output.xml.GioGraphInOutXMLConstants;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Map;

@Slf4j
class ElementWriter extends ChainXmlWriter {

    public ElementWriter(OutputSink outputSink) {
        super(outputSink);
    }

    @Override
    public void startElement(String name, Map<String, String> attributes) throws IOException {
        log.debug("startElement [{}] with attribute [{}].", name, attributes.toString());
        if(nextWriter !=null){
            nextWriter.startElement(name,attributes);
        } else if (GioGraphInOutXMLConstants.GRAPH_ELEMENT_NAME.equals(name)) {
            nextWriter =  new GraphWriter(outputSink, new TempOutputSink("graph"));
            nextWriter.startDocument();
            nextWriter.startElement(name,attributes);
        }else {
            makeStartElement(name, attributes, writer);
        }
        elementPush(name);
    }

}

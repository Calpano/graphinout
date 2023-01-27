package com.calpano.graphinout.base.output.xml.file;

import com.calpano.graphinout.base.output.OutputSink;
import com.calpano.graphinout.base.output.xml.XmlWriter;

import java.io.IOException;
import java.util.Map;

public class XMLFileWriter implements XmlWriter {

    private final XmlWriter xmlWriter ;

    public XMLFileWriter(OutputSink outputSink) {
        this.xmlWriter =new ElementWriter(outputSink);
    }

    @Override
    public void characterData(String characterData) throws IOException {
        this.xmlWriter.characterData(characterData);
    }

    @Override
    public void endDocument() throws IOException {
        this.xmlWriter.endDocument();
    }

    @Override
    public void endElement(String name) throws IOException {
        this.xmlWriter.endElement(name);
    }

    @Override
    public void startDocument() throws IOException {
        this.xmlWriter.startDocument();
    }

    @Override
    public void startElement(String name, Map<String, String> attributes) throws IOException {
        this.xmlWriter.startElement(name,attributes);
    }
}

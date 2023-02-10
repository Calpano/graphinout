package com.calpano.graphinout.base.output.xml.file;

import com.calpano.graphinout.base.output.OutputSink;
import com.calpano.graphinout.base.output.xml.XmlWriter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Just writes out, no validation, no chaining.
 */
@Slf4j
public class SimpleXmlWriter implements XmlWriter {
    protected final OutputSink outputSink;
    protected transient OutputStream out;
    protected transient Writer writer;

    public SimpleXmlWriter(OutputSink outputSink) {
        this.outputSink = outputSink;
    }

    @Override
    public void characterData(String characterData) throws IOException {
        log.debug("characterData [{}].", characterData);
        writer.write(characterData);
    }

    @Override
    public void endDocument() throws IOException {
        log.debug("endDocument.");
        this.writer.flush();
        this.out.flush();
        this.writer.close();
        this.out.close();
    }

    @Override
    public void endElement(String name) throws IOException {
        log.debug("endElement [{}].", name);
        writer.write("</");
        writer.write(name);
        writer.write(">");
    }

    @Override
    public void startDocument() throws IOException {
        this.out = outputSink.outputStream();
        this.writer = new OutputStreamWriter(out, StandardCharsets.UTF_8);
    }

    @Override
    public void startElement(String name, Map<String, String> attributes) throws IOException {
        writer.write("<");
        writer.write(name);
        for (Map.Entry<String, String> entry : attributes.entrySet()) {
            writer.write(" " + entry.getKey() + "=\"" + entry.getValue() + "\"");
        }
        writer.write('>');
        writer.write(System.lineSeparator());
        writer.flush();
    }

}

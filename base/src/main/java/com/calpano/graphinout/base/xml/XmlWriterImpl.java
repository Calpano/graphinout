package com.calpano.graphinout.base.xml;

import com.calpano.graphinout.base.output.OutputSink;
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
public class XmlWriterImpl implements XmlWriter {
    protected final OutputSink outputSink;
    protected transient OutputStream out;
    protected transient Writer writer;

    public XmlWriterImpl(OutputSink outputSink) {
        this.outputSink = outputSink;
    }

    @Override
    public void characterData(String characterData) throws IOException {
        log.trace("characterData [{}]", characterData);
        writer.write(characterData);
    }

    @Override
    public void endDocument() throws IOException {
        log.trace("endDocument");
        this.writer.flush();
        this.out.flush();
        this.writer.close();
        this.out.close();
    }

    @Override
    public void endElement(String name) throws IOException {
        log.trace("endElement [{}]", name);
        writer.write("</");
        writer.write(name);
        writer.write(">");
    }

    @Override
    public void startDocument() throws IOException {
        log.trace("startDocument");
        this.out = outputSink.outputStream();
        this.writer = new OutputStreamWriter(out, StandardCharsets.UTF_8);
    }

    @Override
    public void startElement(String name, Map<String, String> attributes) throws IOException {
        log.trace("startElement [{}]", name);
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

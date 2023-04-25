package com.calpano.graphinout.base.xml;

import com.calpano.graphinout.base.output.OutputSink;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Nullable;
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
        writeBufferMaybe();
        log.trace("characterData [{}]", characterData);
        writer.write(characterData);
    }

    @Override
    public void endDocument() throws IOException {
        writeBufferMaybe();
        log.trace("endDocument");
        writer.write("\n");
        this.writer.flush();
        this.out.flush();
        this.writer.close();
        this.out.close();
    }

    @Override
    public void endElement(String name) throws IOException {
        log.trace("endElement [{}]", name);
        if(this.name!=null) {
            writer.write("<");
            writer.write(name);
            for (Map.Entry<String, String> entry : attributes.entrySet()) {
                writer.write(" " + entry.getKey() + "=\"" + entry.getValue() + "\"");
            }
            writer.write("/>");
            this.name = null;
            this.attributes = null;
        } else {
            writer.write("</");
            writer.write(name);
            writer.write(">");
        }
    }

    @Override
    public void startDocument() throws IOException {
        log.trace("startDocument");
        this.out = outputSink.outputStream();
        this.writer = new OutputStreamWriter(out, StandardCharsets.UTF_8);
    }

    private @Nullable String name = null;
    private @Nullable Map<String,String> attributes = null;


    @Override
    public void startElement(String name, Map<String, String> attributes) throws IOException {
        log.trace("startElement [{}]", name);
        writeBufferMaybe();
        buffer(name, attributes);
    }

    private void buffer(String name, Map<String, String> attributes) {
        this.name=name;
        this.attributes = attributes;
    }

    private void writeBufferMaybe() throws IOException {
        if(this.name != null) {
            writer.write("<");
            writer.write(name);
            for (Map.Entry<String, String> entry : attributes.entrySet()) {
                writer.write(" " + entry.getKey() + "=\"" + entry.getValue() + "\"");
            }
            writer.write('>');
            writer.flush();
        }
        this.name= null;
        this.attributes = null;
    }

    @Override
    public void lineBreak() throws IOException {
        writeBufferMaybe();
        writer.write("\n");
    }

}

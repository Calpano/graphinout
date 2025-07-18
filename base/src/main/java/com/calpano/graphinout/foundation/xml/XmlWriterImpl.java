package com.calpano.graphinout.foundation.xml;

import com.calpano.graphinout.foundation.output.OutputSink;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
public class XmlWriterImpl implements XmlWriter {

    private static final Logger log = LoggerFactory.getLogger(XmlWriterImpl.class);
    protected final OutputSink outputSink;
    protected transient OutputStream out;
    protected transient Writer writer;
    private @Nullable String name = null;
    private @Nullable Map<String, String> attributes = null;
    private boolean hasContent = false;
    private java.util.Stack<String> openElements = new java.util.Stack<>();

    public XmlWriterImpl(OutputSink outputSink) throws IOException {
        this.outputSink = outputSink;
        this.out = outputSink.outputStream();
        this.writer = new OutputStreamWriter(out, StandardCharsets.UTF_8);
    }

    @Override
    public void characterData(String characterData) throws IOException {
        writeBufferMaybe();
        hasContent = true;
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
        if (this.name != null) {
            // No content was written, so self-close
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
        if (!openElements.isEmpty()) openElements.pop();
        hasContent = !openElements.isEmpty();
    }

    @Override
    public void lineBreak() throws IOException {
        writeBufferMaybe();
        writer.write("\n");
    }

    @Override
    public void startDocument() throws IOException {
        log.trace("startDocument");
        this.out = outputSink.outputStream();
        this.writer = new OutputStreamWriter(out, StandardCharsets.UTF_8);
    }

    @Override
    public void startElement(String name, Map<String, String> attributes) throws IOException {
        writeBufferMaybe();
        buffer(name, attributes);
        hasContent = false;
        openElements.push(name);
        log.trace("startElement [{}]", name);
    }

    @Override
    public void startCDATA() throws IOException {
        writeBufferMaybe();
        hasContent = true;
        writer.write("<![CDATA[");
    }

    @Override
    public void endCDATA() throws IOException {
        writer.write("]]>" );
    }

    private void buffer(String name, Map<String, String> attributes) {
        this.name = name;
        this.attributes = attributes;
    }

    private void writeBufferMaybe() throws IOException {
        if (this.name != null) {
            writer.write("<");
            writer.write(name);
            for (Map.Entry<String, String> entry : attributes.entrySet()) {
                writer.write(" " + entry.getKey() + "=\"" + entry.getValue() + "\"");
            }
            writer.write('>');
            writer.flush();
            this.name = null;
            this.attributes = null;
        }
    }

}

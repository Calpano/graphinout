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
    private java.util.Stack<String> openElements = new java.util.Stack<>();
    private boolean insideCDATA = false;  // Add this field

    public XmlWriterImpl(OutputSink outputSink) throws IOException {
        this.outputSink = outputSink;
        this.out = outputSink.outputStream();
        this.writer = new OutputStreamWriter(out, StandardCharsets.UTF_8);
    }

    @Override
    public void characterData(String characterData, boolean isInCdata) throws IOException {
        writeBufferMaybe();
        log.trace("characterData [{}]", characterData);
        if (insideCDATA) {
            // Don't encode CDATA content - write it raw
            writer.write(characterData);
        } else {
            // Only encode regular character data
            writer.write(XmlTool.xmlEncode(characterData));
        }
    }

    @Override
    public void endCDATA() throws IOException {
        insideCDATA = false;  // Reset CDATA state
        raw("]]>");
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
    }

    @Override
    public void lineBreak() throws IOException {
        writeBufferMaybe();
        writer.write("\n");
    }

    @Override
    public void raw(String rawXml) throws IOException {
        writeBufferMaybe();
        writer.write(rawXml);
    }

    @Override
    public void startCDATA() throws IOException {
        System.out.println("CDATA in XmlWriterImpl");
        writeBufferMaybe();
        insideCDATA = true;  // Track CDATA state
        raw("<![CDATA[");
    }

    @Override
    public void startDocument() throws IOException {
        log.trace("startDocument");
        this.out = outputSink.outputStream();
        this.writer = new OutputStreamWriter(out, StandardCharsets.UTF_8);
        writer.write("<?xml version='1.0' encoding='utf-8'?>\n");
        writer.flush();
    }

    @Override
    public void startElement(String name, Map<String, String> attributes) throws IOException {
        writeBufferMaybe();
        buffer(name, attributes);
        openElements.push(name);
        log.trace("startElement [{}]", name);
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

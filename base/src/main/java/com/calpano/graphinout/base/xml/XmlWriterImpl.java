package com.calpano.graphinout.base.xml;

import com.calpano.graphinout.base.gio.GioGraphInOutConstants;
import com.calpano.graphinout.base.output.OutputSink;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class XmlWriterImpl implements XmlWriter {

    public XmlWriterImpl(OutputSink outputSink) {
        this.outputSink = outputSink;
    }

    final OutputSink outputSink;
    private transient OutputStream out;
    private transient Writer w;

    @Override
    public void endDocument() throws IOException {
        this.w.close();
        this.out.close();
    }

    @Override
    public void endElement(String name) {
        // TODO ...
    }

    @Override
    public void startDocument() throws IOException {
        this.out = outputSink.outputStream();
        this.w = new OutputStreamWriter(out, StandardCharsets.UTF_8);
    }

    @Override
    public void startElement(String name, Map<String, String> attributes) throws IOException {
        makeStartElement(name, attributes, w);
    }

    public static void makeStartElement(String name, Map<String, String> attributes, Appendable a) throws IOException {
        a.append('<');
        a.append(name);

        for( Map.Entry<String, String> entry : attributes.entrySet()) {
            a.append(" "+entry.getKey()+"=\""+entry.getValue()+"\"");
        }
        a.append(GioGraphInOutConstants.NEW_LINE_SEPARATOR);
    }

}

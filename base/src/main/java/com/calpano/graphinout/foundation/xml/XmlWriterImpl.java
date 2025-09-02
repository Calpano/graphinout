package com.calpano.graphinout.foundation.xml;

import com.calpano.graphinout.foundation.output.OutputSink;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;

/**
 * Just writes out, no validation, no chaining.
 */
public class XmlWriterImpl extends Xml2AppendableWriter implements AutoCloseable {

    private static final Logger log = LoggerFactory.getLogger(XmlWriterImpl.class);
    protected final OutputSink outputSink;
    protected transient OutputStream out;
    protected transient Writer writer;

    public XmlWriterImpl(OutputSink outputSink, OutputStream out, Writer writer) throws IOException {
        super(writer);
        this.outputSink = outputSink;
        this.out = out;
        this.writer = writer;
    }

    public static XmlWriterImpl create(OutputSink outputSink) throws IOException {
        OutputStream out = outputSink.outputStream();
        Writer writer = new OutputStreamWriter(out, StandardCharsets.UTF_8);
        return new XmlWriterImpl(outputSink, out, writer);
    }

    @Override
    public void close() throws Exception {
        writer.flush();
        writer.close();
        out.flush();
        out.close();
        outputSink.close();
    }

    @Override
    public void documentStart() throws IOException {
        super.documentStart();
        writer.flush();
    }

}

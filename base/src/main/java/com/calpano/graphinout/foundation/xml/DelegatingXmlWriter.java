package com.calpano.graphinout.foundation.xml;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Simple XmlWriter implementation that writes to a StringWriter
 */
public class DelegatingXmlWriter implements XmlWriter {

    @FunctionalInterface
    interface XmlWriterConsumer extends Consumer<XmlWriter> {

        default void accept(XmlWriter writer) {
            try {
                acceptThrowable(writer);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        void acceptThrowable(XmlWriter writer) throws IOException;

    }

    private final List<XmlWriter> writers;

    public DelegatingXmlWriter(XmlWriter... writers) {this.writers = List.of(writers);}

    @Override
    public void characterData(String characterData, boolean isInCdata) throws IOException {
        forEachWriter(w -> w.characterData(characterData, isInCdata));
    }

    @Override
    public void endCDATA() throws IOException {
        forEachWriter(XmlWriter::endCDATA);
    }

    @Override
    public void endDocument() throws IOException {
        forEachWriter(XmlWriter::endDocument);
    }

    @Override
    public void endElement(String name) throws IOException {
        forEachWriter(w -> w.endElement(name));
    }

    @Override
    public void lineBreak() throws IOException {
        forEachWriter(XmlWriter::lineBreak);
    }

    @Override
    public void raw(String rawXml) {
        forEachWriter(w -> w.raw(rawXml));
    }

    @Override
    public void startCDATA() throws IOException {
        forEachWriter(XmlWriter::startCDATA);
    }

    @Override
    public void startDocument() throws IOException {
        forEachWriter(XmlWriter::startDocument);
    }

    @Override
    public void startElement(String name, Map<String, String> attributes) throws IOException {
        forEachWriter(w -> w.startElement(name, attributes));
    }

    private void forEachWriter(XmlWriterConsumer consumer) {
        writers.forEach(consumer);
    }


}

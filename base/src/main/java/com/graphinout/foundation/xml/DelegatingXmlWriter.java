package com.graphinout.foundation.xml;

import com.graphinout.foundation.util.ThrowingConsumer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Simple XmlWriter implementation that writes to a StringWriter
 */
public class DelegatingXmlWriter implements XmlWriter {

    protected final List<XmlWriter> writers = new ArrayList<>();

    public DelegatingXmlWriter(XmlWriter... writers) {this.writers.addAll(List.of(writers));}

    public void addWriter(XmlWriter writer) {writers.add(writer);}

    public void characters(String characters, CharactersKind kind) {
        forEachWriter(w -> w.characters(characters, kind));
    }

    public void charactersEnd() {
        forEachWriter(XmlWriter::charactersEnd);
    }

    public void charactersStart() {
        forEachWriter(XmlWriter::charactersStart);
    }

    @Override
    public void documentEnd() throws IOException {
        forEachWriter(XmlWriter::documentEnd);
    }

    @Override
    public void documentStart() throws IOException {
        forEachWriter(XmlWriter::documentStart);
    }

    @Override
    public void elementEnd(String uri, String localName, String qName) throws IOException {
        forEachWriter(w -> w.elementEnd(uri, localName, qName));
    }

    @Override
    public void elementStart(String uri, String localName, String qName, Map<String, String> attributes) throws IOException {
        forEachWriter(w -> w.elementStart(uri, localName, qName, attributes));
    }

    @Override
    public void lineBreak() throws IOException {
        forEachWriter(XmlWriter::lineBreak);
    }

    @Override
    public void raw(String rawXml) {
        forEachWriter(w -> w.raw(rawXml));
    }

    private void forEachWriter(ThrowingConsumer<XmlWriter, Exception> consumer) {
        writers.forEach(consumer);
    }

}

package com.graphinout.foundation.xml.writer;

import com.graphinout.foundation.input.ContentError;
import com.graphinout.foundation.util.ThrowingConsumer;
import com.graphinout.foundation.xml.factory.BaseXmlHandler;
import com.graphinout.foundation.xml.CharactersKind;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Simple XmlWriter implementation that writes to a StringWriter
 */
public class DelegatingXmlWriter extends BaseXmlHandler implements XmlWriter {

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
    public Consumer<ContentError> contentErrorHandler() {
        return this::onContentError;
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
    public void lineBreak() {
        forEachWriter(XmlWriter::lineBreak);
    }

    @Override
    public void onContentError(ContentError contentError) {
        forEachWriter(w -> w.onContentError(contentError));
    }

    @Override
    public void raw(String rawXml) {
        forEachWriter(w -> w.raw(rawXml));
    }

    @Override
    public void setContentErrorHandler(Consumer<ContentError> errorHandler) {
        // ignored
    }

    private void forEachWriter(ThrowingConsumer<XmlWriter, Exception> consumer) {
        writers.forEach(consumer);
    }

}

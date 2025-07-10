package com.calpano.graphinout.base.writer;

import com.calpano.graphinout.base.gio.GioData;
import com.calpano.graphinout.base.gio.GioDocument;
import com.calpano.graphinout.base.gio.GioEdge;
import com.calpano.graphinout.base.gio.GioGraph;
import com.calpano.graphinout.base.gio.GioKey;
import com.calpano.graphinout.base.gio.GioNode;
import com.calpano.graphinout.base.gio.GioPort;
import com.calpano.graphinout.base.gio.GioWriter;
import com.calpano.graphinout.foundation.json.JsonException;

import javax.annotation.Nullable;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

/** Delegates one input stream to n output streams */
public class DelegatingGioWriter implements GioWriter {

    private final List<GioWriter> writers;

    public DelegatingGioWriter(GioWriter... writers) {
        this.writers = Arrays.asList(writers);
    }

    @Override
    public void arrayEnd() throws JsonException {
        for (GioWriter writer : writers) {
            writer.arrayEnd();
        }
    }

    @Override
    public void arrayStart() throws JsonException {
        for (GioWriter writer : writers) {
            writer.arrayStart();
        }
    }

    @Override
    public void baseUri(String baseUri) throws IOException {
        for (GioWriter writer : writers) {
            writer.baseUri(baseUri);
        }
    }

    @Override
    public void data(GioData data) throws IOException {
        for (GioWriter writer : writers) {
            writer.data(data);
        }
    }

    @Override
    public void endDocument() throws IOException {
        for (GioWriter writer : writers) {
            writer.endDocument();
        }
    }

    @Override
    public void endEdge() throws IOException {
        for (GioWriter writer : writers) {
            writer.endEdge();
        }
    }

    @Override
    public void endGraph(@Nullable URL locator) throws IOException {
        for (GioWriter writer : writers) {
            writer.endGraph(locator);
        }
    }

    @Override
    public void endNode(@Nullable URL locator) throws IOException {
        for (GioWriter writer : writers) {
            writer.endNode(locator);
        }
    }

    @Override
    public void endPort() throws IOException {
        for (GioWriter writer : writers) {
            writer.endPort();
        }
    }

    @Override
    public void key(GioKey gioKey) throws IOException {
        for (GioWriter writer : writers) {
            writer.key(gioKey);
        }
    }

    @Override
    public void objectEnd() throws JsonException {
        for (GioWriter writer : writers) {
            writer.objectEnd();
        }
    }

    @Override
    public void objectStart() throws JsonException {
        for (GioWriter writer : writers) {
            writer.objectStart();
        }
    }

    @Override
    public void onBigDecimal(BigDecimal bigDecimal) throws JsonException {
        for (GioWriter writer : writers) {
            writer.onBigDecimal(bigDecimal);
        }
    }

    @Override
    public void onBigInteger(BigInteger bigInteger) throws JsonException {
        for (GioWriter writer : writers) {
            writer.onBigInteger(bigInteger);
        }
    }

    @Override
    public void onBoolean(boolean b) throws JsonException {
        for (GioWriter writer : writers) {
            writer.onBoolean(b);
        }
    }

    @Override
    public void onDouble(double d) throws JsonException {
        for (GioWriter writer : writers) {
            writer.onDouble(d);
        }
    }

    @Override
    public void onFloat(float f) throws JsonException {
        for (GioWriter writer : writers) {
            writer.onFloat(f);
        }
    }

    @Override
    public void onInteger(int i) throws JsonException {
        for (GioWriter writer : writers) {
            writer.onInteger(i);
        }
    }

    @Override
    public void onKey(String key) throws JsonException {
        for (GioWriter writer : writers) {
            writer.onKey(key);
        }
    }

    @Override
    public void onLong(long l) throws JsonException {
        for (GioWriter writer : writers) {
            writer.onLong(l);
        }
    }

    @Override
    public void onNull() throws JsonException {
        for (GioWriter writer : writers) {
            writer.onNull();
        }
    }

    @Override
    public void startDocument(GioDocument document) throws IOException {
        for (GioWriter writer : writers) {
            writer.startDocument(document);
        }
    }

    @Override
    public void startEdge(GioEdge edge) throws IOException {
        for (GioWriter writer : writers) {
            writer.startEdge(edge);
        }
    }

    @Override
    public void startGraph(GioGraph gioGraph) throws IOException {
        for (GioWriter writer : writers) {
            writer.startGraph(gioGraph);
        }
    }

    @Override
    public void startNode(GioNode node) throws IOException {
        for (GioWriter writer : writers) {
            writer.startNode(node);
        }
    }

    @Override
    public void startPort(GioPort port) throws IOException {
        for (GioWriter writer : writers) {
            writer.startPort(port);
        }
    }

    @Override
    public void stringCharacters(String s) throws JsonException {
        for (GioWriter writer : writers) {
            writer.stringCharacters(s);
        }
    }

    @Override
    public void stringEnd() throws JsonException {
        for (GioWriter writer : writers) {
            writer.stringEnd();
        }
    }

    @Override
    public void stringStart() throws JsonException {
        for (GioWriter writer : writers) {
            writer.stringStart();
        }
    }

    @Override
    public void whitespaceCharacters(String s) throws JsonException {
        for (GioWriter writer : writers) {
            writer.whitespaceCharacters(s);
        }
    }

}

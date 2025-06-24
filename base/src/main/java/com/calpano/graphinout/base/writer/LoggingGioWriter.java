package com.calpano.graphinout.base.writer;

import com.calpano.graphinout.base.gio.GioData;
import com.calpano.graphinout.base.gio.GioDocument;
import com.calpano.graphinout.base.gio.GioEdge;
import com.calpano.graphinout.base.gio.GioGraph;
import com.calpano.graphinout.base.gio.GioKey;
import com.calpano.graphinout.base.gio.GioNode;
import com.calpano.graphinout.base.gio.GioPort;
import com.calpano.graphinout.base.gio.GioWriter;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import java.io.IOException;
import java.net.URL;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Does not produce Graphml, rather, just logs
 */
public class LoggingGioWriter implements GioWriter {
    private static final Logger log = getLogger(LoggingGioWriter.class);

    @Override
    public void data(GioData data) throws IOException {
        log.info("GioWriter: data(GioData " + data + ")");
    }

    @Override
    public void endDocument() throws IOException {
        log.info("GioWriter: endDocument()");
    }

    @Override
    public void endEdge() throws IOException {
        log.info("GioWriter: endEdge()");
    }

    @Override
    public void endGraph(@Nullable URL locator) throws IOException {
        log.info("GioWriter: endGraph(" + locator + ")");
    }

    @Override
    public void endNode(@Nullable URL locator) throws IOException {
        log.info("GioWriter: endNode(" + locator + ")");
    }

    @Override
    public void endPort() throws IOException {
        log.info("GioWriter: endPort()");
    }

    @Override
    public void key(GioKey gioKey) throws IOException {
        log.info("GioWriter: key(GioKey " + gioKey + ")");
    }

    @Override
    public void startDocument(GioDocument document) throws IOException {
        log.info("GioWriter: startDocument(GioDocument " + document + ")");
    }

    @Override
    public void startEdge(GioEdge edge) throws IOException {
        log.info("GioWriter: startEdge(GioEdge " + edge + ")");
    }

    @Override
    public void startGraph(GioGraph gioGraph) throws IOException {
        log.info("GioWriter: startGraph(GioGraph " + gioGraph + ")");
    }

    @Override
    public void startNode(GioNode node) throws IOException {
        log.info("GioWriter: startNode(GioNode " + node + ")");
    }

    @Override
    public void startPort(GioPort port) throws IOException {
        log.info("GioWriter: startPort(GioPort " + port + ")");
    }
}

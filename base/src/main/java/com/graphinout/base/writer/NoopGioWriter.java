package com.graphinout.base.writer;

import com.graphinout.base.gio.GioData;
import com.graphinout.base.gio.GioDocument;
import com.graphinout.base.gio.GioEdge;
import com.graphinout.base.gio.GioGraph;
import com.graphinout.base.gio.GioKey;
import com.graphinout.base.gio.GioNode;
import com.graphinout.base.gio.GioPort;
import com.graphinout.base.gio.GioWriter;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import java.io.IOException;
import java.net.URL;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Does not produce Graphml, rather, just logs
 */
public class NoopGioWriter extends NoopJsonWriter implements GioWriter {


    private static final Logger log = getLogger(NoopGioWriter.class);

    @Override
    public void baseUri(String baseUri) throws IOException {}

    @Override
    public void data(GioData data) throws IOException {}

    @Override
    public void endDocument() throws IOException {}

    @Override
    public void endEdge() throws IOException {}

    @Override
    public void endGraph(@Nullable URL locator) throws IOException {}

    @Override
    public void endNode(@Nullable URL locator) throws IOException {}

    @Override
    public void endPort() throws IOException {}

    @Override
    public void key(GioKey gioKey) throws IOException {}

    @Override
    public void startDocument(GioDocument document) throws IOException {}

    @Override
    public void startEdge(GioEdge edge) throws IOException {}

    @Override
    public void startGraph(GioGraph gioGraph) throws IOException {}

    @Override
    public void startNode(GioNode node) throws IOException {}

    @Override
    public void startPort(GioPort port) throws IOException {}

}

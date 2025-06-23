package com.calpano.graphinout.base.gio;

import org.slf4j.Logger;

import javax.annotation.Nullable;
import java.io.IOException;
import java.net.URL;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Does not produce Graphml, rather, just logs
 */
public class NoopGioWriter implements GioWriter {

    private static final Logger log = getLogger(NoopGioWriter.class);

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

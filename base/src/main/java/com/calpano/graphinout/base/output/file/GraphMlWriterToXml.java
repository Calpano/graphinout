package com.calpano.graphinout.base.output.file;

import com.calpano.graphinout.base.*;
import com.calpano.graphinout.base.gio.GioGraph;
import com.calpano.graphinout.base.gio.GioDocument;
import com.calpano.graphinout.base.gio.GioEdge;
import com.calpano.graphinout.base.gio.GioKey;
import com.calpano.graphinout.base.gio.GioNode;
import com.calpano.graphinout.base.output.GraphMlWriter;

import java.io.IOException;

public class GraphMlWriterToXml<File> implements GraphMlWriter {

    private final ElementHandler outputHandler;

    public GraphMlWriterToXml(ElementHandler outputHandler) {
        this.outputHandler = outputHandler;
    }


    @Override
    public void startGraphMl(GioDocument gioGraphML) throws IOException {
        outputHandler.startElement();
    }

    @Override
    public void startKey(GioKey gioKey) throws IOException {

    }

    @Override
    public void end(GioKey gioKey) throws IOException {

    }

    @Override
    public void startGraph(GioGraph gioGraph) throws IOException {

    }

    @Override
    public void writeData(XMLValue value) throws IOException {

    }

    @Override
    public void startNode(GioNode node) throws IOException {

    }

    @Override
    public void endNode(GioNode node) throws IOException {

    }

    @Override
    public void startEdge(GioEdge edge) throws IOException {

    }

    @Override
    public void endEdge(GioEdge gioHyperEdge) throws IOException {

    }

    @Override
    public void endGraph(GioGraph gioGraph) throws IOException {

    }

    @Override
    public void endGraphMl(GioDocument gioGraphML) throws IOException {

    }
}
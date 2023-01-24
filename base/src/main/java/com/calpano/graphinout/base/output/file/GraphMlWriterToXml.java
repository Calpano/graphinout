package com.calpano.graphinout.base.output.file;

import com.calpano.graphinout.base.*;
import com.calpano.graphinout.base.exception.GioException;
import com.calpano.graphinout.base.output.GraphMlWriter;
import com.calpano.graphinout.base.output.OutputHandler;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

public class GraphMlWriterToXml<File> implements GraphMlWriter {

    private final ElementHandler outputHandler;

    public GraphMlWriterToXml(ElementHandler outputHandler) {
        this.outputHandler = outputHandler;
    }


    @Override
    public void startGraphMl(GioGraphML gioGraphML) throws IOException {
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
    public void startEdge(GioHyperEdge edge) throws IOException {

    }

    @Override
    public void endEdge(GioHyperEdge gioHyperEdge) throws IOException {

    }

    @Override
    public void endGraph(GioGraph gioGraph) throws IOException {

    }

    @Override
    public void endGraphMl(GioGraphML gioGraphML) throws IOException {

    }
}
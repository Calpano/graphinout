package com.calpano.graphinout.base.gio;

import com.calpano.graphinout.base.graphml.GraphmlDescription;
import com.calpano.graphinout.base.graphml.GraphmlDocument;
import com.calpano.graphinout.base.graphml.GraphmlWriter;

import java.io.IOException;

public class GioWriterImpl implements GioWriter {

    final GraphmlWriter graphmlWriter;

    public GioWriterImpl(GraphmlWriter graphmlWriter) {
        this.graphmlWriter = graphmlWriter;
    }

    @Override
    public void data(GioKey data) throws IOException {

    }

    @Override
    public void endDocument() throws IOException {

    }

    @Override
    public void endGraph() throws IOException {

    }

    @Override
    public void startDocument(GioDocument document) throws IOException {
        GraphmlDocument graphmlDocument = new GraphmlDocument();
        if(document.desc != null) {
            graphmlDocument.setDesc(new GraphmlDescription(document.desc));
        }
        // TODO keys
        // TODO data
        graphmlWriter.startDocument(graphmlDocument);
    }

    @Override
    public void startGraph(GioGraph gioGraph) throws IOException {

    }

    @Override
    public void startNode(GioNode node) throws IOException {

    }
}

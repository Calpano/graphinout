package com.graphinout.base.cj.document;

import com.graphinout.base.cj.stream.ICjStream;

public class CjDocument2CjStream {

    private final ICjStream cjStream;

    public CjDocument2CjStream(ICjStream cjStream) {this.cjStream = cjStream;}

    public static void toCjStream(ICjDocument cjDoc, ICjStream cjStream) {
        new CjDocument2CjStream(cjStream).writeDocumentToCjStream(cjDoc);
    }

    private void writeDocumentToCjStream(ICjDocument cjDoc) {
        cjStream.documentStart(cjDoc);
        cjDoc.graphs().forEach(this::writeGraphToCjStream);
        cjStream.documentEnd();
    }

    private void writeGraphToCjStream(ICjGraph cjGraph) {
        cjStream.graphStart(cjGraph);
        cjGraph.nodes().forEach(cjStream::node);
        cjGraph.edges().forEach(cjStream::edge);
        cjGraph.graphs().forEach(this::writeGraphToCjStream);
        cjStream.graphEnd();
    }

}

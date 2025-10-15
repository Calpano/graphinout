package com.graphinout.base.cj.stream.api;

import com.graphinout.base.cj.element.ICjDocumentChunk;
import com.graphinout.base.cj.element.ICjDocumentChunkMutable;
import com.graphinout.base.cj.element.ICjEdgeChunk;
import com.graphinout.base.cj.element.ICjEdgeChunkMutable;
import com.graphinout.base.cj.element.ICjGraphChunk;
import com.graphinout.base.cj.element.ICjGraphChunkMutable;
import com.graphinout.base.cj.element.ICjNodeChunk;
import com.graphinout.base.cj.element.ICjNodeChunkMutable;

import java.util.function.Consumer;

/** A high-level API good for creating examples */
public class CjDocumentStream {

    public class CjNodeStream {

        public void node(Consumer<ICjNodeChunk> node, Consumer<CjGraphStream> graphs) {
            ICjNodeChunkMutable chunk = cjStream.createNodeChunk();
            node.accept(chunk);
            cjStream.nodeStart(chunk);
            graphs.accept(new CjGraphStream());
            cjStream.nodeEnd();
        }

    }

    public class CjEdgeStream {

        public void edge(Consumer<ICjEdgeChunk> edge, Consumer<CjGraphStream> graphs) {
            ICjEdgeChunkMutable chunk = cjStream.createEdgeChunk();
            edge.accept(chunk);
            cjStream.edgeStart(chunk);
            graphs.accept(new CjGraphStream());
            cjStream.edgeEnd();
        }

    }

    public class CjGraphStream {

        public void graph(Consumer<ICjGraphChunk> graph, Consumer<CjNodeStream> nodes, Consumer<CjEdgeStream> edges, Consumer<CjGraphStream> graphs) {
            ICjGraphChunkMutable chunk = cjStream.createGraphChunk();
            graph.accept(chunk);
            cjStream.graphStart(chunk);
            nodes.accept(new CjNodeStream());
            edges.accept(new CjEdgeStream());
            graphs.accept(new CjGraphStream());
            cjStream.graphEnd();
        }

    }

    private final ICjStream cjStream;

    public CjDocumentStream(ICjStream cjStream) {this.cjStream = cjStream;}

    public void document(Consumer<ICjDocumentChunk> documentChunk, Consumer<CjDocumentStream.CjGraphStream> graphStream) {
        ICjDocumentChunkMutable chunk = cjStream.createDocumentChunk();
        documentChunk.accept(chunk);
        cjStream.documentStart(chunk);
        graphStream.accept(new CjGraphStream());
        cjStream.documentEnd();
    }

}

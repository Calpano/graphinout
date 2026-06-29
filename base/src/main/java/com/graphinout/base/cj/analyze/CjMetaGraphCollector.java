package com.graphinout.base.cj.analyze;

import com.graphinout.base.cj.document.ICjDocument;
import com.graphinout.base.cj.document.ICjDocumentChunk;
import com.graphinout.base.cj.document.ICjDocumentChunkMutable;
import com.graphinout.base.cj.document.ICjEdgeChunk;
import com.graphinout.base.cj.document.ICjEdgeChunkMutable;
import com.graphinout.base.cj.document.ICjElementType;
import com.graphinout.base.cj.document.ICjEndpoint;
import com.graphinout.base.cj.document.ICjGraphChunk;
import com.graphinout.base.cj.document.ICjGraphChunkMutable;
import com.graphinout.base.cj.document.ICjNodeChunk;
import com.graphinout.base.cj.document.ICjNodeChunkMutable;
import com.graphinout.base.cj.factory.CjFactory;
import com.graphinout.base.cj.factory.ICjFactory;
import com.graphinout.base.cj.stream.ICjStream;
import com.graphinout.foundation.pure.input.ContentError;
import com.graphinout.foundation.pure.input.Locator;
import com.graphinout.foundation.pure.json.document.IJsonFactory;
import org.jspecify.annotations.Nullable;

import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * A terminal {@link ICjStream} that derives a {@link CjMetaGraph} without ever materializing the input as a full
 * {@link ICjDocument}: it feeds each node's id/types and each edge's type/endpoint-ids into a
 * {@link CjMetaGraph.Accumulator} and lets everything else (labels, data, ports, graph nesting) flow past untouched.
 * Call {@link #build()} once the whole input has been read.
 *
 * <pre>{@code
 * CjMetaGraphCollector c = new CjMetaGraphCollector();
 * reader.read(input, c);
 * ICjDocument meta = c.build();
 * }</pre>
 */
public final class CjMetaGraphCollector implements ICjStream {

    private final ICjFactory factory = new CjFactory();
    private final CjMetaGraph.Accumulator accumulator = new CjMetaGraph.Accumulator();
    private @Nullable Consumer<ContentError> contentErrorHandler;
    private @Nullable Locator locator;

    /** The inferred meta graph; call after the full input has been streamed in. */
    public ICjDocument build() {
        return accumulator.build();
    }

    @Override
    public void nodeStart(ICjNodeChunk node) {
        accumulator.addNode(node.id(), node.types().map(ICjElementType::type).collect(Collectors.toList()));
    }

    @Override
    public void edgeStart(ICjEdgeChunk edge) {
        accumulator.addEdge(edge.type(), edge.endpoints().map(ICjEndpoint::node).collect(Collectors.toList()));
    }

    // ---- the rest of the stream is irrelevant to a type schema: ignore it ------------------------------------------

    @Override
    public void documentStart(ICjDocumentChunk document) {
    }

    @Override
    public void documentEnd() {
    }

    @Override
    public void graphStart(ICjGraphChunk graph) {
    }

    @Override
    public void graphEnd() {
    }

    @Override
    public void nodeEnd() {
    }

    @Override
    public void edgeEnd() {
    }

    // ---- factory + content-error / locator plumbing ----------------------------------------------------------------

    @Override
    public ICjDocumentChunkMutable createDocumentChunk() {
        return factory.createDocumentChunk();
    }

    @Override
    public ICjGraphChunkMutable createGraphChunk() {
        return factory.createGraphChunk();
    }

    @Override
    public ICjNodeChunkMutable createNodeChunk() {
        return factory.createNodeChunk();
    }

    @Override
    public ICjEdgeChunkMutable createEdgeChunk() {
        return factory.createEdgeChunk();
    }

    @Override
    public IJsonFactory jsonFactory() {
        return factory.jsonFactory();
    }

    @Override
    public void setContentErrorHandler(Consumer<ContentError> errorHandler) {
        this.contentErrorHandler = errorHandler;
    }

    @Override
    public @Nullable Consumer<ContentError> contentErrorHandler() {
        return contentErrorHandler;
    }

    @Override
    public @Nullable Locator locator() {
        return locator;
    }

    @Override
    public void setLocator(Locator locator) {
        this.locator = locator;
    }
}

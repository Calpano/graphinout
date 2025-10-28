package com.graphinout.base.writer;

import com.graphinout.base.cj.BaseCjOutput;
import com.graphinout.base.cj.element.ICjDocumentChunk;
import com.graphinout.base.cj.element.ICjDocumentChunkMutable;
import com.graphinout.base.cj.element.ICjEdgeChunk;
import com.graphinout.base.cj.element.ICjEdgeChunkMutable;
import com.graphinout.base.cj.element.ICjGraphChunk;
import com.graphinout.base.cj.element.ICjGraphChunkMutable;
import com.graphinout.base.cj.element.ICjNodeChunk;
import com.graphinout.base.cj.element.ICjNodeChunkMutable;
import com.graphinout.base.cj.stream.api.ICjStream;
import com.graphinout.base.reader.Locator;
import com.graphinout.foundation.input.ContentError;
import com.graphinout.foundation.json.value.IJsonFactory;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import java.util.function.Consumer;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Does not produce Graphml, rather, just logs
 */
@SuppressWarnings("StringConcatenationArgumentToLogCall")
public class LoggingCjStream extends BaseCjOutput implements ICjStream {

    private static final Logger log = getLogger(LoggingCjStream.class);

    @Nullable
    @Override
    public Consumer<ContentError> contentErrorHandler() {
        log.info("CjStream: contentErrorHandler()");
        return super.contentErrorHandler();
    }

    @Override
    public ICjDocumentChunkMutable createDocumentChunk() {
        log.info("CjStream: createDocumentChunk()");
        return super.createDocumentChunk();
    }

    @Override
    public ICjEdgeChunkMutable createEdgeChunk() {
        log.info("CjStream: createEdgeChunk()");
        return super.createEdgeChunk();
    }

    @Override
    public ICjGraphChunkMutable createGraphChunk() {
        log.info("CjStream: createGraphChunk()");
        return super.createGraphChunk();
    }

    @Override
    public ICjNodeChunkMutable createNodeChunk() {
        log.info("CjStream: createNodeChunk()");
        return super.createNodeChunk();
    }

    @Override
    public void documentEnd() {
        log.info("CjStream: documentEnd()");
    }

    @Override
    public void documentStart(ICjDocumentChunk document) {
        log.info("CjStream: documentStart(" + document + ")");
    }

    @Override
    public void edge(ICjEdgeChunk edge) {
        log.info("CjStream: edge(" + edge + ")");
    }

    @Override
    public void edgeEnd() {
        log.info("CjStream: edgeEnd()");
    }

    @Override
    public void edgeStart(ICjEdgeChunk edge) {
        log.info("CjStream: edgeStart(" + edge + ")");
    }

    @Override
    public void graphEnd() {
        log.info("CjStream: graphEnd()");
    }

    @Override
    public void graphStart(ICjGraphChunk graph) {
        log.info("CjStream: graphStart(" + graph + ")");
    }

    @Override
    public IJsonFactory jsonFactory() {
        // no-op
        return null;
    }

    @Nullable
    @Override
    public Locator locator() {
        return null;
    }

    @Override
    public void node(ICjNodeChunk node) {
        log.info("CjStream: node(" + node + ")");
    }

    @Override
    public void nodeEnd() {
        log.info("CjStream: nodeEnd()");
    }

    @Override
    public void nodeStart(ICjNodeChunk node) {
        log.info("CjStream: nodeStart(" + node + ")");
    }

    @Override
    public void setContentErrorHandler(Consumer<ContentError> contentErrorHandler) {
        log.info("CjStream: setContentErrorHandler(" + contentErrorHandler + ")");
    }

    @Override
    public void setLocator(Locator locator) {
        log.info("CjStream: setLocator(" + locator + ")");
    }

}

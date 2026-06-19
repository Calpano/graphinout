package com.graphinout.base.cj.anonymize;

import com.graphinout.base.cj.document.ICjDocument;
import com.graphinout.base.cj.document.ICjDocumentChunk;
import com.graphinout.base.cj.document.ICjDocumentChunkMutable;
import com.graphinout.base.cj.document.ICjEdgeChunk;
import com.graphinout.base.cj.document.ICjEdgeChunkMutable;
import com.graphinout.base.cj.document.ICjGraphChunk;
import com.graphinout.base.cj.document.ICjGraphChunkMutable;
import com.graphinout.base.cj.document.ICjNodeChunk;
import com.graphinout.base.cj.document.ICjNodeChunkMutable;
import com.graphinout.base.cj.stream.CjStream2CjWriter;
import com.graphinout.base.cj.stream.ICjStream;
import com.graphinout.base.cj.writer.CjWriter2CjDocumentWriter;
import com.graphinout.base.cj.writer.CjWriter2CjStream;
import com.graphinout.foundation.pure.input.ContentError;
import com.graphinout.foundation.pure.input.Locator;
import com.graphinout.foundation.pure.json.document.IJsonFactory;
import org.jspecify.annotations.Nullable;

import java.util.function.Consumer;

/**
 * An {@link ICjStream} that sits in front of a real writer's stream, anonymizes everything that flows
 * through it, and forwards the redacted result to that {@code delegate}.
 *
 * <p>Because anonymization is done at the document level (see {@link CjDocumentAnonymizer} — it needs to
 * index all identifiers and JSON keys before remapping them), this decorator buffers the incoming stream
 * into a complete {@link ICjDocument}, and on {@link #documentEnd()} anonymizes that document and replays
 * it into the delegate. Memory cost is one full document — acceptable for the "publish a sanitized copy"
 * use case.
 *
 * <pre>{@code
 * ICjStream out = writer.createCjStream(sink);
 * reader.read(input, new AnonymizingCjStream(out));
 * }</pre>
 */
public class AnonymizingCjStream implements ICjStream {

    private final ICjStream delegate;
    private final CjWriter2CjDocumentWriter docBuilder = new CjWriter2CjDocumentWriter();
    /** Captures the incoming stream into {@link #docBuilder} without reordering. */
    private final ICjStream capture = new CjStream2CjWriter(docBuilder, false);

    public AnonymizingCjStream(ICjStream delegate) {
        this.delegate = delegate;
    }

    // ---- stream lifecycle: buffer everything, then anonymize + replay on documentEnd ----

    @Override
    public void documentStart(ICjDocumentChunk document) {
        capture.documentStart(document);
    }

    @Override
    public void documentEnd() {
        capture.documentEnd();
        ICjDocument anonymized = CjDocumentAnonymizer.anonymize(docBuilder.resultDoc());
        anonymized.fire(new CjWriter2CjStream(delegate), false);
    }

    @Override
    public void graphStart(ICjGraphChunk graph) {
        capture.graphStart(graph);
    }

    @Override
    public void graphEnd() {
        capture.graphEnd();
    }

    @Override
    public void nodeStart(ICjNodeChunk node) {
        capture.nodeStart(node);
    }

    @Override
    public void nodeEnd() {
        capture.nodeEnd();
    }

    @Override
    public void edgeStart(ICjEdgeChunk edge) {
        capture.edgeStart(edge);
    }

    @Override
    public void edgeEnd() {
        capture.edgeEnd();
    }

    // ---- factory: chunks are created by the capture stream (events are buffered there) ----

    @Override
    public ICjDocumentChunkMutable createDocumentChunk() {
        return capture.createDocumentChunk();
    }

    @Override
    public ICjGraphChunkMutable createGraphChunk() {
        return capture.createGraphChunk();
    }

    @Override
    public ICjNodeChunkMutable createNodeChunk() {
        return capture.createNodeChunk();
    }

    @Override
    public ICjEdgeChunkMutable createEdgeChunk() {
        return capture.createEdgeChunk();
    }

    @Override
    public IJsonFactory jsonFactory() {
        return capture.jsonFactory();
    }

    // ---- content-error / locator plumbing: forward to the real writer (and the capture) ----

    @Override
    public void setContentErrorHandler(Consumer<ContentError> errorHandler) {
        capture.setContentErrorHandler(errorHandler);
        delegate.setContentErrorHandler(errorHandler);
    }

    @Override
    public @Nullable Consumer<ContentError> contentErrorHandler() {
        return delegate.contentErrorHandler();
    }

    @Override
    public @Nullable Locator locator() {
        return delegate.locator();
    }

    @Override
    public void setLocator(Locator locator) {
        capture.setLocator(locator);
        delegate.setLocator(locator);
    }
}

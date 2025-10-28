package com.graphinout.base.cj.stream.api;

import com.graphinout.base.cj.CjFactory;
import com.graphinout.base.cj.element.ICjDocumentChunk;
import com.graphinout.base.cj.element.ICjEdgeChunk;
import com.graphinout.base.cj.element.ICjGraphChunk;
import com.graphinout.base.cj.element.ICjNodeChunk;
import com.graphinout.foundation.input.ContentError;
import com.graphinout.base.reader.Locator;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

/**
 * Delegates one input stream to n output streams. The first one needs working factory methods like
 * {@link ICjStream#createDocumentChunk()}
 */
public class DelegatingCjStream extends CjFactory implements ICjStream {

    private final List<ICjStream> streams;

    public DelegatingCjStream(ICjStream... streams) {
        this.streams = Arrays.asList(streams);
    }

    @Nullable
    @Override
    public Consumer<ContentError> contentErrorHandler() {
        // try all streams
        for (ICjStream stream : streams) {
            Consumer<ContentError> errorHandler = stream.contentErrorHandler();
            if (errorHandler != null) return errorHandler;
        }
        return null;
    }

    @Override
    public void documentEnd() {
        for (ICjStream stream : streams) {
            stream.documentEnd();
        }
    }

    @Override
    public void documentStart(ICjDocumentChunk document) {
        for (ICjStream stream : streams) {
            stream.documentStart(document);
        }
    }

    @Override
    public void edgeEnd() {
        for (ICjStream stream : streams) {
            stream.edgeEnd();
        }
    }

    @Override
    public void edgeStart(ICjEdgeChunk edge) {
        for (ICjStream stream : streams) {
            stream.edgeStart(edge);
        }
    }

    @Override
    public void graphEnd() {
        for (ICjStream stream : streams) {
            stream.graphEnd();
        }
    }

    @Override
    public void graphStart(ICjGraphChunk graph) {
        for (ICjStream stream : streams) {
            stream.graphStart(graph);
        }
    }

    @Nullable
    @Override
    public Locator locator() {
        // try all streams
        for (ICjStream stream : streams) {
            Locator locator = stream.locator();
            if (locator != null) return locator
                    ;
        }
        return null;
    }

    @Override
    public void nodeEnd() {
        for (ICjStream stream : streams) {
            stream.nodeEnd();
        }
    }

    @Override
    public void nodeStart(ICjNodeChunk node) {
        for (ICjStream stream : streams) {
            stream.nodeStart(node);
        }
    }

    @Override
    public void setContentErrorHandler(Consumer<ContentError> errorHandler) {
        streams.forEach(s -> s.setContentErrorHandler(errorHandler));
    }

    @Override
    public void setLocator(Locator locator) {
        // set in all streams
        streams.forEach(s -> s.setLocator(locator));
    }

}

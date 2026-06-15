package com.graphinout.reader.gtfs;

import com.graphinout.base.cj.document.ICjDocumentChunk;
import com.graphinout.base.cj.document.ICjEdgeChunk;
import com.graphinout.base.cj.document.ICjGraphChunk;
import com.graphinout.base.cj.document.ICjNodeChunk;
import com.graphinout.base.cj.factory.CjFactory;
import com.graphinout.base.cj.stream.ICjStream;
import com.graphinout.base.input.FileSingleInputSource;
import com.graphinout.foundation.pure.input.ContentError;
import com.graphinout.foundation.pure.json.value.java.JavaJsonFactory;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

/**
 * Opt-in integration test against a real GTFS feed (e.g. the VBB feed from
 * <a href="https://www.vbb.de/vbbgtfs">vbb.de/vbbgtfs</a>, ~80 MB).
 * Runs only when the system property {@code gtfs.feed} points to a feed ZIP:
 * <pre>mvn -pl reader-gtfs test -Dtest=GtfsRealFeedIT -Dgtfs.feed=/path/to/gtfs.zip</pre>
 */
class GtfsRealFeedIT {

    @Test
    void readRealFeed() throws Exception {
        String feedPath = System.getProperty("gtfs.feed", "");
        assumeTrue(!feedPath.isEmpty() && new File(feedPath).isFile(), //
                "system property gtfs.feed not set or file missing - skipping");

        CjFactory factory = new CjFactory();
        ICjStream stream = Mockito.mock(ICjStream.class);
        when(stream.createDocumentChunk()).thenAnswer(inv -> factory.createDocumentChunk());
        when(stream.createGraphChunk()).thenAnswer(inv -> factory.createGraphChunk());
        when(stream.createNodeChunk()).thenAnswer(inv -> factory.createNodeChunk());
        when(stream.createEdgeChunk()).thenAnswer(inv -> factory.createEdgeChunk());
        when(stream.jsonFactory()).thenReturn(JavaJsonFactory.INSTANCE);
        AtomicInteger nodes = new AtomicInteger();
        AtomicInteger edges = new AtomicInteger();
        AtomicInteger graphs = new AtomicInteger();
        doAnswer(inv -> {
            graphs.incrementAndGet();
            return null;
        }).when(stream).graphStart(any(ICjGraphChunk.class));
        doAnswer(inv -> {
            nodes.incrementAndGet();
            return null;
        }).when(stream).node(any(ICjNodeChunk.class));
        doAnswer(inv -> {
            edges.incrementAndGet();
            return null;
        }).when(stream).edge(any(ICjEdgeChunk.class));

        GtfsReader reader = new GtfsReader();
        List<ContentError> errors = new ArrayList<>();
        reader.setContentErrorHandler(errors::add);

        long start = System.currentTimeMillis();
        reader.read(new FileSingleInputSource(new File(feedPath)), stream);
        long millis = System.currentTimeMillis() - start;

        System.out.printf("GTFS feed %s: %d graphs (base + routes), %d stop nodes, %d edges, %d content issues, %d ms%n", //
                feedPath, graphs.get(), nodes.get(), edges.get(), errors.size(), millis);
        errors.stream().limit(10).forEach(e -> System.out.println("  " + e.level + ": " + e.getMessage()));

        assertThat(nodes.get()).isGreaterThan(100);
        assertThat(edges.get()).isGreaterThan(100);
        assertThat(graphs.get()).isAtLeast(2); // base graph + at least one route subgraph
        assertThat(errors.stream().filter(ContentError::isError).count()).isEqualTo(0L);
        Mockito.verify(stream).documentStart(any(ICjDocumentChunk.class));
        Mockito.verify(stream, Mockito.times(graphs.get())).graphEnd();
        Mockito.verify(stream).documentEnd();
    }
}

package com.graphinout.reader.graphml;

import com.graphinout.base.cj.document.CjDocuments;
import com.graphinout.base.cj.document.ICjDocument;
import com.graphinout.base.input.SingleInputSource;
import com.graphinout.foundation.pure.input.ContentError;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static com.google.common.truth.Truth.assertWithMessage;

/**
 * GraphML reading on many threads at once — one {@link GraphmlReader} per thread, as a server or the IntelliJ plugin
 * would do — must give exactly the single-threaded result: same CJ document, same content errors. Guards against
 * shared mutable state anywhere on the read path (XML parsing, HTML-entity decoding, GraphML-to-CJ conversion).
 */
class GraphmlConcurrentReadTest {

    private static final int THREADS = 16;
    private static final int READS_PER_THREAD = 40;

    private static final List<String> DOCUMENTS = List.of(
            // HTML named entities go through the shared entity table
            """
            <?xml version='1.0' encoding='utf-8'?>
            <graphml xmlns="http://graphml.graphdrawing.org/xmlns">
              <key id="d0" attr.name="country" attr.type="string" for="node"/>
              <graph edgedefault="undirected">
                <node id="n1"><data key="d0">M&Eacute;XICO &zwnj; &AElig;</data></node>
                <node id="n2"><data key="d0">caf&eacute;</data></node>
                <edge source="n1" target="n2"/>
              </graph>
            </graphml>
            """,
            // nested graphs, an edge across levels, ports, a dangling edge (reported as an Error)
            """
            <graphml xmlns="http://graphml.graphdrawing.org/xmlns">
              <key id="w" attr.name="weight" attr.type="double" for="edge"/>
              <graph edgedefault="directed">
                <node id="a"><port name="p"/></node>
                <node id="g"><graph edgedefault="directed"><node id="g::x"/></graph></node>
                <edge id="e1" source="a" target="g::x" sourceport="p"><data key="w">1.5</data></edge>
                <edge id="e2" source="a" target="missing"/>
              </graph>
            </graphml>
            """);

    private record Result(String cj, List<String> errors) {}

    private static Result read(String graphml) throws Exception {
        List<ContentError> errors = new ArrayList<>();
        GraphmlReader reader = new GraphmlReader();
        reader.setContentErrorHandler(errors::add);
        ICjDocument doc = reader.readToCjDocument(SingleInputSource.of("concurrent.graphml", graphml));
        return new Result(doc == null ? "<null document>" : CjDocuments.toJsonString(doc), errors.stream().map(ContentError::toString).toList());
    }

    @Test
    void concurrentReadsMatchSingleThreadedResult() throws Exception {
        List<Result> expected = new ArrayList<>();
        for (String doc : DOCUMENTS) expected.add(read(doc));

        ConcurrentLinkedQueue<String> failures = new ConcurrentLinkedQueue<>();
        ExecutorService pool = Executors.newFixedThreadPool(THREADS);
        try {
            CyclicBarrier start = new CyclicBarrier(THREADS);
            List<Future<?>> futures = new ArrayList<>();
            for (int t = 0; t < THREADS; t++) {
                int thread = t;
                futures.add(pool.submit(() -> {
                    start.await();
                    for (int i = 0; i < READS_PER_THREAD; i++) {
                        int d = (thread + i) % DOCUMENTS.size();
                        try {
                            Result actual = read(DOCUMENTS.get(d));
                            if (!actual.equals(expected.get(d))) {
                                failures.add("thread " + thread + " read " + i + " doc " + d + ": got " + actual);
                            }
                        } catch (Throwable e) {
                            failures.add("thread " + thread + " read " + i + " doc " + d + ": " + e);
                        }
                    }
                    return null;
                }));
            }
            for (Future<?> f : futures) f.get(2, TimeUnit.MINUTES);
        } finally {
            pool.shutdownNow();
        }
        assertWithMessage("concurrent GraphML reads differ from the single-threaded result").that(failures).isEmpty();
    }
}

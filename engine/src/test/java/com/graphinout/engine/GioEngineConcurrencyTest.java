package com.graphinout.engine;

import com.graphinout.base.gio.GioInputAnalysis;
import com.graphinout.base.gio.GioInputAnalyzer;
import com.graphinout.base.gio.GioReader;
import com.graphinout.base.input.SingleInputSource;
import com.graphinout.foundation.pure.input.ContentError;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth.assertWithMessage;

/**
 * One {@link GioEngineCore} shared by many threads (a server, the IntelliJ plugin): readers hold per-use state (the
 * content error handler), so the engine hands out a new instance on every call and no thread sees another thread's
 * content errors.
 */
class GioEngineConcurrencyTest {

    private static final int THREADS = 16;
    private static final int ROUNDS = 25;

    private final GioEngineCore engine = new GioEngineCore();

    /** A PG-JSON document whose only content error names this thread's missing node. */
    private static String pgJsonWithDanglingEdge(String missingNode) {
        return """
                {"nodes":[{"id":"a","labels":[],"properties":{}}],
                 "edges":[{"from":"a","to":"%s","labels":[],"properties":{}}]}""".formatted(missingNode);
    }

    @Test
    void everyCallReturnsANewInstance() {
        assertThat(engine.getReader("tgf")).isNotSameInstanceAs(engine.getReader("tgf"));
        assertThat(engine.getWriter("graphml")).isNotSameInstanceAs(engine.getWriter("graphml"));
        assertThat(engine.readers().getFirst()).isNotSameInstanceAs(engine.readers().getFirst());
    }

    @Test
    void contentErrorsStayWithTheirRead() throws Exception {
        List<String> failures = runConcurrently(thread -> {
            String missing = "missing-" + thread;
            List<ContentError> errors = new ArrayList<>();
            GioReader reader = engine.getReader("pg-json");
            reader.setContentErrorHandler(errors::add);
            reader.readToCjDocument(SingleInputSource.of("t" + thread + ".pg.json", pgJsonWithDanglingEdge(missing)));
            List<String> messages = errors.stream().map(ContentError::getMessage).toList();
            List<String> expected = List.of("Edge references unknown node '" + missing + "'");
            return messages.equals(expected) ? null : "thread " + thread + " got " + messages;
        });
        assertWithMessage("content errors crossed between concurrent reads").that(failures).isEmpty();
    }

    @Test
    void sharedAnalyzerIsSafeForConcurrentAnalyze() throws Exception {
        GioInputAnalyzer analyzer = new GioInputAnalyzer(engine::readers);
        List<String> failures = runConcurrently(thread -> {
            boolean dot = thread % 2 == 0;
            GioInputAnalysis analysis = analyzer.analyze(dot
                    ? SingleInputSource.of("unknown.txt", "digraph { a" + thread + " -> b }")
                    : SingleInputSource.of("unknown.json", pgJsonWithDanglingEdge("x" + thread)));
            String best = analysis.best().map(c -> c.format().id()).orElse("<none>");
            String expected = dot ? "dot" : "pg-json";
            return best.equals(expected) ? null : "thread " + thread + " detected " + best + ", expected " + expected;
        });
        assertWithMessage("concurrent analyze() on one analyzer").that(failures).isEmpty();
    }

    /** Runs {@code task} on {@link #THREADS} threads, {@link #ROUNDS} times each; returns every non-null result or exception. */
    private static List<String> runConcurrently(ThreadTask task) throws Exception {
        ConcurrentLinkedQueue<String> failures = new ConcurrentLinkedQueue<>();
        ExecutorService pool = Executors.newFixedThreadPool(THREADS);
        try {
            CyclicBarrier start = new CyclicBarrier(THREADS);
            List<Future<?>> futures = new ArrayList<>();
            for (int t = 0; t < THREADS; t++) {
                int thread = t;
                Callable<Void> c = () -> {
                    start.await();
                    for (int r = 0; r < ROUNDS; r++) {
                        try {
                            String failure = task.run(thread);
                            if (failure != null) failures.add(failure);
                        } catch (Throwable e) {
                            failures.add("thread " + thread + ": " + e);
                        }
                    }
                    return null;
                };
                futures.add(pool.submit(c));
            }
            for (Future<?> f : futures) f.get(5, TimeUnit.MINUTES);
        } finally {
            pool.shutdownNow();
        }
        return new ArrayList<>(failures);
    }

    @FunctionalInterface
    private interface ThreadTask {
        String run(int thread) throws Exception;
    }
}

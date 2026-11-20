package com.graphinout.base.xml;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
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
 * {@link HtmlEntities} is shared by every reader that decodes HTML entities (GraphML, GEXF), and readers run
 * concurrently (servers, the IntelliJ plugin, parallel tests). Its entity table must be complete for every thread from
 * the very first call — a half-built table makes an entity like {@code &Eacute;} "unknown", the XML parse then fails,
 * and the read returns no document.
 * <p>
 * First use happens once per JVM, so each round loads the class afresh in its own class loader and lets several
 * threads hit it at the same instant.
 */
class HtmlEntitiesConcurrencyTest {

    private static final int ROUNDS = 200;
    private static final int THREADS = 8;

    @Test
    void firstConcurrentUseSeesTheCompleteTable() throws Exception {
        URL classes = HtmlEntities.class.getProtectionDomain().getCodeSource().getLocation();
        ConcurrentLinkedQueue<String> failures = new ConcurrentLinkedQueue<>();
        ExecutorService pool = Executors.newFixedThreadPool(THREADS);
        try {
            for (int round = 0; round < ROUNDS && failures.isEmpty(); round++) {
                // a fresh, uninitialized copy of the class (parent: platform loader, so it is not delegated to ours)
                URLClassLoader loader = new URLClassLoader(new URL[]{classes}, ClassLoader.getPlatformClassLoader());
                Class<?> fresh = loader.loadClass(HtmlEntities.class.getName());
                Method contains = fresh.getMethod("contains", String.class);
                Method replacement = fresh.getMethod("getDecimalReplacement", String.class);
                CyclicBarrier start = new CyclicBarrier(THREADS);
                List<Future<?>> futures = new ArrayList<>();
                int r = round;
                for (int t = 0; t < THREADS; t++) {
                    futures.add(pool.submit(() -> {
                        start.await();
                        // first and last entries of the table: both must be there for every thread
                        check(failures, r, "contains(AElig)", contains.invoke(null, "AElig"), true);
                        check(failures, r, "contains(zwnj)", contains.invoke(null, "zwnj"), true);
                        check(failures, r, "getDecimalReplacement(Eacute)", replacement.invoke(null, "Eacute"), "&#201;");
                        return null;
                    }));
                }
                for (Future<?> f : futures) {
                    try {
                        f.get(30, TimeUnit.SECONDS);
                    } catch (java.util.concurrent.ExecutionException e) {
                        Throwable cause = e.getCause() instanceof java.lang.reflect.InvocationTargetException ite ? ite.getCause() : e.getCause();
                        failures.add("round " + r + ": " + cause);
                    }
                }
                loader.close();
            }
        } finally {
            pool.shutdownNow();
        }
        assertWithMessage("concurrent first use of HtmlEntities saw an incomplete table").that(failures).isEmpty();
    }

    private static void check(ConcurrentLinkedQueue<String> failures, int round, String call, Object actual, Object expected) {
        if (!expected.equals(actual)) failures.add("round " + round + ": " + call + " returned " + actual + ", expected " + expected);
    }
}

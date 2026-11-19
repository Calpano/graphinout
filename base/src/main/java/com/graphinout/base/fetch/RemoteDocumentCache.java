package com.graphinout.base.fetch;

import org.jspecify.annotations.Nullable;

import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Process-wide store of remote documents: bundled snapshots (pinned by SHA-256, never expire), pre-warmed documents
 * ({@link #pin}, never expire), and documents fetched earlier (bounded by total size, with a time to live). Failures are remembered briefly, so a broken URL named by many
 * inputs is not fetched again and again.
 * <p>
 * Keys are URLs as normalized by {@link RemoteDocuments#key(URI, FetchLimits)}.
 */
public final class RemoteDocumentCache {

    public static final long DEFAULT_MAX_BYTES = 64L * 1024 * 1024;
    public static final Duration DEFAULT_TTL = Duration.ofHours(24);
    public static final Duration FAILURE_TTL = Duration.ofMinutes(5);
    private static final int MAX_FAILURES = 1024;

    private static final RemoteDocumentCache SHARED = new RemoteDocumentCache(DEFAULT_MAX_BYTES, DEFAULT_TTL);

    private record Entry(Instant at, RemoteDocument doc) {}

    private record Failure(Instant at, String message) {}

    private final long maxBytes;
    private final Duration ttl;
    private final Map<String, RemoteDocument> snapshots = new ConcurrentHashMap<>();
    /** access-ordered, for least-recently-used eviction */
    private final LinkedHashMap<String, Entry> entries = new LinkedHashMap<>(16, 0.75f, true);
    private final Map<String, Failure> failures = new ConcurrentHashMap<>();
    private long bytes;

    public RemoteDocumentCache(long maxBytes, Duration ttl) {
        this.maxBytes = maxBytes;
        this.ttl = ttl;
    }

    public static RemoteDocumentCache shared() {
        return SHARED;
    }

    /**
     * Registers a bundled snapshot under {@code url}.
     *
     * @throws IllegalStateException if {@code body} does not have the expected hash (a corrupted or edited snapshot)
     */
    public void registerSnapshot(URI url, @Nullable String contentType, byte[] body, String expectedSha256) {
        String actual = RemoteDocument.sha256(body);
        if (!actual.equalsIgnoreCase(expectedSha256)) {
            throw new IllegalStateException("Snapshot for " + url + " has sha256 " + actual + ", expected " + expectedSha256);
        }
        String key = RemoteDocuments.keyIgnoringPorts(url);
        snapshots.put(key, new RemoteDocument(url, url, RemoteDocument.Source.SNAPSHOT, contentType, null, body));
    }

    /**
     * Holds {@code doc} for {@code url} for the life of the process, like a snapshot: for applications that pre-warm
     * the cache (e.g. from {@code doc/popular-vocabularies.adoc}) and then run {@link RemoteContentPolicy.Mode#OFFLINE}.
     */
    public void pin(URI url, RemoteDocument doc) {
        snapshots.put(RemoteDocuments.keyIgnoringPorts(url),
                new RemoteDocument(url, doc.finalUrl(), RemoteDocument.Source.PREWARMED, doc.contentType(), doc.link(), doc.body()));
    }

    @Nullable RemoteDocument snapshot(String key) {
        return snapshots.get(key);
    }

    synchronized @Nullable RemoteDocument cached(String key, Instant now) {
        Entry e = entries.get(key);
        if (e == null) return null;
        if (e.at().plus(ttl).isBefore(now)) {
            remove(key);
            return null;
        }
        return e.doc();
    }

    synchronized void put(String key, RemoteDocument doc, Instant now) {
        if (doc.body().length > maxBytes) return;
        remove(key);
        entries.put(key, new Entry(now, doc));
        bytes += doc.body().length;
        Iterator<Map.Entry<String, Entry>> it = entries.entrySet().iterator();
        while (bytes > maxBytes && it.hasNext()) {
            bytes -= it.next().getValue().doc().body().length;
            it.remove();
        }
        failures.remove(key);
    }

    private void remove(String key) {
        Entry old = entries.remove(key);
        if (old != null) bytes -= old.doc().body().length;
    }

    @Nullable String failure(String key, Instant now) {
        Failure f = failures.get(key);
        if (f == null) return null;
        if (f.at().plus(FAILURE_TTL).isBefore(now)) {
            failures.remove(key);
            return null;
        }
        return f.message();
    }

    void putFailure(String key, String message, Instant now) {
        if (failures.size() >= MAX_FAILURES) failures.clear();
        failures.put(key, new Failure(now, message));
    }

    /** Forgets fetched documents and failures; snapshots stay. */
    public synchronized void clear() {
        entries.clear();
        failures.clear();
        bytes = 0;
    }
}

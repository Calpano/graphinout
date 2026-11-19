package com.graphinout.base.fetch;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RemoteDocumentsTest {

    static final String CTX = "{\"@context\":{}}";

    /** Canned responses; records every URL asked for; can advance a clock per fetch. */
    static class FakeFetcher implements UrlFetcher {
        final Map<String, FetchedResponse> responses = new HashMap<>();
        final List<String> fetched = new ArrayList<>();

        FakeFetcher ok(String url, String body) {
            responses.put(url, new FetchedResponse(200, "application/ld+json", null, null, body.getBytes(StandardCharsets.UTF_8)));
            return this;
        }

        FakeFetcher redirect(String url, String location) {
            responses.put(url, new FetchedResponse(302, null, location, null, new byte[0]));
            return this;
        }

        FakeFetcher status(String url, int status) {
            responses.put(url, new FetchedResponse(status, "text/plain", null, null, new byte[0]));
            return this;
        }

        @Override
        public FetchedResponse fetch(URI url, FetchLimits limits) throws IOException {
            fetched.add(url.toString());
            FetchedResponse r = responses.get(url.toString());
            if (r == null) throw new IOException("unexpected fetch " + url);
            return r;
        }
    }

    static class MutableClock extends Clock {
        Instant now = Instant.parse("2026-11-19T10:00:00Z");

        @Override public ZoneOffset getZone() { return ZoneOffset.UTC; }
        @Override public Clock withZone(java.time.ZoneId zone) { return this; }
        @Override public Instant instant() { return now; }
    }

    final MutableClock clock = new MutableClock();
    final RemoteDocumentCache cache = new RemoteDocumentCache(1_000_000, Duration.ofHours(24));

    RemoteDocuments session(RemoteContentPolicy policy, FakeFetcher fetcher) {
        return new RemoteDocuments(policy, cache, () -> fetcher, clock);
    }

    RemoteDocuments session(FakeFetcher fetcher) {
        return session(RemoteContentPolicy.PUBLIC_WEB, fetcher);
    }

    static String refused(RemoteDocuments docs, String url) {
        return assertThrows(RemoteContentException.class, () -> docs.get(URI.create(url))).getMessage();
    }

    @Test
    void fetchesPublicUrl() throws IOException {
        FakeFetcher f = new FakeFetcher().ok("https://ex.org/ctx", CTX);
        RemoteDocument doc = session(f).get(URI.create("https://ex.org/ctx"));
        assertThat(doc.source()).isEqualTo(RemoteDocument.Source.NETWORK);
        assertThat(new String(doc.body(), StandardCharsets.UTF_8)).isEqualTo(CTX);
    }

    @Test
    @DisplayName("other schemes, user info and unusual ports are refused before any fetch")
    void refusesUrls() {
        FakeFetcher f = new FakeFetcher();
        RemoteDocuments docs = session(f);
        for (String url : List.of("file:///etc/passwd", "jar:file:///x.jar!/a", "ftp://ex.org/a", "data:,x",
                "https://user@ex.org/a", "https://user:pw@ex.org/a", "https://ex.org:8080/a", "http://ex.org:22/a", "mailto:a@b")) {
            assertThat(refused(docs, url)).contains("Refusing");
        }
        assertThat(f.fetched).isEmpty();
    }

    @Test
    void explicitDefaultPortsAreFine() throws IOException {
        FakeFetcher f = new FakeFetcher().ok("https://ex.org:443/a", CTX);
        session(f).get(URI.create("https://ex.org:443/a"));
        assertThat(f.fetched).hasSize(1);
    }

    @Test
    @DisplayName("OFFLINE never fetches, but serves snapshots and cache")
    void offline() throws IOException {
        FakeFetcher f = new FakeFetcher().ok("https://ex.org/cached", CTX);
        session(f).get(URI.create("https://ex.org/cached")); // warms the cache
        cache.registerSnapshot(URI.create("https://ex.org/snap"), "application/ld+json", CTX.getBytes(StandardCharsets.UTF_8),
                RemoteDocument.sha256(CTX.getBytes(StandardCharsets.UTF_8)));
        f.fetched.clear();
        RemoteDocuments offline = session(RemoteContentPolicy.OFFLINE, f);
        assertThat(offline.get(URI.create("https://ex.org/cached")).source()).isEqualTo(RemoteDocument.Source.CACHE);
        assertThat(offline.get(URI.create("https://ex.org/snap")).source()).isEqualTo(RemoteDocument.Source.SNAPSHOT);
        assertThat(refused(offline, "https://ex.org/other")).contains("OFFLINE");
        assertThat(f.fetched).isEmpty();
    }

    @Test
    void snapshotHashIsVerified() {
        assertThrows(IllegalStateException.class, () -> cache.registerSnapshot(URI.create("https://ex.org/s"), null,
                "x".getBytes(StandardCharsets.UTF_8), "00"));
    }

    @Test
    @DisplayName("every redirect hop is checked; a refused target is never fetched")
    void redirects() throws IOException {
        FakeFetcher f = new FakeFetcher()
                .redirect("https://ex.org/a", "/b")
                .ok("https://ex.org/b", CTX)
                .redirect("https://ex.org/to-file", "file:///etc/passwd")
                .redirect("https://ex.org/to-port", "https://ex.org:2375/containers")
                .redirect("https://ex.org/downgrade", "http://other.org/x")
                .redirect("https://ex.org/same-host-http", "http://ex.org/b2")
                .ok("http://ex.org/b2", CTX);
        RemoteDocuments docs = session(f);
        assertThat(docs.get(URI.create("https://ex.org/a")).finalUrl().toString()).isEqualTo("https://ex.org/b");
        assertThat(refused(docs, "https://ex.org/to-file")).contains("Refusing");
        assertThat(refused(docs, "https://ex.org/to-port")).contains("Refusing");
        assertThat(refused(docs, "https://ex.org/downgrade")).contains("plain-text");
        docs.get(URI.create("https://ex.org/same-host-http"));
        assertThat(f.fetched).containsNoneOf("file:///etc/passwd", "https://ex.org:2375/containers", "http://other.org/x");
    }

    @Test
    void redirectLimit() {
        FakeFetcher f = new FakeFetcher().redirect("https://ex.org/loop", "/loop");
        assertThat(refused(session(f), "https://ex.org/loop")).contains("redirects");
        assertThat(f.fetched).hasSize(FetchLimits.DEFAULT.maxRedirects() + 1);
    }

    @Test
    @DisplayName("budgets: documents per input, bytes per input, time per input")
    void budgets() throws IOException {
        FakeFetcher f = new FakeFetcher().ok("https://ex.org/1", CTX).ok("https://ex.org/2", CTX).ok("https://ex.org/3", "x".repeat(100));
        RemoteDocuments two = session(RemoteContentPolicy.PUBLIC_WEB.withLimits(FetchLimits.DEFAULT.withMaxDocuments(2)), f);
        two.get(URI.create("https://ex.org/1"));
        two.get(URI.create("https://ex.org/2"));
        assertThat(refused(two, "https://ex.org/1")).contains("more than 2 remote documents");

        cache.clear();
        RemoteDocuments small = session(RemoteContentPolicy.PUBLIC_WEB.withLimits(FetchLimits.DEFAULT.withMaxTotalBytes(50)), f);
        assertThat(refused(small, "https://ex.org/3")).contains("exceed 50 bytes");

        cache.clear();
        RemoteDocuments slow = session(RemoteContentPolicy.PUBLIC_WEB.withLimits(FetchLimits.DEFAULT.withMaxDuration(Duration.ofSeconds(1))), f);
        clock.now = clock.now.plusSeconds(2);
        assertThat(refused(slow, "https://ex.org/1")).contains("time budget");
    }

    @Test
    @DisplayName("responses above the size limit are refused even if the fetcher returned them")
    void responseSize() {
        FakeFetcher f = new FakeFetcher().ok("https://ex.org/big", "x".repeat(100));
        RemoteDocuments docs = session(RemoteContentPolicy.PUBLIC_WEB.withLimits(FetchLimits.DEFAULT.withMaxResponseBytes(10)), f);
        assertThat(refused(docs, "https://ex.org/big")).contains("larger than 10 bytes");
    }

    @Test
    @DisplayName("fetched documents are cached; failures are remembered briefly")
    void caching() throws IOException {
        FakeFetcher f = new FakeFetcher().ok("https://ex.org/a", CTX).status("https://ex.org/missing", 404);
        session(f).get(URI.create("https://ex.org/a"));
        assertThat(session(f).get(URI.create("https://ex.org/a")).source()).isEqualTo(RemoteDocument.Source.CACHE);
        assertThat(refused(session(f), "https://ex.org/missing")).contains("HTTP 404");
        assertThat(refused(session(f), "https://ex.org/missing")).contains("failed recently");
        assertThat(f.fetched).containsExactly("https://ex.org/a", "https://ex.org/missing");
        clock.now = clock.now.plus(RemoteDocumentCache.FAILURE_TTL).plusSeconds(1);
        refused(session(f), "https://ex.org/missing");
        assertThat(f.fetched).hasSize(3);
    }

    @Test
    void customFetcher() throws IOException {
        FakeFetcher custom = new FakeFetcher().ok("https://ex.org/a", CTX);
        FakeFetcher unused = new FakeFetcher();
        RemoteDocuments docs = session(RemoteContentPolicy.custom(custom), unused);
        docs.get(URI.create("https://ex.org/a"));
        assertThat(custom.fetched).hasSize(1);
        assertThat(unused.fetched).isEmpty();
    }

    @Test
    void loadedReportsEverything() throws IOException {
        FakeFetcher f = new FakeFetcher().redirect("https://ex.org/a", "/b").ok("https://ex.org/b", CTX);
        RemoteDocuments docs = session(f);
        docs.get(URI.create("https://ex.org/a"));
        assertThat(docs.loaded()).hasSize(1);
        assertThat(docs.loaded().getFirst().describe()).contains("https://ex.org/a from network via https://ex.org/b (sha256 ");
    }

    @Test
    void defaultPolicyFromSystemProperty() {
        try {
            System.setProperty(RemoteContentPolicy.SYSTEM_PROPERTY, "offline");
            assertThat(RemoteContentPolicy.getDefault().mode()).isEqualTo(RemoteContentPolicy.Mode.OFFLINE);
            RemoteContentPolicy.setDefault(RemoteContentPolicy.PUBLIC_WEB);
            assertThat(RemoteContentPolicy.getDefault().mode()).isEqualTo(RemoteContentPolicy.Mode.PUBLIC_WEB);
        } finally {
            System.clearProperty(RemoteContentPolicy.SYSTEM_PROPERTY);
            RemoteContentPolicy.setDefault(null);
        }
        assertThat(RemoteContentPolicy.getDefault().mode()).isEqualTo(RemoteContentPolicy.Mode.PUBLIC_WEB);
    }
}

package com.graphinout.reader.rdf;

import com.graphinout.base.fetch.FetchLimits;
import com.graphinout.base.fetch.RemoteContentPolicy;
import com.graphinout.base.fetch.RemoteDocument;
import com.graphinout.base.fetch.RemoteDocumentCache;
import com.graphinout.base.fetch.RemoteDocuments;
import com.graphinout.base.fetch.UrlFetcher;
import com.graphinout.base.http.Hc5UrlFetcher;

import java.io.IOException;
import java.net.URI;
import java.time.Clock;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/** Application-level operations on JSON-LD remote contexts. */
public final class JsonLdContexts {

    private JsonLdContexts() {}

    /**
     * Fetches the given context URLs now (from public addresses only, with all checks) and holds them for the life of
     * the process. Typical use on a server: pre-warm from {@code doc/popular-vocabularies.adoc} at startup, then set
     * {@link RemoteContentPolicy#OFFLINE}, so that inputs can never cause network access.
     *
     * @return the URLs that could not be loaded, with the reason; empty if all were loaded
     */
    public static Map<URI, String> prewarm(Collection<URI> urls) {
        return prewarm(urls, Hc5UrlFetcher.shared());
    }

    /** As {@link #prewarm(Collection)}, with the application's own fetcher (proxy, mirror). */
    public static Map<URI, String> prewarm(Collection<URI> urls, UrlFetcher fetcher) {
        JsonLdSnapshots.register();
        RemoteDocumentCache cache = RemoteDocumentCache.shared();
        Map<URI, String> failures = new LinkedHashMap<>();
        for (URI url : urls) {
            // one session per URL: the per-input budgets are not meant for a list of hundreds
            RemoteDocuments remote = new RemoteDocuments(new RemoteContentPolicy(RemoteContentPolicy.Mode.CUSTOM,
                    FetchLimits.DEFAULT, fetcher), cache, () -> fetcher, Clock.systemUTC());
            try {
                RemoteDocument doc = JsonLdContextLoader.load(remote, url);
                cache.pin(url, doc);
            } catch (IOException e) {
                failures.put(url, e.getMessage());
            }
        }
        return failures;
    }
}

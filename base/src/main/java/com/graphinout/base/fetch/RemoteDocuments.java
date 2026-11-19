package com.graphinout.base.fetch;

import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Supplier;

/**
 * Remote documents for one input: the single way a reader gets content from a URL the input names.
 * <p>
 * In order: the URL is checked (http(s) only, no user info, allowed ports), then bundled snapshots and the process
 * cache are consulted, then — if the {@link RemoteContentPolicy policy} allows — the URL is fetched. Redirects are
 * followed here, never by the {@link UrlFetcher}, so every hop is checked again; the fetcher in turn only connects to
 * public addresses. Budgets ({@link FetchLimits}) bound what the input may cost. {@link #loaded()} reports every
 * document used, for transparency.
 * <p>
 * Not thread-safe; create one per input. See {@code doc/url-fetching-safe.adoc}.
 */
public final class RemoteDocuments {

    private final RemoteContentPolicy policy;
    private final RemoteDocumentCache cache;
    private final Supplier<UrlFetcher> defaultFetcher;
    private final Clock clock;
    private final Instant deadline;
    private final List<RemoteDocument> loaded = new ArrayList<>();
    private int documents;
    private long totalBytes;

    public RemoteDocuments(RemoteContentPolicy policy, RemoteDocumentCache cache, Supplier<UrlFetcher> defaultFetcher,
                           Clock clock) {
        this.policy = policy;
        this.cache = cache;
        this.defaultFetcher = defaultFetcher;
        this.clock = clock;
        this.deadline = clock.instant().plus(policy.limits().maxDuration());
    }

    /** With the shared cache and the system clock. */
    public static RemoteDocuments of(RemoteContentPolicy policy, Supplier<UrlFetcher> defaultFetcher) {
        return new RemoteDocuments(policy, RemoteDocumentCache.shared(), defaultFetcher, Clock.systemUTC());
    }

    public RemoteContentPolicy policy() {
        return policy;
    }

    /** Every document handed out so far, in order. */
    public List<RemoteDocument> loaded() {
        return List.copyOf(loaded);
    }

    /**
     * @throws RemoteContentException if the URL is refused, the policy forbids fetching it, a budget is exhausted, or
     *                                it can not be loaded; the message names the URL and the reason
     */
    public RemoteDocument get(URI url) throws IOException {
        FetchLimits limits = policy.limits();
        String key = key(url, limits);
        if (++documents > limits.maxDocuments()) {
            throw new RemoteContentException("Refusing to load " + url + ": more than " + limits.maxDocuments() + " remote documents for one input");
        }
        Instant now = clock.instant();
        RemoteDocument snapshot = cache.snapshot(key);
        if (snapshot != null) return use(snapshot.withSource(snapshot.source(), url));
        RemoteDocument cached = cache.cached(key, now);
        if (cached != null) return use(cached.withSource(RemoteDocument.Source.CACHE, url));
        String failure = cache.failure(key, now);
        if (failure != null) throw new RemoteContentException(failure + " (failed recently, not retried yet)");

        UrlFetcher fetcher = switch (policy.mode()) {
            case OFFLINE -> throw new RemoteContentException("Not loading " + url
                    + ": remote content is OFFLINE and the URL is neither a bundled snapshot nor cached");
            case CUSTOM -> policy.fetcher();
            case PUBLIC_WEB -> defaultFetcher.get();
        };
        assert fetcher != null;
        try {
            RemoteDocument doc = fetch(url, fetcher, limits);
            cache.put(key, doc, clock.instant());
            return use(doc);
        } catch (RemoteContentException e) {
            cache.putFailure(key, e.getMessage(), clock.instant());
            throw e;
        }
    }

    private RemoteDocument use(RemoteDocument doc) {
        loaded.add(doc);
        return doc;
    }

    private RemoteDocument fetch(URI url, UrlFetcher fetcher, FetchLimits limits) throws RemoteContentException {
        URI current = url;
        for (int redirects = 0; ; redirects++) {
            key(current, limits); // every hop: scheme, user info, port
            if (clock.instant().isAfter(deadline)) {
                throw new RemoteContentException("Refusing to load " + url + ": the time budget of "
                        + limits.maxDuration().toSeconds() + " s for this input's remote documents is used up");
            }
            FetchedResponse response;
            try {
                response = fetcher.fetch(current, limits);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RemoteContentException("Interrupted loading " + current, e);
            } catch (IOException e) {
                throw new RemoteContentException("Cannot load " + describe(url, current) + ": " + e.getMessage(), e);
            }
            if (response.isRedirect()) {
                if (redirects >= limits.maxRedirects()) {
                    throw new RemoteContentException("Cannot load " + url + ": more than " + limits.maxRedirects() + " redirects");
                }
                URI next = resolve(current, response.location());
                if (isDowngrade(current, next)) {
                    throw new RemoteContentException("Cannot load " + url + ": redirect from " + current + " to plain-text " + next
                            + " on another host");
                }
                current = next;
                continue;
            }
            if (response.status() != 200) {
                throw new RemoteContentException("Cannot load " + describe(url, current) + ": HTTP " + response.status());
            }
            int length = response.body().length;
            if (length > limits.maxResponseBytes()) {
                throw new RemoteContentException("Cannot load " + describe(url, current) + ": larger than " + limits.maxResponseBytes() + " bytes");
            }
            totalBytes += length;
            if (totalBytes > limits.maxTotalBytes()) {
                throw new RemoteContentException("Refusing to load " + url + ": remote documents for this input exceed "
                        + limits.maxTotalBytes() + " bytes");
            }
            return new RemoteDocument(url, current, RemoteDocument.Source.NETWORK, response.contentType(), response.link(), response.body());
        }
    }

    private static String describe(URI requested, URI current) {
        return current.equals(requested) ? requested.toString() : current + " (reached from " + requested + ")";
    }

    private static URI resolve(URI base, @Nullable String location) throws RemoteContentException {
        try {
            return base.resolve(new URI(location == null ? "" : location.strip()));
        } catch (URISyntaxException | IllegalArgumentException e) {
            throw new RemoteContentException("Invalid redirect location from " + base + ": " + location, e);
        }
    }

    /** https → http is allowed only on the same host, so a secure URL can not be bounced to a plain-text third party. */
    static boolean isDowngrade(URI from, URI to) {
        return "https".equalsIgnoreCase(from.getScheme()) && "http".equalsIgnoreCase(to.getScheme())
                && !from.getHost().equalsIgnoreCase(to.getHost());
    }

    /**
     * Checks a URL and returns its cache key: lowercase scheme and host, default port dropped, path normalized,
     * fragment dropped.
     *
     * @throws RemoteContentException for schemes other than http(s), user info, a missing host, or a port not in
     *                                {@code limits.ports()}
     */
    public static String key(URI url, FetchLimits limits) throws RemoteContentException {
        String key = keyOrNull(url, limits);
        if (key == null) {
            throw new RemoteContentException("Refusing to load " + url + ": only plain http(s) URLs on ports " + limits.ports()
                    + ", without user info, are fetched");
        }
        return key;
    }

    static String keyIgnoringPorts(URI url) {
        String key = keyOrNull(url, null);
        if (key == null) throw new IllegalArgumentException("Not a plain http(s) URL: " + url);
        return key;
    }

    private static @Nullable String keyOrNull(URI url, @Nullable FetchLimits limits) {
        String scheme = url.getScheme();
        if (scheme == null || url.isOpaque()) return null;
        scheme = scheme.toLowerCase(Locale.ROOT);
        if (!scheme.equals("https") && !scheme.equals("http")) return null;
        if (url.getRawUserInfo() != null || url.getHost() == null) return null;
        int port = url.getPort();
        boolean defaultPort = port == -1 || (port == 443 && scheme.equals("https")) || (port == 80 && scheme.equals("http"));
        if (!defaultPort && limits != null && !limits.ports().contains(port)) return null;
        String path = url.normalize().getRawPath();
        if (path == null || path.isEmpty()) path = "/";
        StringBuilder sb = new StringBuilder(scheme).append("://").append(url.getHost().toLowerCase(Locale.ROOT));
        if (!defaultPort) sb.append(':').append(port);
        sb.append(path);
        if (url.getRawQuery() != null) sb.append('?').append(url.getRawQuery());
        return sb.toString();
    }

}

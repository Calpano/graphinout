package com.graphinout.base.fetch;

import org.jspecify.annotations.Nullable;

import java.util.Locale;
import java.util.Objects;

/**
 * Whether and how remote content (e.g. JSON-LD {@code @context} documents) may be used. Chosen by the embedding
 * application or the CLI user; an input never chooses.
 *
 * @param fetcher used in {@link Mode#CUSTOM} only
 */
public record RemoteContentPolicy(Mode mode, FetchLimits limits, @Nullable UrlFetcher fetcher) {

    public enum Mode {
        /** Bundled snapshots and the (pre-warmed) cache only; no network access. Recommended for servers and CI. */
        OFFLINE,
        /** Snapshots, cache, then fetching from public internet addresses. The default. */
        PUBLIC_WEB,
        /** Snapshots, cache, then the application's own {@link UrlFetcher} (proxy, mirror). Budgets still apply. */
        CUSTOM
    }

    /** System property to choose the default mode: {@code offline} or {@code public-web}. */
    public static final String SYSTEM_PROPERTY = "graphinout.remoteContent";

    public static final RemoteContentPolicy OFFLINE = new RemoteContentPolicy(Mode.OFFLINE, FetchLimits.DEFAULT, null);
    public static final RemoteContentPolicy PUBLIC_WEB = new RemoteContentPolicy(Mode.PUBLIC_WEB, FetchLimits.DEFAULT, null);

    private static volatile @Nullable RemoteContentPolicy defaultPolicy;

    public RemoteContentPolicy {
        Objects.requireNonNull(mode);
        Objects.requireNonNull(limits);
        if (mode == Mode.CUSTOM && fetcher == null) throw new IllegalArgumentException("CUSTOM needs a fetcher");
    }

    public static RemoteContentPolicy custom(UrlFetcher fetcher) {
        return new RemoteContentPolicy(Mode.CUSTOM, FetchLimits.DEFAULT, fetcher);
    }

    public RemoteContentPolicy withLimits(FetchLimits limits) {
        return new RemoteContentPolicy(mode, limits, fetcher);
    }

    /** The policy readers use unless told otherwise: set by {@link #setDefault}, else the system property, else {@link #PUBLIC_WEB}. */
    public static RemoteContentPolicy getDefault() {
        RemoteContentPolicy p = defaultPolicy;
        if (p != null) return p;
        String prop = System.getProperty(SYSTEM_PROPERTY);
        if (prop != null && prop.strip().toLowerCase(Locale.ROOT).replace('_', '-').equals("offline")) return OFFLINE;
        return PUBLIC_WEB;
    }

    /** Sets the JVM-wide default; {@code null} returns to the system property / {@link #PUBLIC_WEB}. */
    public static void setDefault(@Nullable RemoteContentPolicy policy) {
        defaultPolicy = policy;
    }
}

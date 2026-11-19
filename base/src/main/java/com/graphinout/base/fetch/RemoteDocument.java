package com.graphinout.base.fetch;

import org.jspecify.annotations.Nullable;

import java.net.URI;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/**
 * A remote document as used by a reader, with where it came from.
 *
 * @param requested the URL the input named
 * @param finalUrl  the URL after redirects (equals {@code requested} for snapshots and cache hits of the same URL)
 * @param link      the {@code Link} header, if any (e.g. JSON-LD {@code rel="alternate"})
 */
public record RemoteDocument(URI requested, URI finalUrl, Source source, @Nullable String contentType,
                             @Nullable String link, byte[] body) {

    public enum Source {
        /** Bundled with GraphInOut, pinned by hash. */
        SNAPSHOT,
        /** Fetched earlier in this process. */
        CACHE,
        /** Fetched for this input. */
        NETWORK,
        /** Fetched by the application ahead of time ({@link RemoteDocumentCache#pin}); held for the life of the process. */
        PREWARMED
    }

    public String sha256() {
        return sha256(body);
    }

    public RemoteDocument withSource(Source source, URI requested) {
        return new RemoteDocument(requested, finalUrl, source, contentType, link, body);
    }

    public static String sha256(byte[] bytes) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    /** One line for reports: requested URL, source, final URL if different, hash. */
    public String describe() {
        String via = finalUrl.equals(requested) ? "" : " via " + finalUrl;
        return requested + " from " + source.name().toLowerCase() + via + " (sha256 " + sha256() + ")";
    }
}

package com.graphinout.base.fetch;

import java.time.Duration;
import java.util.Set;

/**
 * What one input may cost us in remote fetching. Per fetch: response size, redirects, timeouts. Per input: number of
 * remote documents, total bytes, total time. Only ports in {@code ports} may be named explicitly in a URL.
 */
public record FetchLimits(long maxResponseBytes, long maxTotalBytes, int maxDocuments, int maxRedirects,
                          Duration connectTimeout, Duration responseTimeout, Duration maxDuration, Set<Integer> ports) {

    public static final FetchLimits DEFAULT = new FetchLimits(
            4L * 1024 * 1024,   // the largest common context (schema.org's full vocabulary) is about 1.5 MiB
            16L * 1024 * 1024,
            32,
            5,                  // real chains (w3id.org, purl.org, DOI) need up to about 4
            Duration.ofSeconds(5),
            Duration.ofSeconds(15),
            Duration.ofSeconds(60),
            Set.of(80, 443));

    public FetchLimits {
        ports = Set.copyOf(ports);
    }

    public FetchLimits withPorts(Set<Integer> ports) {
        return new FetchLimits(maxResponseBytes, maxTotalBytes, maxDocuments, maxRedirects, connectTimeout, responseTimeout, maxDuration, ports);
    }

    public FetchLimits withMaxDocuments(int maxDocuments) {
        return new FetchLimits(maxResponseBytes, maxTotalBytes, maxDocuments, maxRedirects, connectTimeout, responseTimeout, maxDuration, ports);
    }

    public FetchLimits withMaxTotalBytes(long maxTotalBytes) {
        return new FetchLimits(maxResponseBytes, maxTotalBytes, maxDocuments, maxRedirects, connectTimeout, responseTimeout, maxDuration, ports);
    }

    public FetchLimits withMaxResponseBytes(long maxResponseBytes) {
        return new FetchLimits(maxResponseBytes, maxTotalBytes, maxDocuments, maxRedirects, connectTimeout, responseTimeout, maxDuration, ports);
    }

    public FetchLimits withMaxDuration(Duration maxDuration) {
        return new FetchLimits(maxResponseBytes, maxTotalBytes, maxDocuments, maxRedirects, connectTimeout, responseTimeout, maxDuration, ports);
    }
}

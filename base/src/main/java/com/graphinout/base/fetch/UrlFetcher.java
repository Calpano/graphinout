package com.graphinout.base.fetch;

import java.io.IOException;
import java.net.URI;

/**
 * Performs one HTTP GET. {@link RemoteDocuments} handles everything around it (policy, cache, redirects, budgets).
 * <p>
 * Contract for implementations:
 * <ul>
 *     <li>never follow redirects: return 3xx responses as they are;</li>
 *     <li>connect only to addresses {@link PublicAddresses#isPublic public}, and only to the very addresses that were
 *     checked (no second, unchecked name lookup — DNS rebinding);</li>
 *     <li>ignore system proxy settings; ask for {@code identity} encoding;</li>
 *     <li>read at most {@code limits.maxResponseBytes()} — after decoding, if the server compressed anyway — and fail
 *     if the body is larger.</li>
 * </ul>
 */
public interface UrlFetcher {

    FetchedResponse fetch(URI url, FetchLimits limits) throws IOException, InterruptedException;

}

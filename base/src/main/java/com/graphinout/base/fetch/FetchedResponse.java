package com.graphinout.base.fetch;

import org.jspecify.annotations.Nullable;

/**
 * One HTTP response, as returned by a {@link UrlFetcher}.
 *
 * @param location the {@code Location} header, if any
 * @param link     all {@code Link} header values joined with {@code ", "}, if any
 */
public record FetchedResponse(int status, @Nullable String contentType, @Nullable String location,
                              @Nullable String link, byte[] body) {

    public boolean isRedirect() {
        return status >= 300 && status < 400 && location != null;
    }
}

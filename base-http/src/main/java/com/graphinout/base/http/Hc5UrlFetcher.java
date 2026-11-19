package com.graphinout.base.http;

import com.graphinout.base.fetch.FetchLimits;
import com.graphinout.base.fetch.FetchedResponse;
import com.graphinout.base.fetch.UrlFetcher;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.util.Timeout;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Arrays;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;
import java.util.zip.InflaterInputStream;

/**
 * {@link UrlFetcher} on Apache HttpClient 5, fulfilling its contract: connects only to public addresses (via
 * {@link PublicOnlyDnsResolver}, so checked = connected), follows no redirects, retries nothing, uses no proxy or
 * cookies, asks for no compression, decodes gzip/deflate itself with the size limit applied to the decoded bytes (no
 * decompression bombs), refuses other encodings, and stops reading at the size limit.
 */
public final class Hc5UrlFetcher implements UrlFetcher {

    private static volatile @Nullable Hc5UrlFetcher shared;

    private final CloseableHttpClient client;

    Hc5UrlFetcher(PublicOnlyDnsResolver resolver, FetchLimits limits) {
        ConnectionConfig connectionConfig = ConnectionConfig.custom()
                .setConnectTimeout(Timeout.of(limits.connectTimeout()))
                .setSocketTimeout(Timeout.of(limits.responseTimeout()))
                .build();
        this.client = HttpClients.custom()
                .setConnectionManager(PoolingHttpClientConnectionManagerBuilder.create()
                        .setDnsResolver(resolver)
                        .setDefaultConnectionConfig(connectionConfig)
                        .build())
                // no useSystemProperties(): system proxies would resolve names where we can not check them
                .disableRedirectHandling()
                .disableAutomaticRetries()
                .disableContentCompression()
                .disableCookieManagement()
                .disableAuthCaching()
                .setUserAgent("graphinout")
                .build();
    }

    /** The fetcher for public internet addresses, shared by all readers. */
    public static Hc5UrlFetcher shared() {
        Hc5UrlFetcher result = shared;
        if (result == null) {
            synchronized (Hc5UrlFetcher.class) {
                result = shared;
                if (result == null) {
                    result = new Hc5UrlFetcher(PublicOnlyDnsResolver.INSTANCE, FetchLimits.DEFAULT);
                    shared = result;
                }
            }
        }
        return result;
    }

    @Override
    public FetchedResponse fetch(URI url, FetchLimits limits) throws IOException {
        String scheme = url.getScheme() == null ? "" : url.getScheme().toLowerCase(Locale.ROOT);
        if (!scheme.equals("https") && !scheme.equals("http")) throw new IOException("Only http(s) is fetched: " + url);
        HttpGet get = new HttpGet(url);
        get.setHeader("Accept", "application/ld+json, application/json;q=0.9, */*;q=0.1");
        get.setHeader("Accept-Encoding", "identity");
        get.setConfig(RequestConfig.custom().setResponseTimeout(Timeout.of(limits.responseTimeout())).build());
        return client.execute(get, response -> toFetched(url, response, limits.maxResponseBytes()));
    }

    private static FetchedResponse toFetched(URI url, ClassicHttpResponse response, long maxBytes) throws IOException {
        String encoding = header(response, "Content-Encoding");
        String enc = encoding == null ? "identity" : encoding.strip().toLowerCase(Locale.ROOT);
        if (!enc.equals("identity") && !enc.equals("gzip") && !enc.equals("x-gzip") && !enc.equals("deflate")) {
            throw new IOException("Unsupported content encoding '" + encoding + "' from " + url);
        }
        byte[] body = new byte[0];
        HttpEntity entity = response.getEntity();
        if (entity != null) {
            if (entity.getContentLength() > maxBytes) {
                throw new IOException("Response from " + url + " is larger than " + maxBytes + " bytes");
            }
            // some servers compress although we ask for identity: decode ourselves, and apply the limit to the
            // decoded size, so a small compressed body can not expand without bound (decompression bomb)
            try (InputStream raw = entity.getContent();
                 InputStream in = switch (enc) {
                     case "gzip", "x-gzip" -> new GZIPInputStream(raw);
                     case "deflate" -> new InflaterInputStream(raw);
                     default -> raw;
                 }) {
                body = in.readNBytes((int) Math.min(Integer.MAX_VALUE - 8, maxBytes + 1));
            }
            if (body.length > maxBytes) throw new IOException("Response from " + url + " is larger than " + maxBytes + " bytes");
        }
        Header[] links = response.getHeaders("Link");
        String link = links.length == 0 ? null : Arrays.stream(links).map(Header::getValue).collect(Collectors.joining(", "));
        return new FetchedResponse(response.getCode(), header(response, "Content-Type"), header(response, "Location"), link, body);
    }

    private static @Nullable String header(ClassicHttpResponse response, String name) {
        Header h = response.getFirstHeader(name);
        return h == null ? null : h.getValue();
    }
}

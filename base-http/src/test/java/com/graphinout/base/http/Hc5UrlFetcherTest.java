package com.graphinout.base.http;

import com.graphinout.base.fetch.FetchLimits;
import com.graphinout.base.fetch.FetchedResponse;
import com.graphinout.base.fetch.PublicAddresses;
import com.sun.net.httpserver.HttpServer;
import org.apache.hc.client5.http.DnsResolver;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class Hc5UrlFetcherTest {

    HttpServer server;
    final AtomicInteger requests = new AtomicInteger();
    final List<String> acceptEncodings = new ArrayList<>();
    int port;

    @BeforeEach
    void start() throws IOException {
        server = HttpServer.create(new InetSocketAddress(InetAddress.getLoopbackAddress(), 0), 0);
        server.createContext("/ctx", ex -> {
            requests.incrementAndGet();
            acceptEncodings.add(ex.getRequestHeaders().getFirst("Accept-Encoding"));
            ex.getResponseHeaders().add("Content-Type", "application/ld+json");
            ex.getResponseHeaders().add("Link", "</a>; rel=\"alternate\"");
            ex.getResponseHeaders().add("Link", "</b>; rel=\"next\"");
            send(ex, 200, "{\"@context\":{}}");
        });
        server.createContext("/redirect", ex -> {
            requests.incrementAndGet();
            ex.getResponseHeaders().add("Location", "http://169.254.169.254/latest/meta-data/");
            send(ex, 302, "");
        });
        server.createContext("/gzip", ex -> {
            requests.incrementAndGet();
            ex.getResponseHeaders().add("Content-Encoding", "gzip");
            sendBytes(ex, 200, gzip("{\"@context\":{}}"));
        });
        server.createContext("/bomb", ex -> {
            requests.incrementAndGet();
            ex.getResponseHeaders().add("Content-Encoding", "gzip");
            sendBytes(ex, 200, gzip(" ".repeat(10_000_000))); // ~10 KB compressed, 10 MB decoded
        });
        server.createContext("/br", ex -> {
            requests.incrementAndGet();
            ex.getResponseHeaders().add("Content-Encoding", "br");
            send(ex, 200, "x");
        });
        server.createContext("/big", ex -> {
            requests.incrementAndGet();
            send(ex, 200, "x".repeat(10_000));
        });
        server.start();
        port = server.getAddress().getPort();
    }

    static byte[] gzip(String s) throws IOException {
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        try (java.util.zip.GZIPOutputStream gz = new java.util.zip.GZIPOutputStream(bos)) {
            gz.write(s.getBytes(StandardCharsets.UTF_8));
        }
        return bos.toByteArray();
    }

    static void send(com.sun.net.httpserver.HttpExchange ex, int status, String body) throws IOException {
        sendBytes(ex, status, body.getBytes(StandardCharsets.UTF_8));
    }

    static void sendBytes(com.sun.net.httpserver.HttpExchange ex, int status, byte[] bytes) throws IOException {
        ex.sendResponseHeaders(status, bytes.length == 0 ? -1 : bytes.length);
        try (OutputStream out = ex.getResponseBody()) {
            out.write(bytes);
        }
    }

    @AfterEach
    void stop() {
        server.stop(0);
    }

    /** Resolves {@code *.test} names to loopback, counting lookups; everything else via the system. */
    static final class TestDns implements DnsResolver {
        final AtomicInteger lookups = new AtomicInteger();

        @Override
        public InetAddress[] resolve(String host) throws UnknownHostException {
            lookups.incrementAndGet();
            if (host.endsWith(".test")) return new InetAddress[]{InetAddress.getLoopbackAddress()};
            return InetAddress.getAllByName(host);
        }

        @Override
        public String resolveCanonicalHostname(String host) {
            return host;
        }
    }

    /** Production address policy, test DNS. */
    static Hc5UrlFetcher publicOnly(TestDns dns) {
        return new Hc5UrlFetcher(new PublicOnlyDnsResolver(dns, PublicAddresses::isPublic), FetchLimits.DEFAULT);
    }

    /** Loopback allowed, to test the fetcher's own behaviour against the local server. */
    static Hc5UrlFetcher loopbackAllowed(TestDns dns) {
        return new Hc5UrlFetcher(new PublicOnlyDnsResolver(dns, a -> PublicAddresses.isPublic(a) || a.isLoopbackAddress()), FetchLimits.DEFAULT);
    }

    URI url(String host, String path) {
        return URI.create("http://" + host + ":" + port + path);
    }

    @Test
    @DisplayName("loopback is refused by IP literal, by 'localhost', and by a public-looking name: no request is made")
    void refusesLoopback() {
        Hc5UrlFetcher fetcher = publicOnly(new TestDns());
        for (String host : List.of("127.0.0.1", "[::1]", "localhost", "rebind.test", "2130706433")) {
            IOException e = assertThrows(IOException.class, () -> fetcher.fetch(url(host, "/ctx"), FetchLimits.DEFAULT));
            assertThat(e).isInstanceOf(PublicAddresses.BlockedAddressException.class);
        }
        assertThat(requests.get()).isEqualTo(0);
    }

    @Test
    @DisplayName("the shared fetcher refuses loopback too")
    void sharedRefusesLoopback() {
        assertThrows(PublicAddresses.BlockedAddressException.class,
                () -> Hc5UrlFetcher.shared().fetch(url("127.0.0.1", "/ctx"), FetchLimits.DEFAULT));
        assertThat(requests.get()).isEqualTo(0);
    }

    @Test
    @DisplayName("one lookup per connection: the checked address is the connected address (no DNS rebinding)")
    void singleLookup() throws Exception {
        TestDns dns = new TestDns();
        FetchedResponse r = loopbackAllowed(dns).fetch(url("ctx.test", "/ctx"), FetchLimits.DEFAULT);
        assertThat(r.status()).isEqualTo(200);
        assertThat(dns.lookups.get()).isEqualTo(1);
    }

    @Test
    void fetchesBodyHeadersAndAsksForNoCompression() throws Exception {
        FetchedResponse r = loopbackAllowed(new TestDns()).fetch(url("ctx.test", "/ctx"), FetchLimits.DEFAULT);
        assertThat(new String(r.body(), StandardCharsets.UTF_8)).isEqualTo("{\"@context\":{}}");
        assertThat(r.contentType()).isEqualTo("application/ld+json");
        assertThat(r.link()).isEqualTo("</a>; rel=\"alternate\", </b>; rel=\"next\"");
        assertThat(acceptEncodings).containsExactly("identity");
    }

    @Test
    @DisplayName("redirects are returned, not followed")
    void noRedirects() throws Exception {
        FetchedResponse r = loopbackAllowed(new TestDns()).fetch(url("ctx.test", "/redirect"), FetchLimits.DEFAULT);
        assertThat(r.isRedirect()).isTrue();
        assertThat(r.location()).isEqualTo("http://169.254.169.254/latest/meta-data/");
        assertThat(requests.get()).isEqualTo(1);
    }

    @Test
    @DisplayName("gzip is decoded; the size limit applies to the decoded bytes (decompression bomb)")
    void compression() throws Exception {
        FetchedResponse r = loopbackAllowed(new TestDns()).fetch(url("ctx.test", "/gzip"), FetchLimits.DEFAULT);
        assertThat(new String(r.body(), StandardCharsets.UTF_8)).isEqualTo("{\"@context\":{}}");
        IOException bomb = assertThrows(IOException.class, () -> loopbackAllowed(new TestDns()).fetch(url("ctx.test", "/bomb"), FetchLimits.DEFAULT));
        assertThat(bomb).hasMessageThat().contains("larger than");
        IOException br = assertThrows(IOException.class, () -> loopbackAllowed(new TestDns()).fetch(url("ctx.test", "/br"), FetchLimits.DEFAULT));
        assertThat(br).hasMessageThat().contains("Unsupported content encoding");
    }

    @Test
    void sizeLimit() {
        IOException e = assertThrows(IOException.class, () -> loopbackAllowed(new TestDns())
                .fetch(url("ctx.test", "/big"), FetchLimits.DEFAULT.withMaxResponseBytes(100)));
        assertThat(e).hasMessageThat().contains("larger than 100 bytes");
    }

    @Test
    void refusesOtherSchemes() {
        assertThrows(IOException.class, () -> publicOnly(new TestDns()).fetch(URI.create("file:///etc/passwd"), FetchLimits.DEFAULT));
    }
}

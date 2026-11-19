package com.graphinout.reader.rdf;

import com.graphinout.base.cj.document.CjDocuments;
import com.graphinout.base.cj.stream.CjStream2CjWriter;
import com.graphinout.base.cj.writer.CjWriter2CjDocumentWriter;
import com.graphinout.base.fetch.FetchLimits;
import com.graphinout.base.fetch.FetchedResponse;
import com.graphinout.base.fetch.RemoteContentPolicy;
import com.graphinout.base.fetch.RemoteDocument;
import com.graphinout.base.fetch.RemoteDocumentCache;
import com.graphinout.base.fetch.UrlFetcher;
import com.graphinout.base.input.SingleInputSource;
import com.graphinout.foundation.pure.input.ContentError;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/** JSON-LD input names remote contexts; we load them only through RemoteDocuments (doc/url-fetching-safe.adoc). */
class JsonLdContextLoaderTest {

    static final String CONTEXT = "{\"@context\":{\"name\":\"http://example.org/name\"}}";

    record Result(String cj, List<ContentError> messages) {}

    /** Canned responses for a CUSTOM policy; records every URL asked for. */
    static class FakeFetcher implements UrlFetcher {
        final Map<String, FetchedResponse> responses = new HashMap<>();
        final List<String> fetched = new ArrayList<>();

        FakeFetcher json(String url, String body) {
            responses.put(url, new FetchedResponse(200, "application/ld+json", null, null, body.getBytes(StandardCharsets.UTF_8)));
            return this;
        }

        FakeFetcher htmlWithLink(String url, String link) {
            responses.put(url, new FetchedResponse(200, "text/html", null, link, "<html/>".getBytes(StandardCharsets.UTF_8)));
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

    @AfterEach
    void forgetFetched() {
        RemoteDocumentCache.shared().clear();
    }

    static Result read(String jsonld, RemoteContentPolicy policy) throws IOException {
        JsonLdReader reader = new JsonLdReader();
        reader.setRemoteContentPolicy(policy);
        List<ContentError> messages = new ArrayList<>();
        reader.setContentErrorHandler(messages::add);
        CjWriter2CjDocumentWriter cj = new CjWriter2CjDocumentWriter();
        reader.read(SingleInputSource.of("x.jsonld", jsonld), new CjStream2CjWriter(cj, true));
        return new Result(CjDocuments.toJsonString(cj.resultDoc()), messages);
    }

    static String withContext(String context) {
        return "{\"@context\": \"" + context + "\", \"@id\": \"http://ex/a\", \"name\": \"v\"}";
    }

    static String refused(String jsonld, RemoteContentPolicy policy) {
        return assertThrows(IOException.class, () -> read(jsonld, policy)).getMessage();
    }

    @Test
    @DisplayName("bundled snapshots work OFFLINE, and their use is reported")
    void snapshotsOffline() throws IOException {
        for (String ctx : List.of("https://schema.org/", "http://schema.org", "https://www.w3.org/ns/activitystreams",
                "https://w3id.org/security/v2", "https://www.w3.org/ns/credentials/v2")) {
            Result r = read(withContext(ctx), RemoteContentPolicy.OFFLINE);
            assertThat(r.messages()).isNotEmpty();
            assertThat(r.messages().getFirst().level).isEqualTo(ContentError.ErrorLevel.Info);
            assertThat(r.messages().getFirst().message).contains("from snapshot");
            assertThat(r.cj()).contains(RdfReader.DATA_REMOTE_CONTEXT);
        }
        assertThat(read(withContext("https://schema.org/"), RemoteContentPolicy.OFFLINE).cj()).contains("http://schema.org/name");
    }

    @Test
    @DisplayName("OFFLINE refuses contexts that are not bundled or cached")
    void offlineRefusesUnknown() {
        assertThat(refused(withContext("https://example.org/ctx.jsonld"), RemoteContentPolicy.OFFLINE)).contains("OFFLINE");
    }

    @Test
    @DisplayName("a file: context is refused, and the file is not read")
    void fileContext(@TempDir Path dir) throws IOException {
        Path ctx = dir.resolve("ctx.jsonld");
        Files.writeString(ctx, "{\"@context\":{\"name\":\"http://example.org/SECRET42\"}}");
        FakeFetcher f = new FakeFetcher();
        String msg = refused(withContext(ctx.toUri().toString()), RemoteContentPolicy.custom(f));
        assertThat(msg).contains("Refusing");
        assertThat(msg).doesNotContain("SECRET42");
        assertThat(f.fetched).isEmpty();
    }

    @Test
    @DisplayName("PUBLIC_WEB refuses private addresses (here: loopback and cloud metadata)")
    void privateAddresses() {
        assertThat(refused(withContext("http://127.0.0.1/ctx"), RemoteContentPolicy.PUBLIC_WEB)).contains("non-public address");
        assertThat(refused(withContext("http://169.254.169.254/latest/meta-data/"), RemoteContentPolicy.PUBLIC_WEB)).contains("non-public address");
        assertThat(refused(withContext("http://localhost/ctx"), RemoteContentPolicy.PUBLIC_WEB)).contains("non-public address");
    }

    @Test
    @DisplayName("other ports are refused before any connection")
    void ports() {
        assertThat(refused(withContext("http://example.org:6379/"), RemoteContentPolicy.PUBLIC_WEB)).contains("Refusing");
    }

    @Test
    @DisplayName("fetched contexts are used and reported; Link rel=alternate is followed through the same checks")
    void customFetcherAndAlternate() throws IOException {
        FakeFetcher f = new FakeFetcher()
                .json("https://ctx.example.org/v1", CONTEXT)
                .htmlWithLink("https://ctx.example.org/", "</ctx.jsonld>; rel=\"alternate\"; type=\"application/ld+json\"")
                .json("https://ctx.example.org/ctx.jsonld", CONTEXT)
                .htmlWithLink("https://ctx.example.org/bad", "<file:///etc/passwd>; rel=\"alternate\"; type=\"application/ld+json\"");
        RemoteContentPolicy policy = RemoteContentPolicy.custom(f);
        Result direct = read(withContext("https://ctx.example.org/v1"), policy);
        assertThat(direct.cj()).contains("http://example.org/name");
        assertThat(direct.messages().getFirst().message).contains("https://ctx.example.org/v1 from network");

        Result viaLink = read(withContext("https://ctx.example.org/"), policy);
        assertThat(viaLink.cj()).contains("http://example.org/name");

        assertThat(refused(withContext("https://ctx.example.org/bad"), policy)).contains("Refusing");
        assertThat(f.fetched).doesNotContain("file:///etc/passwd");
    }

    @Test
    @DisplayName("too many remote contexts in one input are refused")
    void documentBudget() {
        FakeFetcher f = new FakeFetcher();
        for (int i = 0; i < 3; i++) f.json("https://ctx.example.org/c" + i, "{\"@context\":{\"p" + i + "\":\"http://ex/p" + i + "\"}}");
        String jsonld = "{\"@context\": [\"https://ctx.example.org/c0\", \"https://ctx.example.org/c1\", \"https://ctx.example.org/c2\"], \"@id\": \"http://ex/a\"}";
        RemoteContentPolicy two = RemoteContentPolicy.custom(f).withLimits(FetchLimits.DEFAULT.withMaxDocuments(2));
        assertThat(refused(jsonld, two)).contains("more than 2 remote documents");
    }

    @Test
    @DisplayName("inline contexts need no loading and report nothing")
    void inlineContext() throws IOException {
        Result r = read("{\"@context\": {\"name\": \"http://example.org/name\"}, \"@id\": \"http://ex/a\", \"name\": \"v\"}", RemoteContentPolicy.OFFLINE);
        assertThat(r.cj()).contains("http://example.org/name");
        assertThat(r.cj()).doesNotContain(RdfReader.DATA_REMOTE_CONTEXT);
        assertThat(r.messages()).isEmpty();
    }

    @Test
    @DisplayName("every bundled snapshot is intact (hash) and served for each of its URLs")
    void bundledSnapshots() throws IOException {
        JsonLdSnapshots.register();
        String index = new String(JsonLdSnapshots.class.getResourceAsStream(JsonLdSnapshots.INDEX).readAllBytes(), StandardCharsets.UTF_8);
        List<JsonLdSnapshots.Entry> entries = JsonLdSnapshots.entries(index);
        assertThat(entries).isNotEmpty();
        var remote = com.graphinout.base.fetch.RemoteDocuments.of(RemoteContentPolicy.OFFLINE, () -> {
            throw new AssertionError("no fetching");
        });
        for (JsonLdSnapshots.Entry e : entries) {
            for (URI url : e.urls()) {
                RemoteDocument doc = remote.get(url);
                assertThat(doc.source()).isEqualTo(RemoteDocument.Source.SNAPSHOT);
                assertThat(doc.sha256()).isEqualTo(e.sha256());
            }
        }
    }

    @Test
    void alternateLinkParsing() {
        assertThat(JsonLdContextLoader.alternateJsonLd("</docs/jsonldcontext.jsonld>; rel=\"alternate\"; type=\"application/ld+json\""))
                .isEqualTo("/docs/jsonldcontext.jsonld");
        assertThat(JsonLdContextLoader.alternateJsonLd("<a>; rel=\"next\", <b>; rel=alternate; type=application/ld+json")).isEqualTo("b");
        assertThat(JsonLdContextLoader.alternateJsonLd("<a>; rel=\"alternate\"; type=\"text/turtle\"")).isNull();
        assertThat(JsonLdContextLoader.alternateJsonLd(null)).isNull();
    }

    @Test
    @DisplayName("pre-warmed contexts are then available OFFLINE, for the life of the process")
    void prewarm() throws IOException {
        FakeFetcher f = new FakeFetcher().json("https://warm.example.org/ctx", CONTEXT);
        Map<URI, String> failures = JsonLdContexts.prewarm(List.of(URI.create("https://warm.example.org/ctx"),
                URI.create("file:///etc/passwd")), f);
        assertThat(failures.keySet()).containsExactly(URI.create("file:///etc/passwd"));
        Result r = read(withContext("https://warm.example.org/ctx"), RemoteContentPolicy.OFFLINE);
        assertThat(r.cj()).contains("http://example.org/name");
        assertThat(r.messages().getFirst().message).contains("from prewarmed");
        RemoteDocumentCache.shared().clear(); // clears fetched documents, not pinned ones
        assertThat(read(withContext("https://warm.example.org/ctx"), RemoteContentPolicy.OFFLINE).cj()).contains("http://example.org/name");
    }

}

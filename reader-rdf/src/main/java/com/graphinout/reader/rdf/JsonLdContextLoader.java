package com.graphinout.reader.rdf;

import com.apicatalog.jsonld.JsonLdError;
import com.apicatalog.jsonld.JsonLdErrorCode;
import com.apicatalog.jsonld.document.Document;
import com.apicatalog.jsonld.document.JsonDocument;
import com.apicatalog.jsonld.http.media.MediaType;
import com.apicatalog.jsonld.loader.DocumentLoader;
import com.apicatalog.jsonld.loader.DocumentLoaderOptions;
import com.graphinout.base.fetch.RemoteDocument;
import com.graphinout.base.fetch.RemoteDocuments;
import org.jspecify.annotations.Nullable;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Locale;

/**
 * Loads JSON-LD remote contexts for one input, through {@link RemoteDocuments}: bundled snapshots first, then the
 * cache, then — if the policy allows — public internet addresses only, with every redirect checked and budgets
 * enforced. Titanium's own loaders, which would open {@code file:} URLs and any host, are never used.
 * <p>
 * Adds the JSON-LD specific step: a non-JSON response pointing to the JSON-LD via
 * {@code Link: <…>; rel="alternate"; type="application/ld+json"} (JSON-LD 1.1 API, remote document retrieval) is
 * followed once, and the target goes through the same checks. See {@code doc/url-fetching-safe.adoc}.
 */
public final class JsonLdContextLoader implements DocumentLoader {

    private final RemoteDocuments remoteDocuments;

    public JsonLdContextLoader(RemoteDocuments remoteDocuments) {
        JsonLdSnapshots.register();
        this.remoteDocuments = remoteDocuments;
    }

    @Override
    public Document loadDocument(URI url, DocumentLoaderOptions options) throws JsonLdError {
        RemoteDocument doc;
        try {
            doc = load(url);
        } catch (IOException e) {
            throw new JsonLdError(JsonLdErrorCode.LOADING_REMOTE_CONTEXT_FAILED, "JSON-LD context " + url + ": " + e.getMessage(), e);
        }
        JsonDocument json = JsonDocument.of(MediaType.JSON_LD, new ByteArrayInputStream(doc.body()));
        json.setDocumentUrl(doc.finalUrl());
        return json;
    }

    /** The context document for {@code url}, following a {@code Link rel="alternate"} once. */
    public RemoteDocument load(URI url) throws IOException {
        return load(remoteDocuments, url);
    }

    /** As {@link #load(URI)}, with the given documents (used by the snapshot refresh tool). */
    static RemoteDocument load(RemoteDocuments remoteDocuments, URI url) throws IOException {
        RemoteDocument doc = remoteDocuments.get(url);
        if (!isJson(doc.contentType())) {
            String alternate = alternateJsonLd(doc.link());
            if (alternate != null) {
                URI target;
                try {
                    target = doc.finalUrl().resolve(alternate);
                } catch (IllegalArgumentException e) {
                    throw new IOException("Invalid Link alternate '" + alternate + "' from " + doc.finalUrl(), e);
                }
                return remoteDocuments.get(target);
            }
        }
        return doc;
    }

    static boolean isJson(@Nullable String contentType) {
        if (contentType == null) return true; // snapshots and unknown types: let the JSON parser decide
        String ct = contentType.toLowerCase(Locale.ROOT);
        int semi = ct.indexOf(';');
        if (semi >= 0) ct = ct.substring(0, semi);
        ct = ct.strip();
        return ct.equals("application/json") || ct.equals("application/ld+json") || ct.endsWith("+json");
    }

    /** The target of a {@code Link: <...>; rel="alternate"; type="application/ld+json"} header, if any. */
    static @Nullable String alternateJsonLd(@Nullable String link) {
        if (link == null) return null;
        for (String entry : link.split(",")) {
            String e = entry.strip();
            int close = e.indexOf('>');
            if (!e.startsWith("<") || close < 0) continue;
            boolean alternate = false;
            boolean jsonLd = false;
            for (String param : e.substring(close + 1).split(";")) {
                String[] kv = param.split("=", 2);
                if (kv.length != 2) continue;
                String key = kv[0].strip().toLowerCase(Locale.ROOT);
                String value = kv[1].strip().replace("\"", "").toLowerCase(Locale.ROOT);
                if (key.equals("rel") && List.of(value.split("\\s+")).contains("alternate")) alternate = true;
                if (key.equals("type") && value.equals("application/ld+json")) jsonLd = true;
            }
            if (alternate && jsonLd) return e.substring(1, close);
        }
        return null;
    }

}

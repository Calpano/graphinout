package com.graphinout.reader.rdf;

import com.apicatalog.jsonld.document.JsonDocument;
import com.graphinout.base.fetch.RemoteContentPolicy;
import com.graphinout.base.fetch.RemoteDocument;
import com.graphinout.base.fetch.RemoteDocumentCache;
import com.graphinout.base.fetch.RemoteDocuments;
import com.graphinout.base.http.Hc5UrlFetcher;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Maintenance tool, not part of the library: re-fetches every bundled JSON-LD snapshot (through the same safe fetcher
 * readers use), and rewrites the files and their hashes, dates and content types in {@code index.txt}.
 * <p>
 * Usage: {@code JsonLdSnapshotRefresh <path to jsonld-snapshots folder>}. Review the diff before committing: a
 * changed hash means the published context changed.
 */
public class JsonLdSnapshotRefresh {

    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            System.err.println("Usage: JsonLdSnapshotRefresh <jsonld-snapshots folder>");
            System.exit(2);
        }
        Path folder = Path.of(args[0]);
        Path indexFile = folder.resolve("index.txt");
        List<String> out = new ArrayList<>();
        String today = LocalDate.now().toString();
        for (String line : Files.readAllLines(indexFile, StandardCharsets.UTF_8)) {
            if (line.isBlank() || line.strip().startsWith("#")) {
                out.add(line);
                continue;
            }
            JsonLdSnapshots.Entry e = JsonLdSnapshots.entries(line).getFirst();
            // a private cache: neither the old snapshots nor earlier fetches may answer
            RemoteDocuments remote = new RemoteDocuments(RemoteContentPolicy.PUBLIC_WEB,
                    new RemoteDocumentCache(64L * 1024 * 1024, Duration.ZERO), Hc5UrlFetcher::shared, Clock.systemUTC());
            RemoteDocument doc = JsonLdContextLoader.load(remote, e.urls().getFirst());
            JsonDocument.of(new ByteArrayInputStream(doc.body())); // must be JSON
            Files.write(folder.resolve(e.file()), doc.body());
            String contentType = doc.contentType() == null ? "application/ld+json" : doc.contentType().split(";")[0].strip();
            JsonLdSnapshots.Entry updated = new JsonLdSnapshots.Entry(e.file(), doc.sha256(), today, contentType, e.urls());
            System.out.printf("%-45s %8d B  %s%n", e.file(), doc.body().length, doc.describe());
            out.add(updated.toLine());
        }
        Files.writeString(indexFile, String.join("\n", out) + "\n", StandardCharsets.UTF_8);
    }
}

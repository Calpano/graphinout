package com.graphinout.reader.rdf;

import com.graphinout.base.fetch.RemoteDocumentCache;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Popular JSON-LD contexts bundled with this module (resource folder {@code jsonld-snapshots/}), so that most JSON-LD
 * files need no network access and read the same way every time.
 * <p>
 * {@code index.txt} lists one snapshot per line: {@code <file> <sha256> <retrieved> <content-type> <url> [<url>…]}.
 * Every URL on a line is served from the file. The hash pins the content: a mismatch fails loudly. Refresh with
 * {@code JsonLdSnapshotRefresh} (test sources); sources and licenses are in {@code NOTICE.txt}.
 */
public final class JsonLdSnapshots {

    static final String FOLDER = "jsonld-snapshots/";
    static final String INDEX = FOLDER + "index.txt";

    /** One line of the index. */
    public record Entry(String file, String sha256, String retrieved, String contentType, List<URI> urls) {

        String toLine() {
            StringBuilder sb = new StringBuilder(file).append(' ').append(sha256).append(' ').append(retrieved)
                    .append(' ').append(contentType);
            urls.forEach(u -> sb.append(' ').append(u));
            return sb.toString();
        }
    }

    private static volatile boolean registered;

    private JsonLdSnapshots() {}

    /** Registers all bundled snapshots in the shared {@link RemoteDocumentCache}, once. */
    public static void register() {
        if (registered) return;
        synchronized (JsonLdSnapshots.class) {
            if (registered) return;
            RemoteDocumentCache cache = RemoteDocumentCache.shared();
            for (Entry e : entries(resource(INDEX))) {
                byte[] body = bytes(FOLDER + e.file());
                for (URI url : e.urls()) {
                    cache.registerSnapshot(url, e.contentType(), body, e.sha256());
                }
            }
            registered = true;
        }
    }

    static List<Entry> entries(String index) {
        List<Entry> result = new ArrayList<>();
        for (String raw : index.split("\n")) {
            String line = raw.strip();
            if (line.isEmpty() || line.startsWith("#")) continue;
            String[] p = line.split("\\s+");
            if (p.length < 5) throw new IllegalStateException("Bad snapshot index line: " + line);
            List<URI> urls = new ArrayList<>();
            for (int i = 4; i < p.length; i++) urls.add(URI.create(p[i]));
            result.add(new Entry(p[0], p[1], p[2], p[3], List.copyOf(urls)));
        }
        return result;
    }

    private static String resource(String path) {
        return new String(bytes(path), StandardCharsets.UTF_8);
    }

    private static byte[] bytes(String path) {
        try (InputStream in = JsonLdSnapshots.class.getResourceAsStream(path)) {
            if (in == null) throw new IllegalStateException("Missing JSON-LD snapshot resource " + path);
            return in.readAllBytes();
        } catch (IOException e) {
            throw new IllegalStateException("Cannot read JSON-LD snapshot resource " + path, e);
        }
    }
}

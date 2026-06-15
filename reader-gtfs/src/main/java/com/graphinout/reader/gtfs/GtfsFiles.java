package com.graphinout.reader.gtfs;

import com.graphinout.base.input.InputSource;
import com.graphinout.base.input.MultiInputSource;
import com.graphinout.base.input.SingleInputSource;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Uniform access to the individual files of a GTFS feed, independent of how the feed is delivered:
 * <ul>
 *     <li>{@link SingleInputSource} → interpreted as a ZIP archive (the canonical GTFS distribution form).
 *     Files may live at the archive root or inside a single sub-directory.</li>
 *     <li>{@link MultiInputSource} → each GTFS file is a named source, e.g. {@code stops.txt}.</li>
 * </ul>
 */
interface GtfsFiles {

    static GtfsFiles of(InputSource inputSource) {
        if (inputSource.isSingle()) {
            return new ZipGtfsFiles((SingleInputSource) inputSource);
        }
        return new MultiGtfsFiles((MultiInputSource) inputSource);
    }

    String name();

    /**
     * @return a fresh UTF-8 reader on the given GTFS file (e.g. "stops.txt"), or null if the feed has no such file.
     *         Caller closes.
     */
    @Nullable Reader open(String fileName) throws IOException;

    class ZipGtfsFiles implements GtfsFiles {

        private final SingleInputSource source;

        ZipGtfsFiles(SingleInputSource source) {
            this.source = source;
        }

        @Override
        public String name() {
            return source.name();
        }

        @Override
        public @Nullable Reader open(String fileName) throws IOException {
            InputStream in = source.inputStream();
            ZipInputStream zip = new ZipInputStream(in);
            ZipEntry entry;
            boolean foundAnyEntry = false;
            while ((entry = zip.getNextEntry()) != null) {
                foundAnyEntry = true;
                if (entry.isDirectory()) continue;
                String name = entry.getName();
                if (name.equals(fileName) || name.endsWith("/" + fileName)) {
                    return new InputStreamReader(zip, StandardCharsets.UTF_8);
                }
            }
            zip.close();
            if (!foundAnyEntry) {
                throw new IOException("Input '" + source.name() + "' is not a ZIP archive (no entries found); GTFS feeds are ZIP files");
            }
            return null;
        }
    }

    class MultiGtfsFiles implements GtfsFiles {

        private final MultiInputSource source;

        MultiGtfsFiles(MultiInputSource source) {
            this.source = source;
        }

        @Override
        public String name() {
            return source.name();
        }

        @Override
        public @Nullable Reader open(String fileName) throws IOException {
            SingleInputSource single = source.getNamedSource(fileName);
            if (single == null && source.names() != null) {
                // tolerate names with a path prefix, e.g. "feed/stops.txt"
                for (String name : source.names()) {
                    if (name.endsWith("/" + fileName)) {
                        single = source.getNamedSource(name);
                        break;
                    }
                }
            }
            if (single == null) return null;
            return new InputStreamReader(single.inputStream(), StandardCharsets.UTF_8);
        }
    }
}

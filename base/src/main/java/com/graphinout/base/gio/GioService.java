package com.graphinout.base.gio;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The interface used in Java service lookup
 */
public interface GioService {

    /**
     * Whether a {@link GioFileFormat} can be read, written, or both by a {@link GioService}.
     */
    enum FormatSupport {
        Read, Write, ReadAndWrite
    }

    /**
     * Id helps debugging the service loader
     *
     * @return
     */
    String id();

    /**
     * @return all {@link GioReader}, which this service provides
     */
    List<GioReader> readers();

    /**
     * @return all {@link GioWriter} instances which this service provides.
     */
    List<GioWriter> writers();

    /**
     * Lists every {@link GioFileFormat} this service supports, mapped to whether the service can
     * {@link FormatSupport#Read}, {@link FormatSupport#Write}, or do both
     * ({@link FormatSupport#ReadAndWrite}) it. A reader and a writer are considered the same format
     * when their {@link GioFileFormat#id()} matches. Iteration order lists readers' formats first,
     * then any write-only formats.
     */
    default Map<GioFileFormat, FormatSupport> formats() {
        // keyed by format id to merge a reader and a writer of the same format (they are distinct instances)
        Map<String, GioFileFormat> formatById = new LinkedHashMap<>();
        Map<String, FormatSupport> supportById = new LinkedHashMap<>();
        for (GioReader reader : readers()) {
            GioFileFormat ff = reader.fileFormat();
            formatById.putIfAbsent(ff.id(), ff);
            supportById.put(ff.id(), FormatSupport.Read);
        }
        for (GioWriter writer : writers()) {
            GioFileFormat ff = writer.fileFormat();
            formatById.putIfAbsent(ff.id(), ff);
            supportById.merge(ff.id(), FormatSupport.Write, (existing, added) ->
                    existing == FormatSupport.Read || existing == FormatSupport.ReadAndWrite
                            ? FormatSupport.ReadAndWrite : FormatSupport.Write);
        }
        Map<GioFileFormat, FormatSupport> result = new LinkedHashMap<>();
        supportById.forEach((id, support) -> result.put(formatById.get(id), support));
        return result;
    }

}

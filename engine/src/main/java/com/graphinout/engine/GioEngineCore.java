package com.graphinout.engine;

import com.graphinout.base.cj.stream.CjStream2CjWriter;
import com.graphinout.base.cj.stream.ICjStream;
import com.graphinout.base.cj.writer.Cj2JsonWriter;
import com.graphinout.base.cj.writer.CjWriter2CjDocumentWriter;
import com.graphinout.base.gio.GioFileFormat;
import com.graphinout.base.gio.GioReader;
import com.graphinout.base.gio.GioService;
import com.graphinout.base.gio.GioWriter;
import com.graphinout.base.input.InputSource;
import com.graphinout.foundation.pure.json.writer.impl.Json2StringWriter;
import com.graphinout.base.output.OutputSink;
import com.graphinout.foundation.pure.xml.XML;
import com.graphinout.foundation.pure.xml.writer.Xml2StringWriter;
import com.graphinout.reader.cj.ConnectedJson5Reader;
import com.graphinout.reader.cj.ConnectedJsonReader;
import com.graphinout.reader.dot.DotReader;
import com.graphinout.reader.graphml.Graphml2XmlWriter;
import com.graphinout.reader.graphml.GraphmlReader;
import com.graphinout.reader.graphml.cj.CjDocument2Graphml;
import com.graphinout.reader.graphml.cj.CjStream2GraphmlWriter;
import com.graphinout.reader.jgrapht.Graph6Reader;
import com.graphinout.reader.textbased.adjlist.AdjListReader;
import com.graphinout.reader.tgf.TgfReader;
import com.graphinout.reader.tripletext.TripleTextReader;
import org.slf4j.Logger;

import org.jspecify.annotations.Nullable;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Core engine implementation for GraphInOut, managing readers, writers, and format services.
 */
public class GioEngineCore {

    private static final Logger log = getLogger(GioEngineCore.class);
    private final List<GioReader> readers = new ArrayList<>();
    private final List<GioWriter> writers = new ArrayList<>();
    @SuppressWarnings("unused") private final Map<String, GioService> services = new HashMap<>();

    public GioEngineCore() {
        loadServices();
    }

    /**
     * Checks if any registered reader can read the given resource path based on its file format.
     *
     * @param resourcePath the path or name of the resource to check.
     * @return true if a capable reader is found, false otherwise.
     */
    public boolean canRead(String resourcePath) {
        for (GioReader gioReader : readers()) {
            if (gioReader.fileFormat().matches(resourcePath)) {
                return true;
            }
        }
        return false;
    }

    @SuppressWarnings("UnnecessaryLocalVariable")
    @Deprecated
    public ICjStream createCjOutputStream(String outputFileFormatId, OutputSink outputSink) {
        switch (outputFileFormatId) {
            case ConnectedJsonReader.FORMAT_ID:
            case ConnectedJson5Reader.FORMAT_ID: {
                Json2StringWriter jsonWriter2 = new Json2StringWriter(json -> {
                    try {
                        outputSink.outputStream().write(json.getBytes(StandardCharsets.UTF_8));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
                Cj2JsonWriter cj2JsonWriter2 = new Cj2JsonWriter(jsonWriter2);
                CjStream2CjWriter cjStream2CjWriter = new CjStream2CjWriter(cj2JsonWriter2, true);
                return cjStream2CjWriter;
            }
            case GraphmlReader.FORMAT_ID: {
                Xml2StringWriter xml2StringWriter = new Xml2StringWriter(XML.AttributeOrderPerElement.Lexicographic, true, xml -> {
                    try {
                        outputSink.outputStream().write(xml.getBytes(StandardCharsets.UTF_8));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
                Graphml2XmlWriter graphml2XmlWriter = new Graphml2XmlWriter(xml2StringWriter);

                // cjStream to cjDocument
                CjWriter2CjDocumentWriter cj2ElementsWriter = new CjWriter2CjDocumentWriter((cjDoc) -> {
                    try {
                        CjDocument2Graphml.writeToGraphml(cjDoc, graphml2XmlWriter);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
                CjStream2CjWriter cjStream2CjWriter = new CjStream2CjWriter(cj2ElementsWriter, true);
                return cjStream2CjWriter;
            }
            case AdjListReader.FORMAT_ID:
            case DotReader.FORMAT_ID:
            case Graph6Reader.FORMAT_ID:
            case TgfReader.FORMAT_ID:
            case TripleTextReader.FORMAT_ID: {
                throw new IllegalArgumentException("no output writer exists for this format '" + outputFileFormatId + "'");
            }
        }
        throw new IllegalArgumentException("Unknown format id '" + outputFileFormatId + "'");
    }

    @Deprecated
    public ICjStream createCjStream(String outputFileFormatId, OutputSink outputSink) {
        switch (outputFileFormatId) {
            case ConnectedJsonReader.FORMAT_ID:
            case ConnectedJson5Reader.FORMAT_ID: {
                Json2StringWriter jsonWriter = new Json2StringWriter(json -> {
                    try {
                        outputSink.outputStream().write(json.getBytes(StandardCharsets.UTF_8));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
                Cj2JsonWriter cj2JsonWriter = new Cj2JsonWriter(jsonWriter);
                return new CjStream2CjWriter(cj2JsonWriter, true);
            }
            case GraphmlReader.FORMAT_ID: {
                Xml2StringWriter xml2StringWriter = new Xml2StringWriter(XML.AttributeOrderPerElement.Lexicographic, true, xml -> {
                    try {
                        outputSink.outputStream().write(xml.getBytes(StandardCharsets.UTF_8));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
                Graphml2XmlWriter graphml2XmlWriter = new Graphml2XmlWriter(xml2StringWriter);
                return new CjStream2GraphmlWriter(graphml2XmlWriter);
            }
            case AdjListReader.FORMAT_ID:
            case DotReader.FORMAT_ID:
            case Graph6Reader.FORMAT_ID:
            case TgfReader.FORMAT_ID:
            case TripleTextReader.FORMAT_ID: {
                throw new IllegalArgumentException("no output writer exists for this format '" + outputFileFormatId + "'");
            }
        }
        throw new IllegalArgumentException("Unknown format id '" + outputFileFormatId + "'");
    }

    /**
     * Lists every {@link GioFileFormat} known across all loaded services, mapped to whether the
     * engine can {@link GioService.FormatSupport#Read}, {@link GioService.FormatSupport#Write}, or
     * do both ({@link GioService.FormatSupport#ReadAndWrite}) it. A reader and a writer are
     * considered the same format when their {@link GioFileFormat#id()} matches. Iteration order
     * lists readers' formats first, then any write-only formats.
     */
    public Map<GioFileFormat, GioService.FormatSupport> formats() {
        // keyed by format id to merge a reader and a writer of the same format (they are distinct instances)
        Map<String, GioFileFormat> formatById = new HashMap<>();
        Map<String, GioService.FormatSupport> supportById = new LinkedHashMap<>();
        for (GioReader reader : readers) {
            GioFileFormat ff = reader.fileFormat();
            formatById.putIfAbsent(ff.id(), ff);
            supportById.put(ff.id(), GioService.FormatSupport.Read);
        }
        for (GioWriter writer : writers) {
            GioFileFormat ff = writer.fileFormat();
            formatById.putIfAbsent(ff.id(), ff);
            supportById.merge(ff.id(), GioService.FormatSupport.Write, (existing, added) ->
                    existing == GioService.FormatSupport.Read || existing == GioService.FormatSupport.ReadAndWrite
                            ? GioService.FormatSupport.ReadAndWrite : GioService.FormatSupport.Write);
        }
        Map<GioFileFormat, GioService.FormatSupport> result = new LinkedHashMap<>();
        supportById.forEach((id, support) -> result.put(formatById.get(id), support));
        return result;
    }

    /**
     * @return a stream of known default file formats supported by the engine.
     */
    public Stream<GioFileFormat> fileFormats() {
        return Stream.of( //
                ConnectedJsonReader.FORMAT, //
                ConnectedJson5Reader.FORMAT, //
                GraphmlReader.FORMAT, //
                AdjListReader.FORMAT, //
                DotReader.FORMAT, //
                Graph6Reader.FORMAT, //
                TgfReader.FORMAT, //
                TripleTextReader.FORMAT //
        );
    }

    /**
     * Gets a writer by its file format ID.
     *
     * @param fileFormatId the ID of the file format.
     * @return the corresponding writer, or null if not found.
     */
    public @Nullable GioWriter getWriter(String fileFormatId) {
        return writers.stream().filter(writer -> writer.fileFormat().id().equals(fileFormatId)).findFirst().orElse(null);
    }

    /**
     * Gets a reader by its file format ID.
     *
     * @param fileFormatId the ID of the file format.
     * @return the corresponding reader, or null if not found.
     */
    public @Nullable GioReader getReader(String fileFormatId) {
        return readers.stream().filter(writer -> writer.fileFormat().id().equals(fileFormatId)).findFirst().orElse(null);
    }

    /**
     * @return a list of all registered readers.
     */
    public List<GioReader> readers() {
        return readers;
    }

    /**
     * @return a map of all registered services by their ID.
     */
    public Map<String, GioService> services() {
        return services;
    }

    /**
     * @return a list of all registered writers.
     */
    public List<GioWriter> writers() {
        return writers;
    }

    private void loadServices() {
        ServiceLoader<GioService> serviceLoader = ServiceLoader.load(GioService.class);
        log.info("Load GioServices ...");
        for (GioService gioService : serviceLoader) {
            log.info("Found service '" + gioService.id() + "' in " + gioService.getClass().getCanonicalName());
            services.put(gioService.id(), gioService);
            for (GioReader reader : gioService.readers()) {
                readers.add(reader);
                log.info("  Found reader '" + reader.fileFormat().id() + "'");
            }
            for (GioWriter writer : gioService.writers()) {
                writers.add(writer);
                log.info("  Found writer '" + writer.fileFormat().id() + "'");
            }
        }
    }

    private List<GioReader> selectReaders(InputSource inputSource) throws IOException {
        List<GioReader> candidates = new ArrayList<>();
        // 1) based on file extension, if available, select possible readers
        for (GioReader r : readers) {
            if (r.fileFormat().matches(inputSource.name())) {
                candidates.add(r);
            }
        }
        if (candidates.isEmpty()) {
            candidates.addAll(readers);
        }

        // 2) ask EVERY reader we have / every candidate, if it can read the given inputSource
        return candidates.stream().filter(gioReader -> {
            try {
                return gioReader.isValid(inputSource);
            } catch (Exception e) {
                log.warn(e.getMessage());
                return false;
            }
        }).collect(Collectors.toList());
    }

}

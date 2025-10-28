package com.graphinout.engine;

import com.graphinout.base.GioService;
import com.graphinout.base.cj.stream.api.CjStream2CjWriter;
import com.graphinout.base.cj.stream.api.ICjStream;
import com.graphinout.base.cj.stream.impl.Cj2JsonWriter;
import com.graphinout.base.cj.stream.impl.CjWriter2CjDocumentWriter;
import com.graphinout.base.GioReader;
import com.graphinout.reader.graphml.Graphml2XmlWriter;
import com.graphinout.base.reader.GioFileFormat;
import com.graphinout.foundation.input.InputSource;
import com.graphinout.foundation.json.json5.Json5Reader;
import com.graphinout.foundation.json.stream.impl.Json2StringWriter;
import com.graphinout.foundation.output.OutputSink;
import com.graphinout.foundation.xml.XML;
import com.graphinout.foundation.xml.Xml2StringWriter;
import com.graphinout.reader.adjlist.AdjListReader;
import com.graphinout.reader.cj.ConnectedJson5Reader;
import com.graphinout.reader.cj.ConnectedJsonReader;
import com.graphinout.reader.graphml.GraphmlReader;
import com.graphinout.reader.graphml.cj.CjDocument2Graphml;
import com.graphinout.reader.graphml.cj.CjStream2GraphmlWriter;
import com.graphinout.reader.jgrapht.Graph6Reader;
import com.graphinout.reader.dot.DotReader;
import com.graphinout.reader.tgf.TgfReader;
import com.graphinout.reader.tripletext.TripleTextReader;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.slf4j.LoggerFactory.getLogger;

public class GioEngineCore {

    private static final Logger log = getLogger(GioEngineCore.class);
    private final List<GioReader> readers = new ArrayList<>();

    @SuppressWarnings("unused") private final Map<String, GioService> services = new HashMap<>();

    public GioEngineCore() {
        loadServices();
    }

    public boolean canRead(String resourcePath) {
        for (GioReader gioReader : readers()) {
            if (gioReader.fileFormat().matches(resourcePath)) {
                return true;
            }
        }
        return false;
    }

    @SuppressWarnings("UnnecessaryLocalVariable")
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
                CjStream2CjWriter cjStream2CjWriter = new CjStream2CjWriter(cj2JsonWriter2);
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
                CjStream2CjWriter cjStream2CjWriter = new CjStream2CjWriter(cj2ElementsWriter);
                return cjStream2CjWriter;
            }
            case AdjListReader.FORMAT_ID:
            case DotReader.FORMAT_ID:
            case Graph6Reader.FORMAT_ID:
            case Json5Reader.FORMAT_ID:
            case TgfReader.FORMAT_ID:
            case TripleTextReader.FORMAT_ID: {
                throw new IllegalArgumentException("no output writer exists for this format '" + outputFileFormatId + "'");
            }
        }
        throw new IllegalArgumentException("Unknown format id '" + outputFileFormatId + "'");
    }

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
                return new CjStream2CjWriter(cj2JsonWriter);
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
            case Json5Reader.FORMAT_ID:
            case TgfReader.FORMAT_ID:
            case TripleTextReader.FORMAT_ID: {
                throw new IllegalArgumentException("no output writer exists for this format '" + outputFileFormatId + "'");
            }
        }
        throw new IllegalArgumentException("Unknown format id '" + outputFileFormatId + "'");
    }

    public Stream<GioFileFormat> fileFormats() {
        return Stream.of( //
                ConnectedJsonReader.FORMAT, //
                ConnectedJson5Reader.FORMAT, //
                GraphmlReader.FORMAT, //
                AdjListReader.FORMAT, //
                DotReader.FORMAT, //
                Graph6Reader.FORMAT, //
                Json5Reader.FORMAT, //
                TgfReader.FORMAT, //
                TripleTextReader.FORMAT //
        );
    }

    public List<GioReader> readers() {
        return readers;
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

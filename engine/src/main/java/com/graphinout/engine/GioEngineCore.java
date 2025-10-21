package com.graphinout.engine;

import com.graphinout.base.CjStream2GioWriter;
import com.graphinout.base.Gio2CjWriter;
import com.graphinout.base.GioService;
import com.graphinout.base.cj.stream.api.CjStream2CjWriter;
import com.graphinout.base.cj.stream.api.ICjStream;
import com.graphinout.base.cj.stream.impl.Cj2ElementsWriter;
import com.graphinout.base.cj.stream.impl.Cj2JsonWriter;
import com.graphinout.base.gio.GioReader;
import com.graphinout.base.gio.GioWriter;
import com.graphinout.base.graphml.Graphml2XmlWriter;
import com.graphinout.base.graphml.gio.Gio2GraphmlWriter;
import com.graphinout.base.reader.ContentError;
import com.graphinout.base.reader.ContentErrors;
import com.graphinout.base.reader.GioFileFormat;
import com.graphinout.base.reader.InMemoryErrorHandler;
import com.graphinout.foundation.input.InputSource;
import com.graphinout.foundation.input.SingleInputSource;
import com.graphinout.foundation.json.json5.Json5Reader;
import com.graphinout.foundation.json.stream.impl.Json2StringWriter;
import com.graphinout.foundation.output.OutputSink;
import com.graphinout.foundation.xml.XML;
import com.graphinout.foundation.xml.Xml2AppendableWriter;
import com.graphinout.foundation.xml.Xml2StringWriter;
import com.graphinout.foundation.xml.XmlWriterImpl;
import com.graphinout.reader.adjlist.AdjListReader;
import com.graphinout.reader.cj.ConnectedJson5Reader;
import com.graphinout.reader.cj.ConnectedJsonReader;
import com.graphinout.reader.example.TripleTextReader;
import com.graphinout.reader.graphml.GraphmlReader;
import com.graphinout.reader.graphml.cj.CjDocument2Graphml;
import com.graphinout.reader.jgrapht.Graph6Reader;
import com.graphinout.reader.jgrapht.dot.DotReader;
import com.graphinout.reader.tgf.TgfReader;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.OutputStream;
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

    @Deprecated
    public GioWriter createGioWriter(String outputFileFormatId, OutputSink outputSink) {
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
                Gio2CjWriter gioWriter = new Gio2CjWriter(cj2JsonWriter2);
                return gioWriter;
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
                Gio2GraphmlWriter gioWriter = new Gio2GraphmlWriter(graphml2XmlWriter);
                return gioWriter;
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
                Cj2ElementsWriter cj2ElementsWriter = new Cj2ElementsWriter( (cjDoc)->{
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

    // FIXME this is unfinished
    public void read(SingleInputSource inputSource, OutputSink outputSink) throws IOException {
        List<GioReader> readerCandidates = selectReaders(inputSource);
        // we expect only one of them to be able to read it without throwing exceptions or reporting
        // contentErrors
        GioWriter gioWriter = new Gio2GraphmlWriter(new Graphml2XmlWriter(Xml2AppendableWriter.createNoop()));
        for (GioReader reader : readerCandidates) {
            InMemoryErrorHandler errorHandler = ContentErrors.createInMemory();
            reader.errorHandler(errorHandler);
            if (reader.isValid(inputSource)) {
                reader.read(inputSource, gioWriter);
            }
            if (errorHandler.hasNoErrors()) {
                // parsing worked, now for real
                gioWriter = new Gio2GraphmlWriter(new Graphml2XmlWriter(Xml2AppendableWriter.createNoop()));
                // FIXME now parse again
                return;
            }
        }
        throw new IllegalStateException("Could not find any parser able to parse " + inputSource.name());
    }

    /**
     * Read the input, transform to GraphML and write result to resultOut. All content errors are written to logOut.
     */
    public void read(InputSource in, OutputStream resultOut, OutputStream logOut) throws IOException {
        GioWriter gioWriter = createGioWriter(resultOut);
        List<GioReader> availableService;
        try {
            availableService = new ArrayList<>(selectReaders(in));
        } catch (IOException e) {
            throw new RuntimeException("Error reading input", e);
        }
        availableService.forEach(gioReader -> {
            List<ContentError> contentErrors = new ArrayList<>();
            gioReader.errorHandler(contentErrors::add);
            try {
                gioReader.read(in, gioWriter);
            } catch (IOException e) {
                throw new RuntimeException(e);
            } finally {
                contentErrors.forEach(contentError -> {
                    try {
                        logOut.write(contentError.toString().getBytes());
                        logOut.write("\n".getBytes());
                    } catch (IOException e) {
                        log.error("Failed reporting contentError", e);
                    }
                });
            }
        });
    }

    public List<GioReader> readers() {
        return readers;
    }

    public void validate(InputSource in, OutputStream logOut) {
        // ....
    }

    private GioWriter createGioWriter(OutputStream resultOut) throws IOException {
        // FIXME create depending on desired output format
        return new Gio2GraphmlWriter(new Graphml2XmlWriter(XmlWriterImpl.create(new OutputSink() {
            @Override
            public void close() throws Exception {
                resultOut.flush();
                resultOut.close();
            }

            @Override
            public OutputStream outputStream() throws IOException {
                return resultOut;
            }
        })));
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
        // Fixed bug management exception handling to check other providers
        //        Iterator<GioReader> it = candidates.iterator();
        //        while (it.hasNext()) {
        //            GioReader reader = it.next();
        //
        //            if (!reader.isValid(inputSource)) {
        //                it.remove();
        //            }
        //        }
        //
        //        return candidates;
    }

}

package com.calpano.graphinout.engine;

import com.calpano.graphinout.base.GioService;
import com.calpano.graphinout.base.gio.GioWriter;
import com.calpano.graphinout.base.gio.GioWriterImpl;
import com.calpano.graphinout.base.graphml.GraphmlWriterImpl;
import com.calpano.graphinout.base.input.SingleInputSource;
import com.calpano.graphinout.base.output.OutputSink;
import com.calpano.graphinout.base.xml.XmlWriterImpl;
import com.calpano.graphinout.base.reader.ContentErrors;
import com.calpano.graphinout.base.reader.GioReader;
import com.calpano.graphinout.base.reader.InMemoryErrorHandler;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

import static org.slf4j.LoggerFactory.getLogger;

public class GioEngineCore {

    private static final Logger log = getLogger(GioEngineCore.class);
    private final List<GioReader> readers = new ArrayList<>();
    private final Map<String, GioService> services = new HashMap<>();

    public GioEngineCore() {
        loadServices();
    }

    public void read(SingleInputSource inputSource, OutputSink outputSink) throws IOException {
        List<GioReader> readerCandidates = selectReaders(inputSource);
        // we expect only one of them to be able to read it without throwing exceptions or reporting contentErrors
        OutputSink noop = OutputSink.createNoop();
        GioWriter gioWriter = new GioWriterImpl(new GraphmlWriterImpl(new XmlWriterImpl(noop)));
        for (GioReader reader : readerCandidates) {
            InMemoryErrorHandler errorHandler = ContentErrors.createInMemory();
            reader.errorHandler(errorHandler);
            if (reader.isValid(inputSource)) {
                reader.read(inputSource, gioWriter);
            }
            if (errorHandler.hasNoErrors()) {
                // parsing worked, now for real
                gioWriter = new GioWriterImpl(new GraphmlWriterImpl(new XmlWriterImpl(outputSink)));
                return;
            }
        }
        throw new IllegalStateException("Could not find any parser able to parse " + inputSource.name());
    }

    public List<GioReader> readers() {
        return readers;
    }

    private void loadServices() {
        ServiceLoader<GioService> serviceLoader = ServiceLoader.load(GioService.class);
        log.info("Load GioServices ...");
        for (GioService gioService : serviceLoader) {
            log.info("Found service '" + gioService.id() + "' in "+gioService.getClass().getCanonicalName());
            services.put(gioService.id(), gioService);
            for (GioReader reader : gioService.readers()) {
                readers.add(reader);
                log.info("  Found reader '" + reader.fileFormat().id() + "'");
            }
        }
    }

    private List<GioReader> selectReaders(SingleInputSource inputSource) {
        // find reader candidates based on inputSource.inputFormat() and reader.fileFormat()
        // TODO let fileFormat have registered list of extensions and use for matching
        // fake it
        return readers;
    }

}

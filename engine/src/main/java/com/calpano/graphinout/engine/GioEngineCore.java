package com.calpano.graphinout.engine;

import com.calpano.graphinout.base.GioService;
import com.calpano.graphinout.base.gio.GioWriter;
import com.calpano.graphinout.base.gio.GioWriterImpl;
import com.calpano.graphinout.base.graphml.GraphmlWriterImpl;
import com.calpano.graphinout.base.input.InputSource;
import com.calpano.graphinout.base.input.MultiInputSource;
import com.calpano.graphinout.base.input.SingleInputSource;
import com.calpano.graphinout.base.output.OutputSink;
import com.calpano.graphinout.base.reader.ContentError;
import com.calpano.graphinout.base.reader.ContentErrors;
import com.calpano.graphinout.base.reader.GioReader;
import com.calpano.graphinout.base.reader.InMemoryErrorHandler;
import com.calpano.graphinout.base.xml.XmlWriterImpl;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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

    public boolean canRead(String resourcePath) {
        for (GioReader gioReader : readers()) {
            if (gioReader.fileFormat().matches(resourcePath)) {
                return true;
            }
        }
        return false;
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
            log.info("Found service '" + gioService.id() + "' in " + gioService.getClass().getCanonicalName());
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

    /**
     * Read the input, transform to GraphML and write result to resultOut. All content errors are written to logOut.
     */
    public void read(InputSource in, String inputType,  OutputStream resultOut, OutputStream logOut ) {
        GioWriter gioWriter = createGioWriter(resultOut);
        List<GioReader> availableService = new ArrayList<>();
        if(in.isSingle()){
availableService.addAll( selectReaders((SingleInputSource) in));

        } else if(in.isMulti()){
            MultiInputSource multiInputSource = (MultiInputSource) in;
         //TODO We need information about file titles
        //    SingleInputSource dataSource = multiInputSource.getNamedSource(DATA);
        //    SingleInputSource mappingSource = multiInputSource.getNamedSource(MAPPING);
            //    availableService.addAll( dataSource));
        }


        availableService.forEach(gioReader ->
        {
            List<ContentError> contentErrors = new ArrayList<>();
            gioReader.errorHandler(contentErrors::add);
            try {
                gioReader.read(in,gioWriter);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }finally{
                contentErrors.forEach(contentError -> {
                    try {
                        logOut.write(contentError.toString().getBytes());
                        logOut.write("\n".getBytes());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
                );
            }
        });
    }

    private GioWriter createGioWriter( OutputStream resultOut){
      return new GioWriterImpl(new GraphmlWriterImpl(new XmlWriterImpl(new OutputSink() {
          @Override
          public OutputStream outputStream() throws IOException {
              return resultOut;
          }

          @Override
          public void close() throws Exception {
               resultOut.flush();
               resultOut.close();
          }
      } )));
    }

    public void validate(InputSource in, OutputStream logOut ) {
        // ....
    }


}

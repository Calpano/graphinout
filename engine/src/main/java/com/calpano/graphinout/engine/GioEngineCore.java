package com.calpano.graphinout.engine;

import static org.slf4j.LoggerFactory.getLogger;

import com.calpano.graphinout.base.GioService;
import com.calpano.graphinout.base.gio.GioWriter;
import com.calpano.graphinout.base.gio.GioWriterImpl;
import com.calpano.graphinout.base.graphml.GraphmlWriterImpl;
import com.calpano.graphinout.base.input.InputSource;
import com.calpano.graphinout.base.input.SingleInputSource;
import com.calpano.graphinout.base.output.OutputSink;
import com.calpano.graphinout.base.reader.ContentError;
import com.calpano.graphinout.base.reader.ContentErrors;
import com.calpano.graphinout.base.reader.GioReader;
import com.calpano.graphinout.base.reader.InMemoryErrorHandler;
import com.calpano.graphinout.base.xml.XmlWriterImpl;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.stream.Collectors;
import org.slf4j.Logger;

public class GioEngineCore {

  private static final Logger log = getLogger(GioEngineCore.class);
  private final List<GioReader> readers = new ArrayList<>();

  @SuppressWarnings("unused")
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
    // we expect only one of them to be able to read it without throwing exceptions or reporting
    // contentErrors
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
    throw new IllegalStateException(
        "Could not find any parser able to parse " + inputSource.name());
  }

  /**
   * Read the input, transform to GraphML and write result to resultOut. All content errors are
   * written to logOut.
   */
  public void read(InputSource in, String inputType, OutputStream resultOut, OutputStream logOut) {
    GioWriter gioWriter = createGioWriter(resultOut);
    List<GioReader> availableService;
    try {
      availableService = new ArrayList<>(selectReaders(in));
    } catch (IOException e) {
      throw new RuntimeException("Error reading input", e);
    }
    availableService.forEach(
        gioReader -> {
          List<ContentError> contentErrors = new ArrayList<>();
          gioReader.errorHandler(contentErrors::add);
          try {
            gioReader.read(in, gioWriter);
          } catch (IOException e) {
            throw new RuntimeException(e);
          } finally {
            contentErrors.forEach(
                contentError -> {
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

  private GioWriter createGioWriter(OutputStream resultOut) {
    return new GioWriterImpl(
        new GraphmlWriterImpl(
            new XmlWriterImpl(
                new OutputSink() {
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
      log.info(
          "Found service '" + gioService.id() + "' in " + gioService.getClass().getCanonicalName());
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
    return candidates.stream()
        .filter(
            gioReader -> {
              try {
              return   gioReader.isValid(inputSource);

              } catch (Exception e) {
                  log.warn(e.getMessage());
                return false;
              }
            })
        .collect(Collectors.toList());
      //Fixed bug management exception handling to check other providers
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

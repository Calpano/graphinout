package com.calpano.graphinout.apprestservice.utility;

import com.calpano.graphinout.base.input.InputSource;
import com.calpano.graphinout.base.output.OutputSink;
import com.calpano.graphinout.filemanagment.IOResource;
import com.calpano.graphinout.filemanagment.LoadInputSource;
import com.calpano.graphinout.filemanagment.SaveOutputSink;
import jakarta.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
//TODO This class requires code review
@Component
public class FileManager {

  private final SaveOutputSink saveOutputSink;
  private final LoadInputSource loadInputSource;

  public FileManager(SaveOutputSink saveOutputSink, LoadInputSource loadInputSource) {
    this.saveOutputSink = saveOutputSink;
    this.loadInputSource = loadInputSource;
  }

  public String save(MultipartFile multipartFile, Optional<String> sessionID) throws IOException {
    InputStream initialStream = multipartFile.getInputStream();
    byte[] buffer = new byte[initialStream.available()];
    initialStream.read(buffer);

    File targetFile = new File("src/main/resources/targetFile.tmp");
    OutputStream outStream = new FileOutputStream(targetFile);
    outStream.write(buffer);

    return save(outStream, sessionID);
  }

  public String save(final OutputStream outputStream, Optional<String> sessionID)
      throws IOException {

    return save(() -> outputStream, sessionID);
  }

  public String save(OutputSink outputSink, Optional<String> sessionID) throws IOException {
    String newSessionID =
        sessionID.isPresent() && !sessionID.get().isEmpty()
            ? sessionID.get()
            : UUID.randomUUID().toString();
    saveOutputSink.save(new IOResource<>(outputSink), newSessionID);
    return newSessionID;
  }

  public IOResource<InputSource> loadInputSource(String sessionID) throws IOException {
    return loadInputSource.load(sessionID);
  }

  public StreamingResponseBody zipAll(final HttpServletResponse response, String sessionID) {
    return new StreamingResponseBody() {
      @Override
      public void writeTo(OutputStream outputStream) throws IOException {
            //TODO load data
      }
    };
  }

  private StreamingResponseBody zipAll(
      final HttpServletResponse response, final OutputStream graphML, final OutputStream events) {
    return out -> {
      final ZipOutputStream zipOut = new ZipOutputStream(response.getOutputStream());

      final ZipEntry graphml = new ZipEntry("graphml.xml");
      zipOut.putNextEntry(graphml);
      zipOut.write(graphML.toString().getBytes(StandardCharsets.UTF_8));
      final ZipEntry event = new ZipEntry("event.log");
      zipOut.putNextEntry(event);
      zipOut.write(events.toString().getBytes(StandardCharsets.UTF_8));
      zipOut.close();
    };
  }
}

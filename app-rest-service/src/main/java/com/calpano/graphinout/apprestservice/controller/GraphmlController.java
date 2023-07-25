package com.calpano.graphinout.apprestservice.controller;

import com.calpano.graphinout.base.input.FileSingleInputSource;
import com.calpano.graphinout.base.output.InMemoryOutputSink;
import com.calpano.graphinout.engine.GioEngineCore;
import jakarta.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@RestController
@RequestMapping(
    value = "/api",
    produces = "application/zip",
    consumes = {
      MediaType.APPLICATION_JSON_VALUE,
      MediaType.APPLICATION_XML_VALUE,
      MediaType.TEXT_PLAIN_VALUE
    })
@Slf4j
public class GraphmlController {

  @PostMapping("/validate")
  @ResponseBody
  public ResponseEntity<StreamingResponseBody> validate(
      @RequestParam(required = true, name = "file") MultipartFile file,
      @RequestParam("type") String fileType,
      HttpServletResponse response,
      RedirectAttributes redirectAttributes) {

    // TODO
    redirectAttributes.addFlashAttribute(
        "message", "You successfully uploaded " + file.getOriginalFilename() + "!");

    try {
      return ResponseEntity.ok(generateResult(response, file));
    } catch (IOException ioe) {
      log.error(ioe.getMessage(), ioe);
      return new ResponseEntity("Internal Error", HttpStatus.BAD_REQUEST);
    }
  }

  @PostMapping("/read")
  @ResponseBody
  public ResponseEntity<StreamingResponseBody> read(
      @RequestParam(required = true, name = "file") MultipartFile file,
      @RequestParam("type") String fileType,
      HttpServletResponse response,
      RedirectAttributes redirectAttributes) {

    // TODO
    redirectAttributes.addFlashAttribute(
        "message", "You successfully uploaded " + file.getOriginalFilename() + "!");
    try {
      return ResponseEntity.ok(generateResult(response, file));
    } catch (IOException ioe) {
      log.error(ioe.getMessage(), ioe);
      return new ResponseEntity("Internal Error", HttpStatus.BAD_REQUEST);
    }
  }

  /**
   * Create compressed(zip) file by StreamingResponseBody whose Content is result.graphml and
   * log.txt
   *
   * @param response : May be required to generate output
   * @return : StreamingResponseBody
   */
  private StreamingResponseBody generateResult(HttpServletResponse response, MultipartFile file)
      throws IOException {
    try (FileSingleInputSource fileSingleInputSource =
        new FileSingleInputSource(file.getResource().getFile())) {
      final GioEngineCore gioEngineCore = new GioEngineCore();
      //TODO need  to access ContentError's contents
      final InMemoryOutputSink outputSink = new InMemoryOutputSink();
      gioEngineCore.read(fileSingleInputSource, outputSink);
      StreamingResponseBody stream =
          zipAll(response, outputSink.outputStream(), outputSink.outputStream());
      return stream;
    }
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

package com.calpano.graphinout.apprestservice.controller;

import com.calpano.graphinout.apprestservice.service.GraphmlReaderService;
import com.calpano.graphinout.apprestservice.utility.FileManager;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

@RestController
@RequestMapping(
    value = "/api"
    //        ,
    //    produces = {GioMediaType.APPLICATION_ZIP_VALUE, MediaType.TEXT_PLAIN_VALUE},
    //    consumes = {
    //      MediaType.ALL_VALUE
    ////            ,
    ////      MediaType.APPLICATION_XML_VALUE,
    ////      MediaType.TEXT_PLAIN_VALUE,
    ////      MediaType.MULTIPART_FORM_DATA_VALUE
    //    }
    )
@Slf4j
@RequiredArgsConstructor
public class GraphmlController {

  private final GraphmlReaderService graphmlReaderService;

  private final FileManager fileManager;

  @GetMapping(value = "/status", produces = MediaType.TEXT_PLAIN_VALUE)
  public ResponseEntity<String> status() {
    final HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.setContentType(MediaType.TEXT_PLAIN);
    return new ResponseEntity<String>("OK", httpHeaders, HttpStatus.OK);
  }

  @PostMapping(value = "/read/{inputType}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @ResponseBody
  public ResponseEntity<StreamingResponseBody> read(
      @PathVariable("inputType") String inputType,
      @RequestParam(required = true, name = "data-file") MultipartFile dataFile, //
      @RequestParam(required = false, name = "format-file") MultipartFile formatFile, //
      HttpServletResponse response) {

    try {
      return ResponseEntity.ok(generateResult(response, dataFile, Optional.of(formatFile)));
    } catch (IOException ioe) {
      log.error(ioe.getMessage(), ioe);
      return new ResponseEntity("Internal Error", HttpStatus.BAD_REQUEST);
    }
  }

  @PostMapping(value = "/validate/{inputType}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @ResponseBody
  public ResponseEntity<StreamingResponseBody> validate(
      @PathVariable("inputType") String inputType,
      @RequestParam(required = true, name = "data-file") MultipartFile dataFile, //
      @RequestParam(required = false, name = "format-file") MultipartFile formatFile, //
      HttpServletResponse response) {

    try {
      final HttpHeaders httpHeaders = new HttpHeaders();
      httpHeaders.setContentType(MediaType.TEXT_PLAIN);
      return new ResponseEntity<StreamingResponseBody>(
          generateResult(response, dataFile, Optional.of(formatFile)), httpHeaders, HttpStatus.OK);

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
  private StreamingResponseBody generateResult(
      HttpServletResponse response, MultipartFile dataFile, Optional<MultipartFile> formatFile)
      throws IOException {
    String sessionID = saveNewRequest(dataFile, formatFile);
    graphmlReaderService.read(sessionID);
    StreamingResponseBody stream = fileManager.zipAll(response, sessionID);
    return stream;
  }

  private String saveNewRequest(MultipartFile dataFile, Optional<MultipartFile> formatFile)
      throws IOException {
    String sessionID = fileManager.save(dataFile, Optional.empty());

    if (formatFile.isPresent()) fileManager.save(formatFile.get(), sessionID.describeConstable());

    return sessionID;
  }

  public void validate(InputStream in, String optionalInputId) {
    // redirect to /session/...
  }

  public void validateInputResult(String inputId, OutputStream resultOut) {}

  public void validateInputContentErrors(String inputId, OutputStream logOut) {}
}

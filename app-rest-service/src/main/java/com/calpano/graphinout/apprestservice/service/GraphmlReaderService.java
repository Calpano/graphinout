package com.calpano.graphinout.apprestservice.service;

import com.calpano.graphinout.apprestservice.utility.FileManager;
import com.calpano.graphinout.base.input.InputSource;
import com.calpano.graphinout.base.output.OutputSink;
import com.calpano.graphinout.engine.GioEngineCore;
import com.calpano.graphinout.filemanagment.IOResource;
import com.calpano.graphinout.filemanagment.LoadInputSource;
import com.calpano.graphinout.filemanagment.SaveOutputSink;
import com.calpano.graphinout.filemanagment.Type;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class GraphmlReaderService {
  private final FileManager fileManager;

  public void read(String inputId) throws IOException {
    IOResource<InputSource> inputSource = fileManager.loadInputSource(inputId);
    ByteArrayOutputStream result = new ByteArrayOutputStream();
    ByteArrayOutputStream log = new ByteArrayOutputStream();
    read(inputSource, result, log);

    fileManager.save(result,inputId.describeConstable());
    fileManager.save(log,inputId.describeConstable());

  }

  private void read(IOResource<InputSource> in, OutputStream result, OutputStream log) {
    GioEngineCore gioEngineCore = new GioEngineCore();
    gioEngineCore.read(in.getResource(), result, log);
  }
}

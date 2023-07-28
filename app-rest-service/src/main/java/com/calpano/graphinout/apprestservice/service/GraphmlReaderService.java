package com.calpano.graphinout.apprestservice.service;

import com.calpano.graphinout.base.input.InputSource;
import com.calpano.graphinout.engine.GioEngineCore;
import com.calpano.graphinout.filemanagment.IOResource;
import com.calpano.graphinout.filemanagment.LoadFile;
import com.calpano.graphinout.filemanagment.SaveFile;
import java.io.IOException;
import java.io.OutputStream;
import java.io.ByteArrayOutputStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GraphmlReaderService {

  @Autowired private LoadFile loadFile;
  @Autowired private SaveFile saveFile;

  public void read(String inputId) throws IOException {
    IOResource<InputSource> inputSource = loadFile.loadFile(inputId);

    ByteArrayOutputStream result = new ByteArrayOutputStream();
    ByteArrayOutputStream log = new ByteArrayOutputStream();
    read(inputSource, result, log);
    saveFile.saveFile(new IOResource<OutputStream>(result, "xml"), inputId);
    saveFile.saveFile(new IOResource<OutputStream>(log, "txt"), inputId);
  }

  private void read(IOResource<InputSource> in, OutputStream result, OutputStream log) {
    GioEngineCore gioEngineCore = new GioEngineCore();
    gioEngineCore.read(in.getResource(), in.getType(), result, log);
  }
}

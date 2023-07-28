package com.calpano.graphinout.apprestservice.service;

import com.calpano.graphinout.base.input.InputSource;
import com.calpano.graphinout.base.output.OutputSink;
import com.calpano.graphinout.engine.GioEngineCore;
import com.calpano.graphinout.filemanagment.IOResource;
import com.calpano.graphinout.filemanagment.LoadInputSource;
import com.calpano.graphinout.filemanagment.SaveOutputSink;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

@Service
public class GraphmlReaderService {

    @Autowired
    private LoadInputSource loadInputSource;
    @Autowired
    private SaveOutputSink saveOutputSink;

    public void read(String inputId) throws IOException {
        IOResource<InputSource> inputSource = loadInputSource.load(inputId);

        ByteArrayOutputStream result = new ByteArrayOutputStream();
        ByteArrayOutputStream log = new ByteArrayOutputStream();
        read(inputSource, result, log);
        saveOutputSink.save(new IOResource<OutputSink>(OutputSink.createInMemory(result), "xml"), inputId);
        saveOutputSink.save(new IOResource<OutputSink>(OutputSink.createInMemory(log), "txt"), inputId);
    }

    private void read(IOResource<InputSource> in, OutputStream result, OutputStream log) {
        GioEngineCore gioEngineCore = new GioEngineCore();
        gioEngineCore.read(in.getResource(), in.getType(), result, log);
    }
}

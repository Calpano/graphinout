package com.graphinout.reader.example;

import com.graphinout.base.cj.stream.ICjStream;
import com.graphinout.base.gio.GioFileFormat;
import com.graphinout.base.gio.GioWriter;
import com.graphinout.base.output.OutputSink;

public class ExampleWriter implements GioWriter {

    @Override
    public ICjStream createCjStream(OutputSink outputSink) {
        return null;
    }

    @Override
    public GioFileFormat fileFormat() {
        return ExampleReader.FORMAT;
    }

}

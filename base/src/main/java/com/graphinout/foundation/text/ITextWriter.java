package com.graphinout.foundation.text;

import com.graphinout.foundation.output.OutputSink;

public interface ITextWriter {

    void line(String line);

    static ITextWriter onOutputSink(OutputSink outputSink) {
        return new TextWriterOnWriter(outputSink.writerUtf8());
    }

}

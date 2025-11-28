package com.graphinout.foundation.text;

import com.graphinout.foundation.output.OutputSink;

public interface ITextWriter {

    static TextWriterOnWriter onOutputSink(OutputSink outputSink) {
        return new TextWriterOnWriter(outputSink);
    }

    void line(String line);

}

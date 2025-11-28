package com.graphinout.base.text;

import com.graphinout.base.output.OutputSink;

public interface ITextWriter {

    static TextWriterOnWriter onOutputSink(OutputSink outputSink) {
        return new TextWriterOnWriter(outputSink);
    }

    void line(String line);

}

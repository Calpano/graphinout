package com.graphinout.base.text;

import com.graphinout.base.output.OutputSink;

/**
 * A sink that receives text one line at a time.
 */
public interface ITextWriter {

    static TextWriterOnWriter onOutputSink(OutputSink outputSink) {
        return new TextWriterOnWriter(outputSink);
    }

    void line(String line);

}

package com.calpano.graphinout.base.output;

import java.io.IOException;
import java.io.OutputStream;

public class NoopOutputSink implements OutputSink {

    @Override
    public OutputStream outputStream() throws IOException {
        return new OutputStream() {
            @Override
            public void write(int b) throws IOException {
                // no-op
            }
        };
    }


}

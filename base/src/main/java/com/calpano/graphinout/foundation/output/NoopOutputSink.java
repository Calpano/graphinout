package com.calpano.graphinout.foundation.output;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Stack;

public class NoopOutputSink implements OutputSink {

    private static final Logger log = LoggerFactory.getLogger(NoopOutputSink.class);


    private Stack<OutputStream> outputStreams = new Stack<>();

    @Override
    public OutputStream outputStream() throws IOException {
        outputStreams.push(new OutputStream() {
            @Override
            public void write(int b) throws IOException {
                // no-op
            }
        });
        return outputStreams.peek();
    }

    @Override
    public void close() throws Exception {
        while (!outputStreams.isEmpty()) {
            log.debug("Closed OutputSink  <NoopOutputSink> type <{}>.", outputStreams.peek().getClass().getName());
            outputStreams.pop().close();
        }
    }
}

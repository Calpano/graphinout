package com.calpano.graphinout.base.output;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Stack;

@Slf4j
public class NoopOutputSink implements OutputSink {


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

package com.calpano.graphinout.base.output;

import com.calpano.graphinout.base.input.MultiInputSource;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.util.List;
import java.util.Map;

public interface OutputSink {

    static InMemoryOutputSink createInMemory() {
        return new InMemoryOutputSink();
    }

    static OutputSink createMock() {
        return new OutputSink() {
            @Override
            public OutputStream outputStream() throws IOException {
                return System.out;
            }

            @Override
            public List<String> readAllData() throws IOException {
                return null;
            }

            @Override
            public Map<String, Object> outputInfo() {
                return null;
            }
        };
    }

    static OutputSink createNoop() {
        return new OutputSink(){
            @Override
            public OutputStream outputStream() throws IOException {
                return new OutputStream() {
                    @Override
                    public void write(int b) throws IOException {
                        // no-op
                    }
                };
            }

            @Override
            public List<String> readAllData() throws IOException {
                return null;
            }

            @Override
            public Map<String, Object> outputInfo() {
                return null;
            }
        };
    }

    /** can be called once. Users need to close {@link OutputStream after usage */
    OutputStream outputStream() throws IOException;

    @Deprecated
    List<String> readAllData() throws IOException;

    @Deprecated
    Map<String, Object> outputInfo() ;

}

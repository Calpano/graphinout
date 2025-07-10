package com.calpano.graphinout.foundation.output;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public interface OutputSink extends AutoCloseable {

    static InMemoryOutputSink createInMemory() {
        return new InMemoryOutputSink();
    }

    static InMemoryOutputSink createInMemory(ByteArrayOutputStream bos) {
        return new InMemoryOutputSink(bos);
    }


    static OutputSink createNoop() {
        return new NoopOutputSink();
    }

    default void close() throws Exception {outputStream().close();}

    /**
     * Can be called once. Users need to close {@link OutputStream} after usage.
     * <p>
     * Never return System.out here. We will close it, causing issues in IntelliJ testing.
     */
    OutputStream outputStream() throws IOException;

}

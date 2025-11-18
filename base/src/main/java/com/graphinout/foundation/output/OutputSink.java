package com.graphinout.foundation.output;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;

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

    /**
     * Write a string to out and close stream.
     * @param string
     * @throws IOException
     */
    default void write(String string) throws IOException {
        try (OutputStream out = outputStream()) {
            out.write(string.getBytes(StandardCharsets.UTF_8));
        }
    }

    /**
     * Users need to close {@link Writer} after usage.
     *
     * @return a writer on this {@link OutputSink}
     */
    default Writer writerUtf8() {
        try (OutputStream out = outputStream(); OutputStreamWriter writer = new OutputStreamWriter(out, StandardCharsets.UTF_8)) {
            return writer;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}

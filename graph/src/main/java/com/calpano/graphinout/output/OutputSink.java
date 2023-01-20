package com.calpano.graphinout.output;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Optional;

public interface OutputSink {

    /** can be called once. Users need to close {@link OutputStream after usage */
    OutputStream outputStream() throws IOException;

}

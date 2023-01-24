package com.calpano.graphinout.base.input;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Optional;

public interface InputSource {

    /** can be called repeatedly, but users need to close {@link InputStream after each usage */
    Optional<Charset> encoding();

    Optional<String> inputFormat();

    InputStream inputStream() throws IOException;

    /**
     * e.g. the canonical filename
     */
    String name();
}

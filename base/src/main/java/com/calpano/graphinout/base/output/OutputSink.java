package com.calpano.graphinout.base.output;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

public interface OutputSink {

    /** can be called once. Users need to close {@link OutputStream after usage */
    OutputStream outputStream() throws IOException;

    List<String> readAllData() throws IOException;

    Map<String, Object> outputInfo() ;

}

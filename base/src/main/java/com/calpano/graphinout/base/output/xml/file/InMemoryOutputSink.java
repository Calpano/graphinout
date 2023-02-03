package com.calpano.graphinout.base.output.xml.file;

import com.calpano.graphinout.base.output.OutputSink;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InMemoryOutputSink implements OutputSink {

    private final ByteArrayOutputStream buffer ;

    public InMemoryOutputSink() {

        buffer =  new ByteArrayOutputStream();
    }

    @Override
    public OutputStream outputStream() throws IOException {
        return buffer;
    }

    @Override
    public List<String> readAllData() throws IOException {
        return Collections.singletonList(buffer.toString());
    }

    @Override
    public Map<String, Object> outputInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("type","ByteArrayOutputStream");
        return info;
    }


}

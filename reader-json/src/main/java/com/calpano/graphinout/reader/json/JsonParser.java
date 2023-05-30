package com.calpano.graphinout.reader.json;

import com.calpano.graphinout.base.gio.GioWriter;
import com.calpano.graphinout.base.input.InputSource;

public class JsonParser {
    private final InputSource inputSource;
    private final GioWriter writer;

    public JsonParser(InputSource inputSource, GioWriter writer) {
        this.inputSource = inputSource;
        this.writer = writer;
    }


}

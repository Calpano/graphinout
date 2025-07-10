package com.calpano.graphinout.reader.json;

import com.calpano.graphinout.base.GioService;
import com.calpano.graphinout.base.gio.GioReader;

import java.util.Arrays;
import java.util.List;

public class JsonService implements GioService {

    @Override
    public String id() {
        return "reader-json";
    }

    @Override
    public List<GioReader> readers() {
        return Arrays.asList(new JsonReader());
    }

}

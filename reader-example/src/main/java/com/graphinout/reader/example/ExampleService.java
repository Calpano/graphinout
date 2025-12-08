package com.graphinout.reader.example;

import com.graphinout.base.gio.GioService;
import com.graphinout.base.gio.GioReader;
import com.graphinout.base.gio.GioWriter;

import java.util.Arrays;
import java.util.List;

public class ExampleService implements GioService {

    @Override
    public String id() {
        return "reader-example";
    }

    @Override
    public List<GioReader> readers() {
        return Arrays.asList(new ExampleReader());
    }

    @Override
    public List<GioWriter> writers() {
        return Arrays.asList(new ExampleWriter());
    }

}

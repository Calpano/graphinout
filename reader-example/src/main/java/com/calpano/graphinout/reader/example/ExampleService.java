package com.calpano.graphinout.reader.example;

import com.calpano.graphinout.base.GioService;
import com.calpano.graphinout.base.gio.GioReader;

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

}

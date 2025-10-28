package com.graphinout.reader.example;

import com.graphinout.base.GioService;
import com.graphinout.base.GioReader;

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

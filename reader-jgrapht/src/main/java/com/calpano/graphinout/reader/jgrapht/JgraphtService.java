package com.calpano.graphinout.reader.jgrapht;

import com.calpano.graphinout.base.GioService;
import com.calpano.graphinout.base.reader.GioReader;

import java.util.Arrays;
import java.util.List;

public class JgraphtService implements GioService {
    @Override
    public String id() {
        return "reader-jgrapht";
    }

    @Override
    public List<GioReader> readers() {
        return Arrays.asList(new Graph6Reader());
    }
}

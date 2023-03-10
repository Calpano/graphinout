package com.calpano.graphinout.reader.graphml;

import com.calpano.graphinout.base.GioService;
import com.calpano.graphinout.base.reader.GioReader;

import java.util.Arrays;
import java.util.List;

public class GraphmlService implements GioService {
    @Override
    public String id() {
        return "reader-graphml";
    }

    @Override
    public List<GioReader> readers() {
        return Arrays.asList(new GraphmlReader());
    }
}

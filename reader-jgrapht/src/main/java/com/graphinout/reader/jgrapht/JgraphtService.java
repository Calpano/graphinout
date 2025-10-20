package com.graphinout.reader.jgrapht;

import com.graphinout.base.GioService;
import com.graphinout.base.gio.GioReader;
import com.graphinout.reader.jgrapht.dot.DotReader;

import java.util.Arrays;
import java.util.List;

public class JgraphtService implements GioService {

    @Override
    public String id() {
        return "reader-jgrapht";
    }

    @Override
    public List<GioReader> readers() {
        return Arrays.asList(new Graph6Reader(), new Sparse6Reader(), new DotReader());
    }

}

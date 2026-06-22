package com.graphinout.reader.jgrapht;

import com.graphinout.base.gio.GioReader;
import com.graphinout.base.gio.GioService;
import com.graphinout.base.gio.GioWriter;

import java.util.Arrays;
import java.util.List;

public class JgraphtService implements GioService {

    @Override
    public String id() {
        return "reader-jgrapht";
    }

    @Override
    public List<GioReader> readers() {
        return Arrays.asList(new Graph6Reader(), new Sparse6Reader(), new Digraph6Reader());
    }

    @Override
    public List<GioWriter> writers() {
        return List.of(new Graph6Writer());
    }


}

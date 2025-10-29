package com.graphinout.reader.graphml;

import com.graphinout.base.gio.GioService;
import com.graphinout.base.gio.GioReader;

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

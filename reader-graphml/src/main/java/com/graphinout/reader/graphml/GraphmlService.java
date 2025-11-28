package com.graphinout.reader.graphml;

import com.graphinout.base.gio.GioReader;
import com.graphinout.base.gio.GioService;
import com.graphinout.base.gio.GioWriter;

import java.util.List;

public class GraphmlService implements GioService {

    @Override
    public String id() {
        return "reader-graphml";
    }

    @Override
    public List<GioReader> readers() {
        return List.of(new GraphmlReader());
    }

    @Override
    public List<GioWriter> writers() {
        return List.of(new GraphmlWriter());
    }

}

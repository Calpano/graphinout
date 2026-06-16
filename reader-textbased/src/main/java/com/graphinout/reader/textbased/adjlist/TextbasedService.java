package com.graphinout.reader.textbased.adjlist;

import com.graphinout.base.gio.GioService;
import com.graphinout.base.gio.GioReader;
import com.graphinout.base.gio.GioWriter;

import java.util.List;

public class TextbasedService implements GioService {

    @Override
    public String id() {
        return "reader-adjlist";
    }

    @Override
    public List<GioReader> readers() {
        return List.of(new AdjListReader(), new EdgeListReader());
    }

    @Override
    public List<GioWriter> writers() {
        return List.of(new AdjListWriter(), new EdgeListWriter());
    }

}

package com.graphinout.reader.gexf;

import com.graphinout.base.gio.GioReader;
import com.graphinout.base.gio.GioService;
import com.graphinout.base.gio.GioWriter;

import java.util.List;

public class GexfService implements GioService {

    @Override
    public String id() {
        return "reader-gexf";
    }

    @Override
    public List<GioReader> readers() {
        return List.of(new GexfReader());
    }

    @Override
    public List<GioWriter> writers() {
        return List.of(new GexfWriter());
    }

}

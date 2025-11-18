package com.graphinout.reader.dot;

import com.graphinout.base.gio.GioReader;
import com.graphinout.base.gio.GioService;
import com.graphinout.base.gio.GioWriter;

import java.util.List;

public class DotService implements GioService {

    @Override
    public String id() {
        return "reader-dot";
    }

    @Override
    public List<GioReader> readers() {
        return List.of(new DotReader());
    }

    @Override
    public List<GioWriter> writers() {
        return List.of(new DotWriter());
    }


}

package com.graphinout.reader.cjgef;

import com.graphinout.base.gio.GioReader;
import com.graphinout.base.gio.GioService;
import com.graphinout.base.gio.GioWriter;

import java.util.List;

public class CjGefService implements GioService {

    @Override
    public String id() {
        return "reader-cj-gef";
    }

    @Override
    public List<GioReader> readers() {
        return List.of(new CjGefReader());
    }

    @Override
    public List<GioWriter> writers() {
        return List.of();
    }
}

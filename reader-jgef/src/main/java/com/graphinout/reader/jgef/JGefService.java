package com.graphinout.reader.jgef;

import com.graphinout.base.gio.GioReader;
import com.graphinout.base.gio.GioService;
import com.graphinout.base.gio.GioWriter;

import java.util.List;

public class JGefService implements GioService {

    @Override
    public String id() {
        return "reader-jgef";
    }

    @Override
    public List<GioReader> readers() {
        return List.of(new JGefReader());
    }

    @Override
    public List<GioWriter> writers() {
        return List.of();
    }
}

package com.graphinout.reader.pajek;

import com.graphinout.base.gio.GioReader;
import com.graphinout.base.gio.GioService;
import com.graphinout.base.gio.GioWriter;

import java.util.List;

public class PajekService implements GioService {

    @Override
    public String id() {
        return "reader-pajek";
    }

    @Override
    public List<GioReader> readers() {
        return List.of(new PajekReader());
    }

    @Override
    public List<GioWriter> writers() {
        return List.of(new PajekWriter());
    }
}

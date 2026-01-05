package com.graphinout.reader.ocif;

import com.graphinout.base.gio.GioReader;
import com.graphinout.base.gio.GioService;
import com.graphinout.base.gio.GioWriter;

import java.util.List;

public class OcifService implements GioService {

    @Override
    public String id() {
        return "reader-ocif";
    }

    @Override
    public List<GioReader> readers() {
        return List.of(new OcifReader());
    }

    @Override
    public List<GioWriter> writers() {
        return List.of(new OcifWriter());
    }

}

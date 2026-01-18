package com.graphinout.reader.rdf;

import com.graphinout.base.gio.GioReader;
import com.graphinout.base.gio.GioService;
import com.graphinout.base.gio.GioWriter;

import java.util.List;

public class RdfService implements GioService {

    @Override
    public String id() {
        return "reader-rdf";
    }

    @Override
    public List<GioReader> readers() {
        return List.of(new RdfReader());
    }

    @Override
    public List<GioWriter> writers() {
        return List.of(new RdfWriter());
    }

}

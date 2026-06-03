package com.graphinout.reader.mermaid;

import com.graphinout.base.gio.GioReader;
import com.graphinout.base.gio.GioService;
import com.graphinout.base.gio.GioWriter;

import java.util.List;

public class MermaidService implements GioService {

    @Override
    public String id() {
        return "reader-mermaid";
    }

    @Override
    public List<GioReader> readers() {
        return List.of(new MermaidReader());
    }

    @Override
    public List<GioWriter> writers() {
        return List.of(new MermaidWriter());
    }
}

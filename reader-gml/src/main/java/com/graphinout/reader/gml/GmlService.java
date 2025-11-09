package com.graphinout.reader.gml;

import com.graphinout.base.gio.GioReader;
import com.graphinout.base.gio.GioService;
import com.graphinout.base.gio.GioWriter;

import java.util.List;

public class GmlService implements GioService {

    @Override
    public String id() {
        return "reader-gml";
    }

    @Override
    public List<GioReader> readers() {
        return List.of(new GmlReader());
    }

    @Override
    public List<GioWriter> writers() {
        return List.of(new GmlWriter());
    }

}

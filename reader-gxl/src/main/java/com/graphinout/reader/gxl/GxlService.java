package com.graphinout.reader.gxl;

import com.graphinout.base.gio.GioReader;
import com.graphinout.base.gio.GioService;
import com.graphinout.base.gio.GioWriter;

import java.util.List;

public class GxlService implements GioService {

    @Override
    public String id() {
        return "reader-gxl";
    }

    @Override
    public List<GioReader> readers() {
        return List.of(new GxlReader());
    }

    @Override
    public List<GioWriter> writers() {
        return List.of(new GxlWriter());
    }

}

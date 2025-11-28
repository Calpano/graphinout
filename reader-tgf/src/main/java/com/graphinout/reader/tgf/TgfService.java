package com.graphinout.reader.tgf;

import com.graphinout.base.gio.GioReader;
import com.graphinout.base.gio.GioService;
import com.graphinout.base.gio.GioWriter;

import java.util.List;

public class TgfService implements GioService {

    @Override
    public String id() {
        return "reader-tgf";
    }

    @Override
    public List<GioReader> readers() {
        return List.of(new TgfReader());
    }

    @Override
    public List<GioWriter> writers() {
        return List.of(new TgfWriter());
    }

}

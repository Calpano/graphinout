package com.graphinout.reader.d2;

import com.graphinout.base.gio.GioReader;
import com.graphinout.base.gio.GioService;
import com.graphinout.base.gio.GioWriter;

import java.util.List;

public class D2Service implements GioService {

    @Override
    public String id() {
        return "reader-d2";
    }

    @Override
    public List<GioReader> readers() {
        return List.of(new D2Reader());
    }

    @Override
    public List<GioWriter> writers() {
        return List.of(new D2Writer());
    }
}

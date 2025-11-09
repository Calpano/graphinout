package com.graphinout.reader.cj;

import com.graphinout.base.gio.GioReader;
import com.graphinout.base.gio.GioService;
import com.graphinout.base.gio.GioWriter;

import java.util.List;

public class ConnectedJsonService implements GioService {

    @Override
    public String id() {
        return "reader-cj";
    }

    @Override
    public List<GioReader> readers() {
        return List.of(new ConnectedJsonReader(), new ConnectedJson5Reader());
    }

    @Override
    public List<GioWriter> writers() {
        return List.of(new ConnectedJsonWriter());
    }

}

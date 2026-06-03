package com.graphinout.reader.ddot;

import com.graphinout.base.gio.GioReader;
import com.graphinout.base.gio.GioService;
import com.graphinout.base.gio.GioWriter;

import java.util.List;

public class DDotService implements GioService {

    @Override
    public String id() {
        return "reader-ddot";
    }

    @Override
    public List<GioReader> readers() {
        return List.of(new DDotReader());
    }

    @Override
    public List<GioWriter> writers() {
        return List.of(new DDotWriter());
    }

}

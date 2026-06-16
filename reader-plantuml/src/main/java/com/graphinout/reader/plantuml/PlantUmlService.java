package com.graphinout.reader.plantuml;

import com.graphinout.base.gio.GioReader;
import com.graphinout.base.gio.GioService;
import com.graphinout.base.gio.GioWriter;

import java.util.List;

public class PlantUmlService implements GioService {

    @Override
    public String id() {
        return "reader-plantuml";
    }

    @Override
    public List<GioReader> readers() {
        return List.of(new PlantUmlReader());
    }

    @Override
    public List<GioWriter> writers() {
        return List.of(new PlantUmlWriter());
    }

}

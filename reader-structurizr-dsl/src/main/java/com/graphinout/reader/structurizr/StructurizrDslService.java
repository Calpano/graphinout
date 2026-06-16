package com.graphinout.reader.structurizr;

import com.graphinout.base.gio.GioReader;
import com.graphinout.base.gio.GioService;
import com.graphinout.base.gio.GioWriter;

import java.util.List;

public class StructurizrDslService implements GioService {

    @Override
    public String id() {
        return "reader-structurizr-dsl";
    }

    @Override
    public List<GioReader> readers() {
        return List.of(new StructurizrDslReader());
    }

    @Override
    public List<GioWriter> writers() {
        // Read-only: generating valid C4/Structurizr DSL (workspace/model/views nesting) is out of scope.
        return List.of();
    }

}

package com.graphinout.reader.pgformat;

import com.graphinout.base.gio.GioReader;
import com.graphinout.base.gio.GioService;
import com.graphinout.base.gio.GioWriter;

import java.util.List;

/** Registers the PG-JSON reader and writer with the engine (via {@link java.util.ServiceLoader}). */
public class PgJsonService implements GioService {

    @Override
    public String id() {
        return "reader-pgformat";
    }

    @Override
    public List<GioReader> readers() {
        return List.of(new PgJsonReader());
    }

    @Override
    public List<GioWriter> writers() {
        return List.of(new PgJsonReader());
    }

}

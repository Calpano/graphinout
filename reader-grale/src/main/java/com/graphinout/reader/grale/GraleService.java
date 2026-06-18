package com.graphinout.reader.grale;

import com.graphinout.base.gio.GioReader;
import com.graphinout.base.gio.GioService;
import com.graphinout.base.gio.GioWriter;

import java.util.List;

/**
 * Service-loader entry for the grale graph-layout JSON format. A single {@link GraleReader} instance
 * acts as both reader and writer.
 */
public class GraleService implements GioService {

    @Override
    public String id() {
        return "reader-grale";
    }

    @Override
    public List<GioReader> readers() {
        return List.of(new GraleReader());
    }

    @Override
    public List<GioWriter> writers() {
        return List.of(new GraleReader());
    }
}

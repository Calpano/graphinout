package com.graphinout.reader.gtfs;

import com.graphinout.base.gio.GioReader;
import com.graphinout.base.gio.GioService;
import com.graphinout.base.gio.GioWriter;

import java.util.List;

public class GtfsService implements GioService {

    @Override
    public String id() {
        return "reader-gtfs";
    }

    @Override
    public List<GioReader> readers() {
        return List.of(new GtfsReader());
    }

    /** GTFS is read-only: a CJ graph carries too little information to write a meaningful timetable feed. */
    @Override
    public List<GioWriter> writers() {
        return List.of();
    }

}

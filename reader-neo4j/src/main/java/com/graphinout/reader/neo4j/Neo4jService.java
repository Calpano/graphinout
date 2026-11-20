package com.graphinout.reader.neo4j;

import com.graphinout.base.gio.GioReader;
import com.graphinout.base.gio.GioService;
import com.graphinout.base.gio.GioWriter;

import java.util.List;

/** Registers the Neo4j APOC JSON reader and writer with the engine (via {@link java.util.ServiceLoader}). */
public class Neo4jService implements GioService {

    @Override
    public String id() {
        return "reader-neo4j";
    }

    @Override
    public List<GioReader> readers() {
        return List.of(new Neo4jReader());
    }

    @Override
    public List<GioWriter> writers() {
        return List.of(new Neo4jReader());
    }

}

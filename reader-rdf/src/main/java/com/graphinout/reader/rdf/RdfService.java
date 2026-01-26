package com.graphinout.reader.rdf;

import com.graphinout.base.gio.GioReader;
import com.graphinout.base.gio.GioService;
import com.graphinout.base.gio.GioWriter;

import java.util.List;
import java.util.stream.Collectors;

public class RdfService implements GioService {

    @Override
    public String id() {
        return "reader-rdf";
    }

    public List<RdfReader> rdfReaders() {
        return List.of( //
                new RdfTurtleReader(), //
                new RdfXmlReader(), //
                new RdfNTriplesReader(), //
                new RdfNQuadsReader(), //
                new RdfTriGReader(), //
                new RdfTriXReader(), //
                new RdfNQuadsReader(), //
                new RdfTriGReader(), //
                new RdfTriXReader(),
                new RdfJsonReader(),
                new JsonLdReader()
        );
    }

    @Override
    public List<GioReader> readers() {
        return rdfReaders().stream().map(x -> x).collect(Collectors.toList());
    }

    @Override
    public List<GioWriter> writers() {
        return rdfReaders().stream().map(x -> x).collect(Collectors.toList());
    }

}

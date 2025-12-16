package com.graphinout.reader.ocif.document.impl;

import com.graphinout.reader.ocif.document.IOcifRepresentation;
import com.graphinout.reader.ocif.document.IOcifResourceMutable;

import java.util.ArrayList;
import java.util.List;

public class OcifResource extends OcifExtensibleEntity implements IOcifResourceMutable {

    private final String id;
    private final List<IOcifRepresentation> representations = new ArrayList<>();

    public OcifResource(String id) {this.id = id;}

    @Override
    public IOcifResourceMutable addRepresentation(IOcifRepresentation rep) {
        this.representations.add(rep);
        return this;
    }

    @Override
    public String id() {return id;}

    @Override
    public List<IOcifRepresentation> representations() {return representations;}

}

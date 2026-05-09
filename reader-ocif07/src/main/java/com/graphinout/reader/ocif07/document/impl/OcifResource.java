package com.graphinout.reader.ocif07.document.impl;

import com.graphinout.reader.ocif07.document.IOcifRepresentation;
import com.graphinout.reader.ocif07.document.IOcifResourceMutable;
import com.graphinout.reader.ocif07.document.extension.resource.IOcifResourceExtension;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static com.graphinout.reader.ocif07.OCIF.Common.COMMENT;
import static com.graphinout.reader.ocif07.OCIF.Common.DATA;
import static com.graphinout.reader.ocif07.OCIF.Common.ID;
import static com.graphinout.reader.ocif07.OCIF.Resource.REPRESENTATIONS;

public class OcifResource extends DecoratedJsonObject implements IOcifResourceMutable {

    private final String id;
    private final List<IOcifRepresentation> representations = new ArrayList<>();
    private final List<IOcifResourceExtension> extensions = new ArrayList<>();

    public OcifResource(String id) {this.id = id;}

    @Override
    public IOcifResourceMutable addExtension(IOcifResourceExtension extension) {
        this.extensions.add(extension);
        return this;
    }

    @Override
    public IOcifResourceMutable addRepresentation(IOcifRepresentation rep) {
        this.representations.add(rep);
        return this;
    }

    @Override
    public Set<String> definedKeys() {
        return Set.of(ID, REPRESENTATIONS, DATA, COMMENT);
    }

    @Override
    public @NonNull List<IOcifResourceExtension> extensions() {
        return extensions;
    }

    @Override
    public String id() {return id;}

    @Override
    public List<IOcifRepresentation> representations() {return representations;}


}

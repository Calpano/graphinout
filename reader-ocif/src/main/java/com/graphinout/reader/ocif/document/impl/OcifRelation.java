package com.graphinout.reader.ocif.document.impl;

import com.graphinout.reader.ocif.OCIF;
import com.graphinout.reader.ocif.document.IOcifRelationMutable;
import com.graphinout.reader.ocif.document.extension.relation.IOcifRelationExtension;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@SuppressWarnings("UnusedReturnValue")
public class OcifRelation extends DecoratedJsonObject implements IOcifRelationMutable {

    private final List<IOcifRelationExtension> extensions = new ArrayList<>();
    private String id;
    /** visual node representing relation */
    private @Nullable String node;

    @Override
    public IOcifRelationMutable addExtension(@NonNull IOcifRelationExtension ext) {
        this.extensions.add(ext);
        return this;
    }

    @Override
    public Set<String> definedKeys() {
        return Set.of(OCIF.Common.ID, OCIF.Common.NODE);
    }

    @Override
    public @NonNull List<IOcifRelationExtension> extensions() {return extensions;}

    @Override
    public String id() {return id;}

    @Override
    public IOcifRelationMutable id(String id) {
        this.id = id;
        return this;
    }

    @Override
    public @Nullable String node() {return node;}

    @Override
    public IOcifRelationMutable node(String node) {
        this.node = node;
        return this;
    }

}

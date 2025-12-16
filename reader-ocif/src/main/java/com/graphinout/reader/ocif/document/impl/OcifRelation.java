package com.graphinout.reader.ocif.document.impl;

import com.graphinout.reader.ocif.OCIF;
import com.graphinout.reader.ocif.document.IOcifRelationMutable;
import com.graphinout.reader.ocif.document.extension.IOcifExtension;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@SuppressWarnings("UnusedReturnValue")
public class OcifRelation extends OcifExtensibleEntity implements IOcifRelationMutable {

    private final List<IOcifExtension> extensions = new ArrayList<>();
    private String id;
    /** visual node representing relation */
    private @Nullable String node;

    @Override
    public IOcifRelationMutable addExtension(@NonNull IOcifExtension ext) {
        this.extensions.add(ext);
        return this;
    }

    @Override
    public Set<String> definedKeys() {
        return Set.of( OCIF.Common.ID, OCIF.Common.NODE );
    }

    @Override
    public List<IOcifExtension> extensions() {return extensions;}

    @Override
    public String id() {return id;}

    @Override
    public @Nullable String node() {return node;}

    @Override
    public IOcifRelationMutable setId(String id) {
        this.id = id;
        return this;
    }

    @Override
    public IOcifRelationMutable setNode(String node) {
        this.node = node;
        return this;
    }

}

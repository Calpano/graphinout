package com.graphinout.reader.ocif.document.impl;

import com.graphinout.reader.ocif.document.IOcifRepresentation;
import com.graphinout.reader.ocif.document.IOcifRepresentationMutable;
import com.graphinout.reader.ocif.document.extension.representation.IOcifRepresentationExtension;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class OcifRepresentation extends DecoratedJsonObject implements IOcifRepresentationMutable {

    private final String location;
    private final String mimeType;
    private final String content;
    private final List<IOcifRepresentationExtension> extensions = new ArrayList<>();

    /** Either content or location MUST be present. */
    public OcifRepresentation(@Nullable String content, @Nullable String location, @NonNull String mimeType) {
        if (content == null && location == null) {
            throw new IllegalArgumentException("Either content or location MUST be present");
        }
        this.location = location;
        this.mimeType = mimeType;
        this.content = content;
    }

    @Override
    public @NonNull IOcifRepresentation addExtension(@NonNull IOcifRepresentationExtension extension) {
        this.extensions.add(extension);
        return this;
    }

    @Override
    public String content() {return content;}

    @Override
    public Set<String> definedKeys() {
        return Set.of("content", "location", "mimeType");
    }

    @Override
    public @NonNull List<IOcifRepresentationExtension> extensions() {
        return extensions;
    }

    @Override
    public String location() {return location;}

    @Override
    public String mimeType() {return mimeType;}

}

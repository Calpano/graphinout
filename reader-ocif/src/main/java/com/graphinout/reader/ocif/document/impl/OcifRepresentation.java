package com.graphinout.reader.ocif.document.impl;

import com.graphinout.reader.ocif.document.IOcifRepresentation;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class OcifRepresentation implements IOcifRepresentation {

    private final String location;
    private final String mimeType;
    private final String content;

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
    public String content() {return content;}

    @Override
    public String location() {return location;}

    @Override
    public String mimeType() {return mimeType;}

}

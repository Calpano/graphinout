package com.calpano.graphinout.reader.example;

import org.jetbrains.annotations.Nullable;

public class Triple<S, P, O> {

    final S s;
    final P p;
    final O o;
    final @Nullable String meta;

    public Triple(S s, P p, O o, @Nullable String meta) {
        this.s = s;
        this.p = p;
        this.o = o;
        this.meta = meta;
    }

}

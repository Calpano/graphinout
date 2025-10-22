package com.graphinout.reader.example;

import javax.annotation.Nullable;

public interface ITripleHandler<S, P, O> {

    void onTriple(S s, P p, O o, @Nullable String meta);

}

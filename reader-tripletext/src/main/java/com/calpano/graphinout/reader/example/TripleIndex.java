package com.calpano.graphinout.reader.example;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * TODO sort index;
 * TODO meta
 *
 * @param <S>
 * @param <P>
 * @param <O>
 */
public class TripleIndex<S, P, O> {

    private Map<S, Map<P, Set<O>>> index = new HashMap<>();

    public void forEach(ITripleHandler<S, P, O> handler) {
        index.forEach((s, ps) -> //
                ps.forEach((p, os) -> //
                        os.forEach(o -> //
                                handler.onTriple(s, p, o, null))));
    }

    public void index(S s, P p, O o) {
        index.computeIfAbsent(s, _s -> new HashMap<>()).computeIfAbsent(p, _p -> new HashSet<>()).add(o);
    }

}

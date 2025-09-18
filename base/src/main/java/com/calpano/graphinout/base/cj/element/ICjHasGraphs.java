package com.calpano.graphinout.base.cj.element;

import java.util.stream.Stream;

public interface ICjHasGraphs extends ICjElement {

    Stream<ICjGraph> graphs();

}

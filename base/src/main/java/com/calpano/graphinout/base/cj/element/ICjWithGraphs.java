package com.calpano.graphinout.base.cj.element;

import java.util.stream.Stream;

public interface ICjWithGraphs extends ICjElement {

    Stream<ICjGraph> graphs();

}

package com.graphinout.base.cj.document;

import java.util.stream.Stream;

public interface ICjHasGraphs extends ICjElement {

    Stream<ICjGraph> graphs();

}

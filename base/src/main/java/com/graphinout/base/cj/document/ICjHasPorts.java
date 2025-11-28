package com.graphinout.base.cj.document;

import java.util.stream.Stream;

public interface ICjHasPorts extends ICjElement {

    Stream<ICjPort> ports();

}

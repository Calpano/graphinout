package com.graphinout.base.cj.element;

import java.util.stream.Stream;

public interface ICjHasPorts extends ICjElement {

    Stream<ICjPort> ports();

}

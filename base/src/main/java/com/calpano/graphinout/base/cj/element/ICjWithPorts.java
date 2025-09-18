package com.calpano.graphinout.base.cj.element;

import java.util.stream.Stream;

public interface ICjWithPorts extends ICjElement {

    Stream<ICjPort> ports();

}

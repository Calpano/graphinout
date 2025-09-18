package com.calpano.graphinout.base.cj.element;

import java.util.List;
import java.util.stream.Stream;

public interface ICjLabel extends ICjElement {

    Stream<ICjLabelEntry> entries();

}

package com.calpano.graphinout.base.cj.element;

import java.util.function.Consumer;

public interface ICjHasPortsMutable extends ICjHasPorts {

    void addPort(Consumer<ICjPortMutable> consumer);

}

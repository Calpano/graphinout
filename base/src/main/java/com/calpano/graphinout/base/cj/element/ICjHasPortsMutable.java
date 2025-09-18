package com.calpano.graphinout.base.cj.element;

import com.calpano.graphinout.base.cj.element.impl.CjPortElement;

import java.util.function.Consumer;

public interface ICjHasPortsMutable extends ICjHasPorts {

    void addPort(Consumer<CjPortElement> consumer);

}

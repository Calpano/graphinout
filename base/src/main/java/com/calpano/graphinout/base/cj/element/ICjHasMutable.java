package com.calpano.graphinout.base.cj.element;

import com.calpano.graphinout.base.cj.element.impl.CjPortElement;

import java.util.function.Consumer;

public interface ICjHasMutable extends ICjHasPorts {

    CjPortElement addPort(Consumer<CjPortElement> consumer);

}

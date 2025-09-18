package com.calpano.graphinout.base.cj.element;

import com.calpano.graphinout.base.cj.element.impl.CjLabelElement;

import java.util.function.Consumer;

public interface ICjHasLabelMutable extends ICjHasLabel {

    void label(Consumer<CjLabelElement> label);


}

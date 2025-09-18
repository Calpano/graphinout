package com.calpano.graphinout.base.cj.element;

import java.util.function.Consumer;

public interface ICjHasLabelMutable extends ICjHasLabel {

    void label(Consumer<ICjLabelMutable> label);


}

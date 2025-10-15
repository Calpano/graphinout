package com.graphinout.base.cj.element;

import java.util.function.Consumer;

public interface ICjHasLabelMutable extends ICjHasLabel {

    /** at most one per element */
    void setLabel(Consumer<ICjLabelMutable> label);


}

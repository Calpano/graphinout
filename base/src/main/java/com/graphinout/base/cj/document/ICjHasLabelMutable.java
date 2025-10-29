package com.graphinout.base.cj.document;

import java.util.function.Consumer;

public interface ICjHasLabelMutable extends ICjHasLabel {

    /** at most one per element */
    void setLabel(Consumer<ICjLabelMutable> label);

    ICjLabelMutable labelMutable();

}

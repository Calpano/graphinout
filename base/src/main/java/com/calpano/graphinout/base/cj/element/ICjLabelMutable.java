package com.calpano.graphinout.base.cj.element;

import java.util.function.Consumer;

public interface ICjLabelMutable extends ICjLabel {

    void entry(Consumer<ICjLabelEntryMutable> labelEntry);

}

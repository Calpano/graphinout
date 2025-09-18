package com.calpano.graphinout.base.cj.element;

import com.calpano.graphinout.base.cj.element.impl.CjLabelEntryElement;

import java.util.function.Consumer;

public interface ICjLabelMutable extends ICjLabel {

    void entry(Consumer<CjLabelEntryElement> labelEntry);

}

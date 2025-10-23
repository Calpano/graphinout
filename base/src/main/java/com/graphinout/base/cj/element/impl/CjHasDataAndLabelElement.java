package com.graphinout.base.cj.element.impl;

import com.graphinout.base.cj.element.ICjHasLabelMutable;
import com.graphinout.base.cj.element.ICjLabelMutable;

import javax.annotation.Nullable;
import java.util.function.Consumer;

/** Luckily, all CJ elements with a label also have data */
public abstract class CjHasDataAndLabelElement extends CjHasDataElement implements ICjHasLabelMutable {

    @Nullable CjLabelElement labelElement;

    @Nullable
    @Override
    public CjLabelElement label() {
        return labelElement;
    }

    @Override
    public ICjLabelMutable labelMutable() {
        if (this.labelElement == null) {
            this.labelElement = new CjLabelElement();
        }
        return labelElement;
    }

    @Override
    public void setLabel(Consumer<ICjLabelMutable> label) {
        label.accept(labelMutable());
    }


}

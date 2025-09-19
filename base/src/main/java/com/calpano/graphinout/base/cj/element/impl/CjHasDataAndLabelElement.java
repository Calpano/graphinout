package com.calpano.graphinout.base.cj.element.impl;

import com.calpano.graphinout.base.cj.stream.ICjWriter;
import com.calpano.graphinout.base.cj.element.ICjHasLabelMutable;
import com.calpano.graphinout.base.cj.element.ICjLabelMutable;

import javax.annotation.Nullable;
import java.util.function.Consumer;

/** Luckily, all CJ elements with a label also have data */
public abstract class CjHasDataAndLabelElement extends CjHasDataElement implements ICjHasLabelMutable {

    @Nullable CjLabelElement labelElement;

    CjHasDataAndLabelElement(@Nullable CjElement parent) {
        super(parent);
    }

    @Override
    public void label(Consumer<ICjLabelMutable> label) {
        this.labelElement = new CjLabelElement(this);
        label.accept(labelElement);
    }

    @Nullable
    @Override
    public CjLabelElement label() {
        return labelElement;
    }

    protected void fireLabelMaybe(ICjWriter cjWriter) {
        if(labelElement!=null) {
            labelElement.fire(cjWriter);
        }

    }

}

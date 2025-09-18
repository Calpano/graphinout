package com.calpano.graphinout.base.cj.element.impl;

import com.calpano.graphinout.base.cj.CjWriter;

import javax.annotation.Nullable;
import java.util.function.Consumer;

/** Luckily, all CJ elements with a label also have data */
public abstract class CjHasDataAndLabelElement extends CjHasDataElement {

    @Nullable CjLabelElement labelElement;

    CjHasDataAndLabelElement(@Nullable CjElement parent) {
        super(parent);
    }

    public void label(Consumer<CjLabelElement> label) {
        this.labelElement = new CjLabelElement(this);
        label.accept(labelElement);
    }

    public @Nullable CjLabelElement label() {
        return labelElement;
    }

    protected void fireLabelMaybe(CjWriter cjWriter) {
        if(labelElement!=null) {
            labelElement.fire(cjWriter);
        }

    }

}

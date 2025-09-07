package com.calpano.graphinout.base.cj.element.impl;

import javax.annotation.Nullable;
import java.util.function.Consumer;

/** Luckily, all CJ elements with a label also have data */
public abstract class CjWithDataAndLabelElement extends CjWithDataElement {

    @Nullable CjLabelElement labelElement;

    CjWithDataAndLabelElement(@Nullable CjElement parent) {
        super(parent);
    }

    public void label(Consumer<CjLabelElement> label) {
        this.labelElement = new CjLabelElement(this);
        label.accept(labelElement);
    }

    public @Nullable CjLabelElement label() {
        return labelElement;
    }

}

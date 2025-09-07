package com.calpano.graphinout.base.cj.element.impl;

import com.calpano.graphinout.base.cj.CjWriter;

import javax.annotation.Nullable;
import java.util.function.Consumer;

/** Helper class to track element context */
public abstract class CjWithDataElement extends CjElement {

    private @Nullable CjDataElement dataElement;

    CjWithDataElement(@Nullable CjElement parent) {
        super(parent);
    }

    public void data(Consumer<CjDataElement> data) {
        this.dataElement = new CjDataElement(this);
    }

    public @Nullable CjDataElement data() {
        return dataElement;
    }

    public void fireDataMaybe(CjWriter cjWriter) {
        if (dataElement == null) return;
        dataElement.fire(cjWriter);
    }

}

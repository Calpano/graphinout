package com.calpano.graphinout.base.cj.element.impl;

import com.calpano.graphinout.base.cj.CjWriter;
import com.calpano.graphinout.foundation.json.impl.IMagicMutableJsonValue;

import javax.annotation.Nullable;
import java.util.function.Consumer;

/** Helper class to track element context */
public abstract class CjHasDataElement extends CjElement {

    private @Nullable CjDataElement dataElement;

    CjHasDataElement(@Nullable CjElement parent) {
        super(parent);
    }

    public void data(Consumer<IMagicMutableJsonValue> consumer) {
        dataElement(de -> consumer.accept(de.jsonValueMutable()));
    }

    public @Nullable CjDataElement data() {
        return dataElement;
    }

    public void dataElement(Consumer<CjDataElement> consumer) {
        // attach
        this.dataElement = new CjDataElement(this);
        consumer.accept(dataElement);
    }

    public void fireDataMaybe(CjWriter cjWriter) {
        if (dataElement == null) return;
        dataElement.fire(cjWriter);
    }

}

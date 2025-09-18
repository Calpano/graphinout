package com.calpano.graphinout.base.cj.element.impl;

import com.calpano.graphinout.base.cj.CjWriter;
import com.calpano.graphinout.base.cj.element.ICjData;
import com.calpano.graphinout.base.cj.element.ICjHasDataMutable;
import com.calpano.graphinout.foundation.json.impl.IMagicMutableJsonValue;

import javax.annotation.Nullable;
import java.util.function.Consumer;

/** Helper class to track element context */
public abstract class CjHasDataElement extends CjElement implements ICjHasDataMutable {

    private @Nullable CjDataElement dataElement;

    CjHasDataElement(@Nullable CjElement parent) {
        super(parent);
    }

    @Override
    public void data(Consumer<IMagicMutableJsonValue> consumer) {
        dataElement(de -> consumer.accept(de.jsonValueMutable()));
    }

    @Nullable
    @Override
    public ICjData data() {
        return dataElement;
    }

    @Override
    public void dataElement(Consumer<ICjData> consumer) {
        // attach
        this.dataElement = new CjDataElement(this);
        consumer.accept(dataElement);
    }

    public void fireDataMaybe(CjWriter cjWriter) {
        if (dataElement == null) return;
        dataElement.fire(cjWriter);
    }

}

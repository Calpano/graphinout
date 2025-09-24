package com.calpano.graphinout.base.cj.element.impl;

import com.calpano.graphinout.base.cj.element.ICjData;
import com.calpano.graphinout.base.cj.element.ICjHasDataMutable;
import com.calpano.graphinout.foundation.json.impl.IMagicMutableJsonValue;

import javax.annotation.Nullable;
import java.util.function.Consumer;

/** Helper class to track element context */
public abstract class CjHasDataElement implements ICjHasDataMutable {

    private @Nullable CjDataElement dataElement;

    @Override
    public void addData(Consumer<IMagicMutableJsonValue> consumer) {
        addDataElement(de -> consumer.accept(de.jsonValueMutable()));
    }

    @Override
    public void addDataElement(Consumer<ICjData> consumer) {
        if (this.dataElement == null) {
            this.dataElement = new CjDataElement(this);
        }
        consumer.accept(dataElement);
    }

    @Nullable
    @Override
    public ICjData data() {
        return dataElement;
    }


}

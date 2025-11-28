package com.graphinout.base.cj.document.impl;

import com.graphinout.base.cj.document.ICjDataMutable;
import com.graphinout.base.cj.document.ICjHasDataMutable;

import javax.annotation.Nullable;
import java.util.function.Consumer;

/** Helper class to track element context */
public abstract class CjHasDataElement implements ICjHasDataMutable {

    private @Nullable CjDataElement dataElement;

    @Nullable
    @Override
    public ICjDataMutable data() {
        return dataElement;
    }

    @Override
    public void dataMutable(Consumer<ICjDataMutable> consumer) {
        if (dataElement == null) {
            this.dataElement = new CjDataElement();
        }
        consumer.accept(dataElement);
    }


}

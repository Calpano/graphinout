package com.graphinout.base.cj.document.impl;

import com.graphinout.base.cj.document.ICjDataMutable;
import com.graphinout.base.cj.document.ICjHasDataMutable;
import org.jspecify.annotations.Nullable;

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
        // we must deliver and keep even empty data, as later stages might add data to it
        if (dataElement == null) {
            dataElement = new CjDataElement();
        }
        consumer.accept(dataElement);
    }


}

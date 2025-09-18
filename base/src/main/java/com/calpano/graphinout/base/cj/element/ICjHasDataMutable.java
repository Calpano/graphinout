package com.calpano.graphinout.base.cj.element;

import com.calpano.graphinout.base.cj.element.impl.CjDataElement;
import com.calpano.graphinout.foundation.json.impl.IMagicMutableJsonValue;

import javax.annotation.Nullable;
import java.util.function.Consumer;

public interface ICjHasDataMutable {

    void data(Consumer<IMagicMutableJsonValue> consumer);

    @Nullable
    CjDataElement data();

    void dataElement(Consumer<CjDataElement> consumer);

}

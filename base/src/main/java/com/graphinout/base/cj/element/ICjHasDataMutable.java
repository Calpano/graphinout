package com.graphinout.base.cj.element;

import com.graphinout.base.cj.CjDataProperty;
import com.graphinout.foundation.json.value.IJsonFactory;
import com.graphinout.foundation.json.value.IJsonXmlString;

import java.util.function.Consumer;

public interface ICjHasDataMutable extends ICjHasData {

    ICjDataMutable data();

    /**
     * @param consumer receive current or new {@link ICjDataMutable}, never null.
     */
    void dataMutable(Consumer<ICjDataMutable> consumer);

    default void descriptionPlainText(IJsonFactory jsonFactory, String descriptionText) {
        dataMutable(data -> {
            IJsonXmlString value = IJsonXmlString.ofPlainString(jsonFactory, descriptionText);
            data.addProperty(CjDataProperty.Description.cjPropertyKey, value);
        });
    }

}

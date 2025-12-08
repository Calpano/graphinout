package com.graphinout.base.cj.document;

import com.graphinout.base.cj.data.CjDataProperty;
import com.graphinout.foundation.pure.json.document.IJsonFactory;
import com.graphinout.foundation.pure.json.document.IJsonXmlString;

import java.util.function.Consumer;

public interface ICjHasDataMutable extends ICjHasData {

    @Override
    ICjDataMutable data();

    /**
     * @param consumer receive current or new {@link ICjDataMutable}, never null.
     */
    void dataMutable(Consumer<ICjDataMutable> consumer);

    /**
     * Set the <em>description</em> to a plain-text string.
     * @param jsonFactory
     * @param descriptionText
     */
    default void descriptionPlainText(IJsonFactory jsonFactory, String descriptionText) {
        dataMutable(data -> {
            IJsonXmlString value = IJsonXmlString.ofPlainString(jsonFactory, descriptionText);
            data.addProperty(CjDataProperty.Description.cjPropertyKey, value);
        });
    }

}

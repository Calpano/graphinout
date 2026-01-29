package com.graphinout.base.cj.document;

import com.graphinout.base.cj.data.CjDataProperty;
import com.graphinout.foundation.pure.json.document.IJsonFactory;
import com.graphinout.foundation.pure.json.document.IJsonValue;
import com.graphinout.foundation.pure.json.document.IJsonXmlString;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.function.Consumer;

public interface ICjHasDataMutable extends ICjHasData {

    /**
     * Add a simple property. For more complex data, use {@code dataMutable(data-> data.addProperty(key, value)); }
     *
     * @param key
     * @param value
     */
    default void addProperty(String key, String value) {
        dataMutable(data -> data.add(key, value));
    }

    @Override
    @NonNull ICjDataMutable data();

    default void dataJsonValue(@Nullable IJsonValue jsonValue) {
        if (jsonValue == null) {
            dataMutable(ICjDataMutable::removeJsonValue);
        } else {
            dataMutable(data -> data.setJsonValue(jsonValue));
        }
    }

    /**
     * If an {@link ICjDataMutable} was created (none existed) it is auto-aded to the CJ entity, if it is not empty.
     *
     * @param consumer receive current or new {@link ICjDataMutable}, never null.
     */
    void dataMutable(Consumer<ICjDataMutable> consumer);

    /**
     * Set the <em>description</em> to a plain-text string.
     *
     * @param jsonFactory
     * @param descriptionText
     */
    default void descriptionPlainText(IJsonFactory jsonFactory, String descriptionText) {
        dataMutable(data -> {
            assert data != null : "data cannot be null";
            IJsonXmlString value = IJsonXmlString.ofPlainString(jsonFactory, descriptionText);
            data.add(CjDataProperty.Description.cjPropertyKey, value);
        });
    }

}

package com.calpano.graphinout.foundation.json.stream;

import com.calpano.graphinout.foundation.json.value.IJsonTypedString;

/**
 * See {@link IJsonTypedString}.
 */
public interface IJsonTypedStringWriter extends IHasJsonValueWriter {

    /**
     * @param type  string type
     * @param value string value
     */
    default void onTypedString(String type, String value) {
        jsonValueWriter().objectStart();

        jsonValueWriter().onKey(IJsonTypedString.TYPE);
        jsonValueWriter().onString(type);

        jsonValueWriter().onKey(IJsonTypedString.VALUE);
        jsonValueWriter().onString(value);

        jsonValueWriter().objectEnd();
    }

    default void onXmlString(String value) {
        onTypedString("xml", value);
    }

}

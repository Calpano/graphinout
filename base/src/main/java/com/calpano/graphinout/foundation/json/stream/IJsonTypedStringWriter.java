package com.calpano.graphinout.foundation.json.stream;

/**
 * Our custom JSON extension: Typed strings. This transports the fact that a string should not be XML encoded when written.
 * Instead, it contains #PCDATA, i.e., XML tags, which should be emitted as-is.
 */
public interface IJsonTypedStringWriter extends IHasJsonValueWriter {

    default void onTypedString(String type, String value) {
        jsonValueWriter().objectStart();
        jsonValueWriter().onKey(type);
        jsonValueWriter().onString(value);
        jsonValueWriter().objectEnd();
    }

    default void onXmlString(String value) {
        onTypedString("xml", value);
    }

}

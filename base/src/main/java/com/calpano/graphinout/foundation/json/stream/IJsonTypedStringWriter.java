package com.calpano.graphinout.foundation.json.stream;

/**
 * Our custom JSON extension: Typed strings. This transports the fact that a string should not be XML encoded when
 * written. Instead, it contains #PCDATA, i.e., XML tags, which should be emitted as-is.
 * <p>
 * Encodes as <code>{ "type": "xml", "value": "foo<em>bar</em>" }</code>. Design alternative would have been to encode
 * as <code>{ "xml": "foo<em>bar</em>" }</code>.
 */
public interface IJsonTypedStringWriter extends IHasJsonValueWriter {

    String TYPE = "type";
    String VALUE = "value";

    /**
     * @param type  string type
     * @param value string value
     */
    default void onTypedString(String type, String value) {
        jsonValueWriter().objectStart();

        jsonValueWriter().onKey(TYPE);
        jsonValueWriter().onString(type);

        jsonValueWriter().onKey(VALUE);
        jsonValueWriter().onString(value);

        jsonValueWriter().objectEnd();
    }

    default void onXmlString(String value) {
        onTypedString("xml", value);
    }

}

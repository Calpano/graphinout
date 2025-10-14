package com.calpano.graphinout.foundation.json.stream;

import com.calpano.graphinout.foundation.json.value.IJsonXmlString;

/**
 * See {@link IJsonXmlString}.
 */
public interface IJsonXmlStringWriter extends IHasJsonValueWriter {

    static void writeXmlString(IJsonXmlString xmlString, JsonValueWriter jsonValueWriter) {
        jsonValueWriter.objectStart();

        jsonValueWriter.onKey(IJsonXmlString.XML);
        jsonValueWriter.onString(xmlString.rawXmlString());

        if (xmlString.xmlSpace() != IJsonXmlString.XML_SPACE_DEFAULT) {
            jsonValueWriter.onKey(IJsonXmlString.XML_SPACE);
            jsonValueWriter.onString(xmlString.xmlSpace().jsonStringValue);
        }

        jsonValueWriter.objectEnd();
    }

    default void onTypedString(IJsonXmlString xmlString) {
        writeXmlString(xmlString, jsonValueWriter());
    }

}

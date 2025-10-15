package com.graphinout.foundation.json.value;


import com.graphinout.foundation.json.JSON;
import com.graphinout.foundation.json.JsonType;
import com.graphinout.foundation.json.stream.JsonValueWriter;
import com.graphinout.foundation.json.stream.JsonWriter;
import com.graphinout.foundation.json.value.java.JavaJsonFactory;
import com.graphinout.foundation.xml.XML;
import com.graphinout.foundation.xml.XmlFragmentString;

import javax.annotation.Nullable;
import java.util.Objects;

import static com.graphinout.foundation.util.Nullables.mapOrDefault;

/**
 * Our custom JSON extension: XML strings. This transports the fact that a string should not be XML encoded when
 * written. Instead, it contains #PCDATA, i.e., XML tags, which should be emitted as-is.
 * <p>
 * Encodes as <code>{ "xml": "foo<em>bar</em>", "xmlSpace": :"default" }</code>. The 'xmlSpace' is optional. Design
 * alternative would have been to encode as <code>{ "type":"xml", "value": "foo<em>bar</em>" }</code>.
 * <p>
 * An XML string MAY NOT have other properties besides the defined ones.
 */
public interface IJsonXmlString extends IJsonPrimitive {

    JSON.XmlSpace XML_SPACE_DEFAULT = JSON.XmlSpace.auto;
    /** Property for XML space handling, resembling 'xml:space' attribute. Values are {@link JSON.XmlSpace}. */
    String XML_SPACE = "xmlSpace";

    /** Property to mark the JSON object as XML string and carry the actual value. XML escaping should NOT be applied. */
    String XML = "xml";


    /** Check for JSON object with 'xml' string property like <code>{ "xml":"..." }</code> */
    static boolean canParseAsJsonXmlString(IJsonValue jsonValue) {
        if (!jsonValue.isObject()) return false;
        IJsonObject o = jsonValue.asObject();
        IJsonValue propXml = o.get(XML);
        return (propXml != null && propXml.jsonType() == JsonType.String);
    }

    static IJsonXmlString of(IJsonFactory factory, @Nullable String value, JSON.XmlSpace xmlSpace) {
        assert factory != null;
        assert value != null;
        assert xmlSpace != null;
        return new JsonXmlString(factory, value, xmlSpace);
    }

    static IJsonXmlString of(JavaJsonFactory factory, XmlFragmentString xmlFragmentString) {
        assert factory != null;
        assert xmlFragmentString != null;
        return of(factory, xmlFragmentString.rawXml(), xmlFragmentString.xmlSpace().toJson_XmlSpace());
    }

    static IJsonXmlString ofJsonObject(IJsonObject jsonObject) {
        if (!canParseAsJsonXmlString(jsonObject)) {
            throw new IllegalArgumentException("JsonObject is not a valid IJsonXmlString. Missing 'xml' property. " + jsonObject.toJsonString());
        }
        String value = Objects.requireNonNull(jsonObject.get(XML)).asString();
        JSON.XmlSpace xmlSpace = mapOrDefault(jsonObject.get(XML_SPACE), IJsonValue::asString, JSON.XmlSpace::valueOf, XML_SPACE_DEFAULT);
        return IJsonXmlString.of(jsonObject.factory(), value, xmlSpace);
    }

    static IJsonXmlString ofJsonValue(IJsonValue jsonValue) {
        return switch (jsonValue.jsonType()) {
            case XmlString -> (IJsonXmlString) jsonValue;
            case String -> IJsonXmlString.ofPlainString(jsonValue.factory(), jsonValue.asString());
            case Boolean, Number -> IJsonXmlString.ofPlainString(jsonValue.factory(), jsonValue.toJsonString());
            default ->
                    throw new IllegalArgumentException("Cannot convert JSON type '" + jsonValue.jsonType() + "' to IJsonXmlString String.");
        };
    }

    static IJsonXmlString ofPlainString(IJsonFactory factory, String plainTextString) {
        return of(factory, plainTextString, XML_SPACE_DEFAULT);
    }

    static void writeTo(IJsonXmlString xmlString, JsonValueWriter jsonValueWriter) {
        jsonValueWriter.objectStart();
        jsonValueWriter.onKey(IJsonXmlString.XML);
        jsonValueWriter.onString(xmlString.rawXmlString());
        if (xmlString.xmlSpace() != JSON.XmlSpace.auto) {
            jsonValueWriter.onKey(IJsonXmlString.XML_SPACE);
            jsonValueWriter.onString(xmlString.xmlSpace().jsonStringValue);
        }
        jsonValueWriter.objectEnd();
    }

    @Override
    default void fire(JsonWriter jsonWriter) {
        writeTo(this, jsonWriter);
    }

    @Override
    default JsonType jsonType() {
        return JsonType.XmlString;
    }

    /** raw XML document fragment string. Usually has no XML declaration and may or may not have a root element. */
    String rawXmlString();

    default XmlFragmentString toXmlFragmentString() {
        XML.XmlSpace xmlSpace = xmlSpace().toXml_XmlSpace();
        String rawXml = rawXmlString();
        return XmlFragmentString.of(rawXml, xmlSpace);
    }

    JSON.XmlSpace xmlSpace();

}

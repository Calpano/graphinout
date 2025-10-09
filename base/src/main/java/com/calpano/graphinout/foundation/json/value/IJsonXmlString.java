package com.calpano.graphinout.foundation.json.value;


import com.calpano.graphinout.foundation.json.JsonType;
import com.calpano.graphinout.foundation.json.stream.JsonWriter;

import javax.annotation.Nullable;
import java.util.Objects;

import static com.calpano.graphinout.foundation.util.Nullables.mapOrDefault;

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

    enum XmlSpace {
        preserve("preserve"), ignore("ignore"), auto("default");

        public final String xmlValue;

        XmlSpace(String xmlValue) {
            this.xmlValue = xmlValue;
        }
    }

    XmlSpace XML_SPACE_DEFAULT = XmlSpace.auto;
    /** Property for XML space handling, resembling 'xml:space' attribute. Values are {@link XmlSpace}. */
    String XML_SPACE = "xmlSpace";

    /** Property to mark the JSON object as XML string and carry the actual value. XML escaping should NOT be applied. */
    String XML = "xml";

    /**
     * @param jsonValue must be an {@link IJsonObject}
     * @return as {@link IJsonXmlString}
     */
    static IJsonXmlString asJsonXmlString(IJsonValue jsonValue) {
        assert isJsonXmlString(jsonValue) : "is not a JSON XML string: '" + jsonValue.toJsonString() + "'";
        return ofJsonObject(jsonValue.asObject());
    }

    /**
     * @param jsonValue must be
     *                  <code>{ "xml":"..." }</code>
     */
    static boolean isJsonXmlString(IJsonValue jsonValue) {
        if (!jsonValue.isObject()) return false;
        IJsonObject o = jsonValue.asObject();
        // check type
        IJsonValue propXml = o.get(XML);
        return (propXml != null && propXml.jsonType() == JsonType.String);
    }

    static String jsonStringOrXmlStringToJavaString(IJsonValue jsonValue) {
        return switch (jsonValue.jsonType()) {
            case String -> jsonValue.asString();
            case XmlString -> ((IJsonXmlString) jsonValue.base()).value();
            default ->
                    throw new IllegalArgumentException("Cannot convert JSON type '" + jsonValue.jsonType() + "' to Java String.");
        };
    }

    static IJsonXmlString of(IJsonFactory factory, String value, XmlSpace xmlSpace) {
        assert factory != null;
        assert value != null;
        assert xmlSpace != null;
        return new IJsonXmlString() {

            @Override
            public Object base() {
                return this;
            }

            @Override
            public IJsonFactory factory() {
                return factory;
            }

            @Override
            public JsonType jsonType() {
                return JsonType.XmlString;
            }

            @Override
            public String toString() {
                return "IJsonXmlString{xml='" + value() + "' xmlSpace='" + xmlSpace() + "'}";
            }

            @Override
            public String value() {
                return value;
            }

            @Override
            public XmlSpace xmlSpace() {
                return xmlSpace;
            }
        };

    }

    static IJsonXmlString ofJsonObject(IJsonObject jsonObject) {
        if (!isJsonXmlString(jsonObject)) {
            throw new IllegalArgumentException("JsonObject is not a valid IJsonXmlString. Missing 'xml' property. " + jsonObject.toJsonString());
        }
        String value = Objects.requireNonNull(jsonObject.get(XML)).asString();
        XmlSpace xmlSpace = mapOrDefault(jsonObject.get(XML_SPACE), IJsonValue::asString, XmlSpace::valueOf, XML_SPACE_DEFAULT);
        return IJsonXmlString.of(jsonObject.factory(), value, xmlSpace);
    }

    @Override
    default void fire(JsonWriter jsonWriter) {
        jsonWriter.objectStart();
        jsonWriter.onKey(IJsonXmlString.XML);
        jsonWriter.onString(value());
        if (xmlSpace() != XML_SPACE_DEFAULT) {
            jsonWriter.onKey(IJsonXmlString.XML_SPACE);
            jsonWriter.onString(xmlSpace().xmlValue);
        }
        jsonWriter.objectEnd();
    }

    @Nullable
    String value();

    XmlSpace xmlSpace();

}

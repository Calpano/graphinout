package com.calpano.graphinout.reader.graphml.cj;

import com.calpano.graphinout.base.graphml.GraphmlDataType;
import com.calpano.graphinout.base.graphml.GraphmlElements;
import com.calpano.graphinout.base.graphml.GraphmlKeyForType;
import com.calpano.graphinout.base.graphml.IGraphmlDefault;
import com.calpano.graphinout.base.graphml.IGraphmlDescription;
import com.calpano.graphinout.base.graphml.IGraphmlKey;
import com.calpano.graphinout.base.graphml.builder.GraphmlKeyBuilder;
import com.calpano.graphinout.foundation.json.stream.impl.JsonReaderImpl;
import com.calpano.graphinout.foundation.json.value.IJsonObject;
import com.calpano.graphinout.foundation.json.value.IJsonTypedString;
import com.calpano.graphinout.foundation.json.value.IJsonValue;
import com.calpano.graphinout.foundation.json.value.java.JavaJsonObject;
import com.calpano.graphinout.foundation.xml.element.XmlDocument;
import com.calpano.graphinout.foundation.xml.element.XmlElement;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static com.calpano.graphinout.foundation.util.Nullables.ifPresentAccept;

/**
 * Information about all {@code <key>} elements combined.
 */
public class GraphmlSchema {

    /** key@id to key */
    private final Map<String, IGraphmlKey> keys = new HashMap<>();

    /**
     * Fetch the existing KEY elements from a Graphml XML document.
     * <p>
     * IMPROVE could also be obtained by using a streaming writer and listening only to KEY elements
     *
     * @param graphmlAsXmlDoc to analyze
     * @return schema
     */
    public static GraphmlSchema of(XmlDocument graphmlAsXmlDoc) {
        XmlElement root = graphmlAsXmlDoc.rootElement();
        GraphmlSchema schema = new GraphmlSchema();
        assert root.localName().equals(GraphmlElements.GRAPHML);
        root.directChildren().filter(node -> {
            if (node instanceof XmlElement elem) {
                return elem.localName().equals(GraphmlElements.KEY);
            }
            return false;
        }).map(node -> (XmlElement) node).forEach(keyElement -> schema.addKey(IGraphmlKey.toGraphmlKey(keyElement)));
        return schema;
    }

    public void addKey(IGraphmlKey graphmlKey) {
        IGraphmlKey prev = keys.put(graphmlKey.id(), graphmlKey);
        assert prev == null : "key already indexed: " + graphmlKey;
    }

    public List<IGraphmlKey> defaultKeysFor(GraphmlKeyForType keyForType) {
        return keys.values().stream().filter(key -> key.forType() == keyForType) //
                .filter(IGraphmlKey::definesDefaultValue)//
                .toList();
    }

    public IGraphmlKey findKeyById(String id) {
        return keys.get(id);
    }

    public void forEachKey(Consumer<IGraphmlKey> consumer) {
        // sort by 'for' then by 'id'
        keys.values().stream().sorted(Comparator.comparing(IGraphmlKey::forType).thenComparing(IGraphmlKey::id_)).forEach(consumer);
    }

    /** De-serialize */
    public GraphmlSchema fromJson(String json) {
        IJsonValue value = JsonReaderImpl.readToJsonValue(json);
        if (value instanceof IJsonObject schemaObject) {
            schemaObject.forEach((id, keyValue) -> {
                if (keyValue.isObject()) {
                    IJsonObject keyObject = keyValue.asObject();
                    GraphmlKeyBuilder builder = IGraphmlKey.builder();
                    builder.id(id);
                    ifPresentAccept(keyObject.get(IGraphmlKey.ATTRIBUTE_ATTR_NAME), IJsonValue::asString, builder::attrName);
                    ifPresentAccept(keyObject.get(IGraphmlKey.ATTRIBUTE_ATTR_TYPE), IJsonValue::asString, GraphmlDataType::fromString, builder::attrType);
                    ifPresentAccept(keyObject.get(IGraphmlKey.ATTRIBUTE_FOR), IJsonValue::asString, GraphmlKeyForType::valueOf, builder::forType);

                    ifPresentAccept(keyObject.get(GraphmlElements.DEFAULT),
                            // could be a typed JSON string with XML content
                            IJsonTypedString::asJsonTypedString, IJsonTypedString::value, IGraphmlDefault::of, builder::defaultValue);

                    ifPresentAccept(keyObject.get(GraphmlElements.DESC),
                            // could be a typed JSON string with XML content
                            IJsonTypedString::asJsonTypedString, IJsonTypedString::value, IGraphmlDescription::of, builder::desc);

                    IGraphmlKey key = builder.build();
                    keys.put(id, key);
                }
            });
        }
        return this;
    }

    public boolean isEmpty() {
        return keys.isEmpty();
    }

    public IGraphmlKey keyById(String id) {
        return keys.get(id);
    }

    public Collection<IGraphmlKey> keys() {
        return keys.values();
    }

    /** Serialize */
    public String toJson() {
        JavaJsonObject schemaObject = new JavaJsonObject();
        keys.forEach((id, key) -> {
            JavaJsonObject keyObject = new JavaJsonObject();
            schemaObject.addProperty(id, keyObject);
            String attrName = key.attrName();
            if (attrName != null) keyObject.addProperty(IGraphmlKey.ATTRIBUTE_ATTR_NAME, attrName);
            keyObject.addProperty(IGraphmlKey.ATTRIBUTE_ATTR_TYPE, key.attrType());
            keyObject.addProperty(IGraphmlKey.ATTRIBUTE_FOR, key.forType().name());
            IGraphmlDefault graphmlDefault = key.defaultValue();
            if (graphmlDefault != null) {
                keyObject.addProperty(GraphmlElements.DEFAULT, graphmlDefault.value());
            }
            IGraphmlDescription desc = key.desc();
            if (desc != null) {
                keyObject.addProperty(GraphmlElements.DESC, desc.value());
            }
            if (!key.customXmlAttributes().isEmpty()) {
                JavaJsonObject attributesObject = new JavaJsonObject();
                keyObject.addProperty("xmlAttributes", attributesObject);
                key.customXmlAttributes().forEach(attributesObject::addProperty);
            }

        });
        return schemaObject.toJsonString();
    }


}

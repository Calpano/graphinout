package com.calpano.graphinout.reader.graphml.cj;

import com.calpano.graphinout.base.graphml.CjGraphmlMapping;
import com.calpano.graphinout.base.graphml.GraphmlDataType;
import com.calpano.graphinout.base.graphml.GraphmlElements;
import com.calpano.graphinout.base.graphml.GraphmlKeyForType;
import com.calpano.graphinout.base.graphml.IGraphmlDefault;
import com.calpano.graphinout.base.graphml.IGraphmlDescription;
import com.calpano.graphinout.base.graphml.IGraphmlKey;
import com.calpano.graphinout.base.graphml.builder.GraphmlKeyBuilder;
import com.calpano.graphinout.foundation.json.value.IJsonFactory;
import com.calpano.graphinout.foundation.json.value.IJsonObject;
import com.calpano.graphinout.foundation.json.value.IJsonObjectAppendable;
import com.calpano.graphinout.foundation.json.value.IJsonValue;
import com.calpano.graphinout.foundation.json.value.IJsonXmlString;
import com.calpano.graphinout.foundation.json.value.java.JavaJsonObject;
import com.calpano.graphinout.foundation.xml.element.XmlDocument;
import com.calpano.graphinout.foundation.xml.element.XmlElement;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Stream;

import static com.calpano.graphinout.foundation.util.Nullables.ifPresentAccept;
import static java.util.Objects.requireNonNullElseGet;

/**
 * Information about all {@code <key>} elements combined.
 */
public class GraphmlSchema {

    /** key@id to key */
    private final Map<String, IGraphmlKey> keys = new TreeMap<>();
    private int keyId = 0;

    /** De-serialize */
    public static GraphmlSchema fromJson(IJsonValue value) {
        GraphmlSchema schema = new GraphmlSchema();
        if (value instanceof IJsonObject schemaObject) {
            schemaObject.forEach((id, keyValue) -> {
                if (keyValue.isObject()) {
                    IJsonObject keyObject = keyValue.asObject();
                    GraphmlKeyBuilder builder = IGraphmlKey.builder();
                    builder.id(id);
                    ifPresentAccept(keyObject.get(IGraphmlKey.ATTRIBUTE_ATTR_NAME), IJsonValue::asString, builder::attrName);
                    ifPresentAccept(keyObject.get(IGraphmlKey.ATTRIBUTE_ATTR_TYPE), IJsonValue::asString, GraphmlDataType::fromString, builder::attrType);
                    ifPresentAccept(keyObject.get(IGraphmlKey.ATTRIBUTE_FOR), IJsonValue::asString, GraphmlKeyForType::fromGraphmlName, builder::forType);

                    ifPresentAccept(keyObject.get(GraphmlElements.DEFAULT),
                            // could be a typed JSON string with XML content
                            IJsonXmlString::jsonStringOrXmlStringToJavaString, IGraphmlDefault::of, builder::defaultValue);

                    ifPresentAccept(keyObject.get(GraphmlElements.DESC),
                            // could be a typed JSON string with XML content
                            IJsonXmlString::asJsonXmlString, IJsonXmlString::value, IGraphmlDescription::of, builder::desc);

                    ifPresentAccept(keyObject.get(CjGraphmlMapping.XML_ATTRIBUTES), xmlAtts->{
                        if (!xmlAtts.isObject()) {
                            return;
                        }
                        // atts to Map
                        Map<String,String> attsMap = new HashMap<>();
                        xmlAtts.asObject().forEach((propKey,jsonValue)->{
                            if (jsonValue.isString()) {
                                attsMap.put(propKey,jsonValue.asString());
                            } else {
                                throw new IllegalArgumentException("Could not read XML attribute from "+value.toJsonString());
                            }
                        });
                        builder.attributes(attsMap);
                    });

                    IGraphmlKey key = builder.build();
                    schema.keys.put(id, key);
                }
            });
        }
        return schema;
    }

    /**
     * Fetch the existing KEY elements from a Graphml XML document.
     * <p>
     * IMPROVE could also be obtained by using a streaming writer and listening only to KEY elements
     *
     * @param graphmlAsXmlDoc to analyze
     * @return schema
     */
    @Deprecated
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

    /**
     * Convert {@link IGraphmlKey} to JSON
     *
     * @param key     to convert
     * @param factory to use
     */
    public static IJsonObject toJsonValue(IGraphmlKey key, IJsonFactory factory) {
        IJsonObjectAppendable o = factory.createObjectAppendable();

        ifPresentAccept(key.attrName(), attrName -> o.addProperty(IGraphmlKey.ATTRIBUTE_ATTR_NAME, attrName));
        o.addProperty(IGraphmlKey.ATTRIBUTE_ATTR_TYPE, key.attrType());
        o.addProperty(IGraphmlKey.ATTRIBUTE_FOR, key.forType().graphmlName());
        ifPresentAccept(key.defaultValue(), defaultValue -> o.addProperty(GraphmlElements.DEFAULT, defaultValue.value()));
        ifPresentAccept(key.desc(), desc -> o.addProperty(GraphmlElements.DESC, desc.value()));

        if (!key.customXmlAttributes().isEmpty()) {
            JavaJsonObject attributesObject = new JavaJsonObject();
            o.addProperty(CjGraphmlMapping.XML_ATTRIBUTES, attributesObject);
            key.customXmlAttributes().forEach(attributesObject::addProperty);
        }
        return o;
    }

    public void addAsUniqueKey(IGraphmlKey key) {
        String k = key.id_();
        while (keys.containsKey(k)) {
            // create a new unique key
            k = "" + (keyId++);
        }
        IGraphmlKey keyWithNewId = key.withId(k);
        addKey(keyWithNewId);
    }

    public void addKey(IGraphmlKey graphmlKey) {
        IGraphmlKey prev = keys.put(graphmlKey.id(), graphmlKey);
        assert prev == null : "key already indexed. prev=\n" + prev + " now=\n" + graphmlKey;
    }

    public List<IGraphmlKey> defaultKeysFor(GraphmlKeyForType keyForType) {
        return keys.values().stream().filter(key -> key.forType() == keyForType) //
                .filter(IGraphmlKey::definesDefaultValue)//
                .toList();
    }

    public IGraphmlKey findKeyByForAndAttrName(GraphmlKeyForType graphmlKeyForType, String attrName) {
        return keys.values().stream() //
                .filter(key -> key.forType() == graphmlKeyForType) //
                .filter(key -> key.attrName().equals(attrName))//
                .findFirst().orElse(null);
    }

    public IGraphmlKey findKeyById(String id) {
        return keys.get(id);
    }

    public boolean isEmpty() {
        return keys.isEmpty();
    }

    public IGraphmlKey keyById(String id) {
        return keys.get(id);
    }

    /** sort by (1) 'for' then by (2) 'id' -- or if that is not defined -- 'attr.name' */
    public Stream<IGraphmlKey> keys() {
        return keys.values().stream().sorted(Comparator //
                .comparing(IGraphmlKey::forType) //
                .thenComparing(key -> requireNonNullElseGet(key.attrName(), key::id_)));
    }

    public void removeKeyById(String id) {
        keys.remove(id);
    }

    /** Serialize */
    public void toJson(IJsonObjectAppendable keysObject) {
        keys().forEach(key -> {
            // filter out CJ-builtins
            if (CjGraphmlMapping.CjDataProperty.isCjPropertyKey(key.id())) {
                return;
            }
            IJsonValue keyAsJsonValue = toJsonValue(key, keysObject.factory());
            keysObject.addProperty(key.id_(), keyAsJsonValue);
        });
    }

}

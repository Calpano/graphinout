package com.graphinout.base.cj.util;

import com.graphinout.base.cj.CjConstants;
import com.graphinout.base.cj.data.CjDataProperty;
import com.graphinout.base.cj.data.CjMappedProperties;
import com.graphinout.base.cj.document.CjDocuments;
import com.graphinout.base.cj.document.ICjDocumentMeta;
import com.graphinout.base.cj.document.ICjDocumentMutable;
import com.graphinout.base.cj.document.ICjElement;
import com.graphinout.base.json.JavaJsons;
import com.graphinout.foundation.pure.json.JsonConstants;
import com.graphinout.foundation.pure.json.JsonTransformer;
import com.graphinout.foundation.pure.json.document.IJsonArrayMutable;
import com.graphinout.foundation.pure.json.document.IJsonContainer;
import com.graphinout.foundation.pure.json.document.IJsonObjectMutable;
import com.graphinout.foundation.pure.json.document.IJsonValue;
import com.graphinout.foundation.pure.json.path.JsonPaths;
import com.graphinout.foundation.pure.json.writer.impl.Json2StringWriter;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.List;

import static com.graphinout.foundation.pure.functional.Nullables.ifPresentAccept;
import static org.slf4j.LoggerFactory.getLogger;

public class CjNormalizer {

    private static final JsonTransformer.IJsonTransformHandler HANDLER = new JsonTransformer.IJsonTransformHandler() {

        @Override
        public void transformArrayPost(List<Object> steps, IJsonArrayMutable arrayValue) {
            // sort edge endpoints by nodeId
            if (JsonPaths.endsWith(steps,
                    s -> s.equals(CjConstants.GRAPH__EDGES),
                    s -> s instanceof Integer,
                    s -> s.equals(CjConstants.EDGE__ENDPOINTS))) {
                arrayValue.sort((a, b) -> {
                    String nodeIdA = a.asObject().getString(CjConstants.ENDPOINT__NODE);
                    String nodeIdB = b.asObject().getString(CjConstants.ENDPOINT__NODE);
                    return nodeIdA.compareTo(nodeIdB);
                });
            }
        }

        public void transformArrayPre(List<Object> steps, IJsonArrayMutable arrayValue) {

        }

        public void transformObjectPost(List<Object> steps, IJsonObjectMutable o) {
            // remove empty "data" {} objects
            ifPresentAccept(o.get(CjConstants.DATA), data -> {
                if (data.isContainer()) {
                    IJsonContainer c = data.asContainer();
                    if (c.isEmpty()) {
                        o.removeProperty(CjConstants.DATA);
                    }
                }
            });
            // remove "graph"/"data"/"parse.*" keys (graphml parseinfo)
            if (JsonPaths.endsWith(steps, //
                    s -> s.equals(CjConstants.GRAPHS),
                    s -> s instanceof Integer,
                    s -> s.equals(CjConstants.DATA))) {
                // remove all keys starting with "parse."
                o.removePropertyIf(k -> k.startsWith("parse."));
            }
        }

        public void transformObjectPre(List<Object> steps, IJsonObjectMutable o) {
            // strip connectedJson document metadata
            if (o.hasProperty(CjConstants.ROOT__CONNECTED_JSON)) {
                o.removeProperty(CjConstants.ROOT__CONNECTED_JSON);
            }
            // strip JSON schema refs
            if (o.hasProperty(JsonConstants.DOLLAR_ID)) {
                o.removeProperty(JsonConstants.DOLLAR_ID);
            }
            if (o.hasProperty(JsonConstants.DOLLAR_SCHEMA)) {
                o.removeProperty(JsonConstants.DOLLAR_SCHEMA);
            }
            ifPresentAccept(o.get(CjDataProperty.CustomXmlAttributes.cjPropertyKey), value -> (IJsonObjectMutable) value.asObject().asMutable(), customXmlAttributes -> {
                customXmlAttributes.keys().stream().filter(k -> k.startsWith("parse.")).toList() //
                        .forEach(customXmlAttributes::removeProperty);
                if (customXmlAttributes.isEmpty()) {
                    o.removeProperty(CjDataProperty.CustomXmlAttributes.cjPropertyKey);
                }
            });

            // remove redundant "direction": "undir" property from an endpoint
            if (JsonPaths.endsWith(steps,
                    s -> s.equals(CjConstants.GRAPH__EDGES),
                    s -> s instanceof Integer,
                    s -> s.equals(CjConstants.EDGE__ENDPOINTS),
                    s -> s instanceof Integer)) {
                if (o.hasProperty(CjConstants.ENDPOINT__DIRECTION)) {
                    IJsonValue direction = o.get(CjConstants.ENDPOINT__DIRECTION);
                    if (direction.isString() && "undir".equals(direction.asString())) {
                        o.removeProperty(CjConstants.ENDPOINT__DIRECTION);
                    }
                }
            }
        }

    };
    private static final Logger log = getLogger(CjNormalizer.class);
    private final String resultJson;

    public CjNormalizer(String cjJson) {
        String n;
        try {
            ICjDocumentMutable cjDoc = CjDocuments.parseCjJsonString("normalizing", cjJson);

            // FIXME do the normalization here
            cjDoc.connectedJson((ICjDocumentMeta) null);

            cjDoc.dataMutable(data->{
                // for comparing CJ, just ignore the graphml keys
                if (data.isNotEmpty() && data.jsonValue_().isObject()) {
                    data.remove(CjDataProperty.Keys.cjPropertyKey);
                }
                if (data.isEmpty()) {
                    data.removeJsonValue();
                }
            });

            cjDoc.graphsAll().map(ICjElement::asGraph).forEach(g -> {
                g.dataMutable(data -> {
                    // remove auto-added GraphML stats "data"."graphml:xmlAttributes"."parse." *
                    IJsonValue xmlAtts = data.getProperty(CjMappedProperties.XML_ATTRIBUTES);
                    if (xmlAtts != null && xmlAtts.isObject()) {
                        IJsonObjectMutable x = xmlAtts.asObject().mutableCopy();
                        x.removePropertyIf(p -> p.startsWith("parse."));
                        data.remove(CjMappedProperties.XML_ATTRIBUTES);
                        if (!x.isEmpty()) {
                            data.add(CjMappedProperties.XML_ATTRIBUTES, x);
                        }
                    }
                    if (data.isEmpty()) {
                        data.removeJsonValue();
                    }
                });
            });


            n = CjDocuments.toJsonString(cjDoc);
        } catch (IOException e) {
            log.warn("Could not parse to CjDoc");
            n = normalizeOnJsonLevel(cjJson);
        }
        this.resultJson = n;
    }

    public static String canonicalize(String cjJson) {
        CjNormalizer normalizer = new CjNormalizer(cjJson);
        return normalizer.resultJson;
    }

    private static String normalizeOnJsonLevel(String cjJson) {
        if (cjJson.isBlank()) {
            return "";
        }

        IJsonValue jsonValue = JavaJsons.ofJsonString(cjJson);
        // is mutated in-place
        new JsonTransformer(HANDLER).traverse(jsonValue);

        Json2StringWriter w = new Json2StringWriter();
        jsonValue.fire(w);
        return w.jsonString();
    }


}

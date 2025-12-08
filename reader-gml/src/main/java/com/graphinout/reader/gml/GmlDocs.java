package com.graphinout.reader.gml;

import com.graphinout.base.cj.document.CjDirection;
import com.graphinout.base.cj.document.ICjDocumentChunkMutable;
import com.graphinout.base.cj.document.ICjEdgeChunkMutable;
import com.graphinout.base.cj.document.ICjGraphChunkMutable;
import com.graphinout.base.cj.document.ICjHasDataMutable;
import com.graphinout.base.cj.document.ICjNodeChunkMutable;
import com.graphinout.base.cj.stream.ICjStream;
import com.graphinout.foundation.pure.json.document.IJsonArrayMutable;
import com.graphinout.foundation.pure.json.document.IJsonObjectMutable;
import com.graphinout.foundation.pure.json.document.IJsonPrimitive;
import com.graphinout.foundation.pure.json.document.IJsonValue;
import com.graphinout.foundation.pure.json.value.java.JavaJsonFactory;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.graphinout.foundation.pure.functional.Nullables.ifPresentAccept;

public class GmlDocs {

    public static final JavaJsonFactory JSON_FACTORY = JavaJsonFactory.INSTANCE;

    private static void copyDataExcept(GmlData gmlEntity, ICjHasDataMutable cjHasDataMutable, String... excludedKeys) {
        Set<String> excluded = Set.of(excludedKeys);
        gmlEntity.forEach((key, value) -> {
            if (excluded.contains(key)) return;

            if (value instanceof List<?> list) {
                // convert to array
                IJsonArrayMutable array = JSON_FACTORY.createArrayMutable();
                for (Object o : list) {
                    if (o instanceof String s) {
                        array.add(JSON_FACTORY.createString(s));
                    } else if (o instanceof GmlData d) {
                        array.add(d.toJsonValue());
                    } else {
                        throw new IllegalStateException("Unexpected list element type: " + o.getClass());
                    }
                }
                cjHasDataMutable.dataMutable(d -> d.addProperty(key, array));
            } else if (value instanceof GmlData gmlData) {
                // first create sub-json object, then add it
                IJsonValue jsonValue = gmlData.toJsonValue();
                cjHasDataMutable.dataMutable(d -> d.addProperty(key, jsonValue));
            } else if (value instanceof IJsonPrimitive primitive) {
                cjHasDataMutable.dataMutable(d -> d.addProperty(key, primitive));
            } else if (value instanceof Object o) {
                assert GmlData.isJavaJson(o);
                IJsonValue jsonValue = GmlDocs.toJsonValue(o);
                cjHasDataMutable.dataMutable(d -> d.addProperty(key, jsonValue));
            }

        });
    }

    public static void toCjDocument(GmlData gmlDoc, ICjStream cjStream) {
        ICjDocumentChunkMutable cjDoc = cjStream.createDocumentChunk();
        // doc metadata
        gmlDoc.forEachExcept((kev, val) -> {
            IJsonValue jsonValue = toJsonValue(val);
            cjDoc.dataMutable(d -> d.addProperty(kev, jsonValue));
        }, Gml.GRAPH);
        cjStream.documentStart(cjDoc);

        gmlDoc.forEachChild(Gml.GRAPH, gmlGraph -> toCjGraph(gmlGraph, cjStream));

        cjStream.documentEnd();
    }

    private static void toCjEdge(GmlData gmlEdge, ICjStream cjStream) {
        ICjEdgeChunkMutable cjEdge = cjStream.createEdgeChunk();
        // standard attributes
        String sourceId = gmlEdge.get(Gml.SOURCE);
        String targetId = gmlEdge.get(Gml.TARGET);
        ifPresentAccept(gmlEdge.get(Gml.ID), cjEdge::id);
        ifPresentAccept(gmlEdge.get(Gml.LABEL), cjEdge::addLabelWithoutLanguage);
        cjEdge.addEndpoint(ep -> ep.direction(CjDirection.IN).node(sourceId));
        cjEdge.addEndpoint(ep -> ep.direction(CjDirection.OUT).node(targetId));
        // extended
        copyDataExcept(gmlEdge, cjEdge, Gml.SOURCE, Gml.TARGET, Gml.ID, Gml.LABEL);

        cjStream.edge(cjEdge);
    }

    private static void toCjGraph(GmlData gmlGraph, ICjStream cjStream) {
        ICjGraphChunkMutable cjGraph = cjStream.createGraphChunk();
        // standard attributes
        ifPresentAccept(gmlGraph.get(Gml.ID), cjGraph::id);
        ifPresentAccept(gmlGraph.get(Gml.LABEL), cjGraph::addLabelWithoutLanguage);
        // graph metadata
        copyDataExcept(gmlGraph,cjGraph, Gml.NODE, Gml.EDGE,Gml.ID, Gml.LABEL);

        cjStream.graphStart(cjGraph);
        gmlGraph.forEachChild(Gml.NODE, gmlNode -> toCjNode(gmlNode, cjStream));
        gmlGraph.forEachChild(Gml.EDGE, gmlEdge -> toCjEdge(gmlEdge, cjStream));
        cjStream.graphEnd();
    }

    private static void toCjNode(GmlData gmlNode, ICjStream cjStream) {
        ICjNodeChunkMutable cjNode = cjStream.createNodeChunk();
        // standard attributes
        ifPresentAccept(gmlNode.get(Gml.ID), cjNode::id);
        ifPresentAccept(gmlNode.get(Gml.LABEL), cjNode::addLabelWithoutLanguage);
        // node metadata
        copyDataExcept(gmlNode, cjNode, Gml.ID, Gml.LABEL);

        cjStream.node(cjNode);
    }

    /**
     * @param val "java json" = null, bool, String (here: may be quoted), Number, List, Map
     */
    public static IJsonValue toJsonValue(Object val) {
        if (val instanceof String string) {
            if (string.startsWith("\"") && string.endsWith("\"")) {
                return JSON_FACTORY.createString(string.substring(1, string.length() - 1));
            } else if (string.startsWith("'") && string.endsWith("'")) {
                return JSON_FACTORY.createString(string.substring(1, string.length() - 1));
            } else {
                // bool, number, string
                if (string.equalsIgnoreCase("true")) {
                    return JSON_FACTORY.createBoolean(true);
                } else if (string.equalsIgnoreCase("false")) {
                    return JSON_FACTORY.createBoolean(false);
                } else {
                    try {
                        return JSON_FACTORY.createNumber(Double.parseDouble(string));
                    } catch (NumberFormatException e) {
                        // fall-back
                        return JSON_FACTORY.createString(string);
                    }
                }
            }
        } else if (val instanceof GmlData gmlData) {
            // nested JSON object
            return gmlData.toJsonValue();
        } else if (val instanceof List list) {
            // nested JSON arra
            IJsonArrayMutable array = JSON_FACTORY.createArrayMutable();
            for (Object o : list) {
                array.add(toJsonValue(o));
            }
            return array;
        } else if (val instanceof Map map) {
            // nested JSON arra
            IJsonObjectMutable object = JSON_FACTORY.createObjectMutable();
            map.forEach((k, v) -> {
                object.addProperty((String) k, toJsonValue(v));
            });
            return object;
        } else {
            throw new IllegalArgumentException("Unknown type " + val.getClass());
        }
    }

}

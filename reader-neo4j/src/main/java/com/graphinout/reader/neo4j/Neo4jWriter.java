package com.graphinout.reader.neo4j;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.graphinout.base.cj.document.*;
import com.graphinout.base.cj.factory.BaseCjOutput;
import com.graphinout.base.cj.stream.ICjStream;
import com.graphinout.base.output.OutputSink;
import com.graphinout.foundation.pure.json.document.IJsonFactory;
import com.graphinout.foundation.pure.json.document.IJsonValue;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Writes CJ documents to Neo4j APOC JSON export format (JSON Lines).
 */
public class Neo4jWriter extends BaseCjOutput implements ICjStream {

    private final OutputSink outputSink;
    private final ObjectMapper objectMapper;
    private boolean wroteAnything = false;

    public Neo4jWriter(OutputSink outputSink) {
        this.outputSink = outputSink;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public void documentStart(ICjDocumentChunk document) {
        // No-op: Neo4j JSON Lines format has no document wrapper
    }

    @Override
    public void documentEnd() {
        // A Neo4j property graph is nodes + relationships only. A document with no nodes/edges (e.g. only
        // document-level custom data) produces no output; report that loss instead of writing silence.
        if (!wroteAnything) {
            sendContentError_Warn("No nodes or edges to export to Neo4j; the document has no graph content");
        }
    }

    @Override
    public void graphStart(ICjGraphChunk graph) {
        // No-op: Neo4j JSON Lines format has no graph wrapper
    }

    @Override
    public void graphEnd() {
        // No-op: Neo4j JSON Lines format has no graph wrapper
    }

    @Override
    public void nodeStart(ICjNodeChunk node) {
        try {
            Map<String, Object> nodeMap = new HashMap<>();
            nodeMap.put("type", "node");

            if (node.id() != null) {
                nodeMap.put("id", node.id());
            }

            // Labels
            if (node.label() != null) {
                List<String> labels = new java.util.ArrayList<>();
                List<ICjLabelEntry> entryList = node.label().entries().toList();
                for (ICjLabelEntry entry : entryList) {
                    labels.add(entry.value());
                }
                if (!labels.isEmpty()) {
                    nodeMap.put("labels", labels);
                }
            }

            // Properties from data
            if (node.data() != null && node.data().jsonValue() != null) {
                Map<String, Object> properties = extractProperties(node.data().jsonValue());
                if (!properties.isEmpty()) {
                    nodeMap.put("properties", properties);
                }
            }

            writeJsonLine(nodeMap);
        } catch (IOException e) {
            throw new RuntimeException("Failed to write node", e);
        }
    }

    @Override
    public void nodeEnd() {
        // No-op: node already written in nodeStart
    }

    @Override
    public void edgeStart(ICjEdgeChunk edge) {
        try {
            Map<String, Object> edgeMap = new HashMap<>();
            edgeMap.put("type", "relationship");

            if (edge.id() != null) {
                edgeMap.put("id", edge.id());
            }

            // Label
            if (edge.label() != null) {
                List<ICjLabelEntry> entryList = edge.label().entries().toList();
                if (!entryList.isEmpty()) {
                    edgeMap.put("label", entryList.get(0).value());
                }
            }

            // Extract source and target from endpoints
            List<ICjEndpoint> sources = edge.sources();
            List<ICjEndpoint> targets = edge.targets();

            if (!sources.isEmpty()) {
                Map<String, String> start = new HashMap<>();
                start.put("id", sources.get(0).node());
                edgeMap.put("start", start);
            }

            if (!targets.isEmpty()) {
                Map<String, String> end = new HashMap<>();
                end.put("id", targets.get(0).node());
                edgeMap.put("end", end);
            }

            // Properties from data
            if (edge.data() != null && edge.data().jsonValue() != null) {
                Map<String, Object> properties = extractProperties(edge.data().jsonValue());
                if (!properties.isEmpty()) {
                    edgeMap.put("properties", properties);
                }
            }

            writeJsonLine(edgeMap);
        } catch (IOException e) {
            throw new RuntimeException("Failed to write edge", e);
        }
    }

    @Override
    public void edgeEnd() {
        // No-op: edge already written in edgeStart
    }

    private void writeJsonLine(Map<String, Object> object) throws IOException {
        String json = objectMapper.writeValueAsString(object);
        outputSink.write(json + "\n");
        wroteAnything = true;
    }

    private Map<String, Object> extractProperties(IJsonValue jsonValue) {
        Map<String, Object> properties = new HashMap<>();

        if (jsonValue.isObject()) {
            com.graphinout.foundation.pure.json.document.IJsonObject jsonObject = jsonValue.asObject();
            for (String key : jsonObject.keys()) {
                IJsonValue value = jsonObject.get(key);
                if (value != null) {
                    properties.put(key, convertJsonValue(value));
                }
            }
        }

        return properties;
    }

    private Object convertJsonValue(IJsonValue value) {
        if (value.isString()) {
            return value.asString();
        } else if (value.isNumber()) {
            return value.asNumber();
        } else if (value.isBoolean()) {
            return value.asBoolean();
        } else if (value.isNull()) {
            return null;
        }
        return value.toString();
    }

    @Override
    public IJsonFactory jsonFactory() {
        return IJsonFactory.INSTANCE;
    }
}

package com.graphinout.reader.pgformat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.graphinout.base.cj.document.*;
import com.graphinout.base.cj.factory.BaseCjOutput;
import com.graphinout.base.cj.stream.ICjStream;
import com.graphinout.base.output.OutputSink;
import com.graphinout.foundation.pure.json.document.IJsonFactory;
import com.graphinout.foundation.pure.json.document.IJsonValue;

import java.io.IOException;
import java.util.*;

/**
 * Writes CJ documents to PG-JSON (Property Graph JSON) format.
 * Specification: https://pg-format.github.io/specification/
 */
public class PgJsonWriter extends BaseCjOutput implements ICjStream {

    private final OutputSink outputSink;
    private final ObjectMapper objectMapper;
    private final List<Map<String, Object>> nodes = new ArrayList<>();
    private final List<Map<String, Object>> edges = new ArrayList<>();

    public PgJsonWriter(OutputSink outputSink) {
        this.outputSink = outputSink;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public void documentStart(ICjDocumentChunk document) {
        // Collect nodes and edges in memory
    }

    @Override
    public void documentEnd() {
        // Write the complete PG-JSON document
        try {
            Map<String, Object> pgJson = new LinkedHashMap<>();
            pgJson.put("nodes", nodes);
            pgJson.put("edges", edges);

            String json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(pgJson);
            outputSink.write(json);
        } catch (IOException e) {
            throw new RuntimeException("Failed to write PG-JSON", e);
        }
    }

    @Override
    public void graphStart(ICjGraphChunk graph) {
        // No-op: PG-JSON doesn't have explicit graph wrappers
    }

    @Override
    public void graphEnd() {
        // No-op: PG-JSON doesn't have explicit graph wrappers
    }

    @Override
    public void nodeStart(ICjNodeChunk node) {
        Map<String, Object> nodeMap = new LinkedHashMap<>();

        // ID (mandatory in PG-JSON)
        if (node.id() != null) {
            nodeMap.put("id", node.id());
        } else {
            // Generate an ID if not present
            nodeMap.put("id", "node_" + nodes.size());
        }

        // Labels (mandatory in PG-JSON, must be non-empty array)
        List<String> labels = new ArrayList<>();
        if (node.label() != null) {
            List<ICjLabelEntry> entryList = node.label().entries().toList();
            for (ICjLabelEntry entry : entryList) {
                labels.add(entry.value());
            }
        }
        if (labels.isEmpty()) {
            labels.add("Node"); // Default label
        }
        nodeMap.put("labels", labels);

        // Properties (mandatory in PG-JSON, can be empty object)
        Map<String, List<Object>> properties = new LinkedHashMap<>();
        if (node.data() != null && node.data().jsonValue() != null) {
            extractProperties(node.data().jsonValue(), properties);
        }
        nodeMap.put("properties", properties);

        nodes.add(nodeMap);
    }

    @Override
    public void nodeEnd() {
        // No-op: node already added in nodeStart
    }

    @Override
    public void edgeStart(ICjEdgeChunk edge) {
        Map<String, Object> edgeMap = new LinkedHashMap<>();

        // ID (optional in PG-JSON, can be null)
        if (edge.id() != null) {
            edgeMap.put("id", edge.id());
        }

        // Determine direction and endpoints
        List<ICjEndpoint> sources = edge.sources();
        List<ICjEndpoint> targets = edge.targets();
        List<ICjEndpoint> undirected = edge.undirectedEndpoints();

        boolean isUndirected = false;
        String from = null;
        String to = null;

        if (!undirected.isEmpty() && undirected.size() >= 2) {
            // Undirected edge
            isUndirected = true;
            from = undirected.get(0).node();
            to = undirected.get(1).node();
        } else if (!sources.isEmpty() && !targets.isEmpty()) {
            // Directed edge
            from = sources.get(0).node();
            to = targets.get(0).node();
        } else if (!sources.isEmpty() && undirected.size() >= 1) {
            from = sources.get(0).node();
            to = undirected.get(0).node();
        } else if (!targets.isEmpty() && undirected.size() >= 1) {
            from = undirected.get(0).node();
            to = targets.get(0).node();
        }

        // From and To (mandatory in PG-JSON)
        if (from != null && to != null) {
            edgeMap.put("from", from);
            edgeMap.put("to", to);
        }

        // Undirected flag (optional, defaults to false)
        if (isUndirected) {
            edgeMap.put("undirected", true);
        }

        // Labels (mandatory in PG-JSON, must be non-empty array)
        List<String> labels = new ArrayList<>();
        if (edge.label() != null) {
            List<ICjLabelEntry> entryList = edge.label().entries().toList();
            for (ICjLabelEntry entry : entryList) {
                labels.add(entry.value());
            }
        }
        if (labels.isEmpty()) {
            labels.add("Edge"); // Default label
        }
        edgeMap.put("labels", labels);

        // Properties (mandatory in PG-JSON, can be empty object)
        Map<String, List<Object>> properties = new LinkedHashMap<>();
        if (edge.data() != null && edge.data().jsonValue() != null) {
            extractProperties(edge.data().jsonValue(), properties);
        }
        edgeMap.put("properties", properties);

        edges.add(edgeMap);
    }

    @Override
    public void edgeEnd() {
        // No-op: edge already added in edgeStart
    }

    private void extractProperties(IJsonValue jsonValue, Map<String, List<Object>> properties) {
        if (jsonValue.isObject()) {
            com.graphinout.foundation.pure.json.document.IJsonObject jsonObject = jsonValue.asObject();
            for (String key : jsonObject.keys()) {
                IJsonValue value = jsonObject.get(key);
                if (value != null) {
                    Object converted = convertJsonValue(value);
                    // PG-JSON requires arrays of values
                    properties.put(key, Collections.singletonList(converted));
                }
            }
        }
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

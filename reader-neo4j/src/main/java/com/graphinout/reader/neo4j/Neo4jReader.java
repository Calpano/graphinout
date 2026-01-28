package com.graphinout.reader.neo4j;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.graphinout.base.cj.document.ICjHasDataMutable;
import com.graphinout.base.cj.stream.ICjStream;
import com.graphinout.base.gio.GioFileFormat;
import com.graphinout.base.gio.GioReader;
import com.graphinout.base.gio.GioWriter;
import com.graphinout.base.input.InputSource;
import com.graphinout.base.output.OutputSink;
import com.graphinout.foundation.pure.input.ContentError;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Reads and writes Neo4j APOC JSON export format.
 * Supports JSON Lines format (one JSON object per line).
 */
public class Neo4jReader implements GioReader, GioWriter {

    public static final String FORMAT_ID = "neo4j-json";
    public static final GioFileFormat FORMAT = new GioFileFormat(FORMAT_ID, "Neo4j APOC JSON Export Format",
            ".neo4j.json", ".neo4j-export.json");

    private @Nullable Consumer<ContentError> errorHandler;
    private final JsonFactory jsonFactory = new JsonFactory();

    @Override
    public GioFileFormat fileFormat() {
        return FORMAT;
    }

    @Override
    public void read(InputSource inputSource, ICjStream cjStream) throws IOException {
        try (InputStream is = inputSource.asSingle().inputStream()) {
            JsonParser parser = jsonFactory.createParser(is);

            // Start document and graph
            cjStream.documentStart(cjStream.createDocumentChunk());
            cjStream.graphStart(cjStream.createGraphChunk());

            // Process JSON lines
            while (parser.nextToken() != null) {
                if (parser.getCurrentToken() == JsonToken.START_OBJECT) {
                    processObject(parser, cjStream);
                }
            }

            // End graph and document
            cjStream.graphEnd();
            cjStream.documentEnd();

            parser.close();
        }
    }

    private void processObject(JsonParser parser, ICjStream cjStream) throws IOException {
        Map<String, Object> object = new HashMap<>();

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String fieldName = parser.getCurrentName();
            parser.nextToken();

            switch (fieldName) {
                case "type":
                    object.put("type", parser.getText());
                    break;
                case "id":
                    object.put("id", parser.getText());
                    break;
                case "labels":
                    object.put("labels", parseArray(parser));
                    break;
                case "label":
                    object.put("label", parser.getText());
                    break;
                case "properties":
                    object.put("properties", parseProperties(parser));
                    break;
                case "start":
                    object.put("start", parseNodeReference(parser));
                    break;
                case "end":
                    object.put("end", parseNodeReference(parser));
                    break;
                case "rel":
                    // Handle wrapped relationship format
                    if (parser.getCurrentToken() == JsonToken.START_OBJECT) {
                        processObject(parser, cjStream);
                        return;
                    }
                    break;
                default:
                    // Skip unknown fields
                    parser.skipChildren();
                    break;
            }
        }

        // Process the object based on type
        String type = (String) object.get("type");
        if ("node".equals(type)) {
            processNode(object, cjStream);
        } else if ("relationship".equals(type)) {
            processRelationship(object, cjStream);
        }
    }

    private void processNode(Map<String, Object> nodeData, ICjStream cjStream) {
        cjStream.node(node -> {
            String id = (String) nodeData.get("id");
            if (id != null) {
                node.id(id);
            }

            @SuppressWarnings("unchecked")
            java.util.List<String> labels = (java.util.List<String>) nodeData.get("labels");
            if (labels != null && !labels.isEmpty()) {
                for (String label : labels) {
                    node.addLabelWithoutLanguage(label);
                }
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> properties = (Map<String, Object>) nodeData.get("properties");
            if (properties != null) {
                for (Map.Entry<String, Object> entry : properties.entrySet()) {
                    addPropertyToData(node, entry.getKey(), entry.getValue());
                }
            }
        });
    }

    private void processRelationship(Map<String, Object> relData, ICjStream cjStream) {
        cjStream.edge(edge -> {
            String id = (String) relData.get("id");
            if (id != null) {
                edge.id(id);
            }

            String label = (String) relData.get("label");
            if (label != null) {
                edge.setLabel(lbl -> lbl.addEntry(entry -> entry.value(label)));
            }

            @SuppressWarnings("unchecked")
            Map<String, String> start = (Map<String, String>) relData.get("start");
            if (start != null && start.get("id") != null) {
                edge.addEndpointOutgoing(start.get("id"));
            }

            @SuppressWarnings("unchecked")
            Map<String, String> end = (Map<String, String>) relData.get("end");
            if (end != null && end.get("id") != null) {
                edge.addEndpointIncoming(end.get("id"));
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> properties = (Map<String, Object>) relData.get("properties");
            if (properties != null) {
                for (Map.Entry<String, Object> entry : properties.entrySet()) {
                    addPropertyToData(edge, entry.getKey(), entry.getValue());
                }
            }
        });
    }

    private java.util.List<String> parseArray(JsonParser parser) throws IOException {
        java.util.List<String> list = new java.util.ArrayList<>();
        while (parser.nextToken() != JsonToken.END_ARRAY) {
            list.add(parser.getText());
        }
        return list;
    }

    private Map<String, Object> parseProperties(JsonParser parser) throws IOException {
        Map<String, Object> properties = new HashMap<>();
        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String key = parser.getCurrentName();
            parser.nextToken();
            properties.put(key, parseValue(parser));
        }
        return properties;
    }

    private Map<String, String> parseNodeReference(JsonParser parser) throws IOException {
        Map<String, String> nodeRef = new HashMap<>();
        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String key = parser.getCurrentName();
            parser.nextToken();
            if ("id".equals(key)) {
                nodeRef.put("id", parser.getText());
            } else {
                parser.skipChildren();
            }
        }
        return nodeRef;
    }

    private Object parseValue(JsonParser parser) throws IOException {
        switch (parser.getCurrentToken()) {
            case VALUE_STRING:
                return parser.getText();
            case VALUE_NUMBER_INT:
                return parser.getIntValue();
            case VALUE_NUMBER_FLOAT:
                return parser.getDoubleValue();
            case VALUE_TRUE:
                return Boolean.TRUE;
            case VALUE_FALSE:
                return Boolean.FALSE;
            case VALUE_NULL:
                return null;
            default:
                parser.skipChildren();
                return null;
        }
    }

    private void addPropertyToData(ICjHasDataMutable element, String key, Object value) {
        if (value == null) {
            return;
        }
        element.dataMutable(data -> {
            if (value instanceof String) {
                data.add(key, (String) value);
            } else if (value instanceof Integer || value instanceof Long ||
                       value instanceof Double || value instanceof Float) {
                data.add(key, (Number) value);
            } else if (value instanceof Boolean) {
                data.add(key, (Boolean) value);
            } else {
                data.add(key, value.toString());
            }
        });
    }

    @Override
    public void setContentErrorHandler(Consumer<ContentError> errorHandler) {
        this.errorHandler = errorHandler;
    }

    @Override
    public ICjStream createCjStream(OutputSink outputSink) {
        return new Neo4jWriter(outputSink);
    }
}

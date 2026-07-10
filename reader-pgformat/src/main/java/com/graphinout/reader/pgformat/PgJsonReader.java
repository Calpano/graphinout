package com.graphinout.reader.pgformat;

import tools.jackson.core.json.JsonFactory;
import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Reads and writes PG-JSON (Property Graph JSON) format.
 * Specification: <a href="https://pg-format.github.io/specification/">https://pg-format.github.io/specification/</a>
 */
public class PgJsonReader implements GioReader, GioWriter {

    public static final String FORMAT_ID = "pg-json";
    public static final GioFileFormat FORMAT = new GioFileFormat(FORMAT_ID, "PG-JSON (Property Graph Format)",
            ".pg.json", ".pgjson");

    private @Nullable Consumer<ContentError> errorHandler;
    private final JsonFactory jsonFactory = new JsonFactory();

    @Override
    public GioFileFormat fileFormat() {
        return FORMAT;
    }

    @Override
    public void read(InputSource inputSource, ICjStream cjStream) throws IOException {
        try (InputStream is = inputSource.asSingle().inputStream()) {
            JsonParser parser = jsonFactory.createParser(tools.jackson.core.ObjectReadContext.empty(), is);

            // Start document and graph
            cjStream.documentStart(cjStream.createDocumentChunk());
            cjStream.graphStart(cjStream.createGraphChunk());

            // Expect start of object
            if (parser.nextToken() != JsonToken.START_OBJECT) {
                throw new IOException("Expected JSON object at root");
            }

            // Process nodes and edges
            while (parser.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = parser.currentName();
                parser.nextToken();

                if ("nodes".equals(fieldName)) {
                    processNodes(parser, cjStream);
                } else if ("edges".equals(fieldName)) {
                    processEdges(parser, cjStream);
                } else {
                    warnUnknownField("graph", fieldName);
                    parser.skipChildren();
                }
            }

            // End graph and document
            cjStream.graphEnd();
            cjStream.documentEnd();

            parser.close();
        }
    }

    private void processNodes(JsonParser parser, ICjStream cjStream) throws IOException {
        if (parser.currentToken() != JsonToken.START_ARRAY) {
            throw new IOException("Expected array for 'nodes'");
        }

        while (parser.nextToken() != JsonToken.END_ARRAY) {
            processNode(parser, cjStream);
        }
    }

    private void processNode(JsonParser parser, ICjStream cjStream) throws IOException {
        Map<String, Object> nodeData = new HashMap<>();

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String fieldName = parser.currentName();
            parser.nextToken();

            switch (fieldName) {
                case "id":
                    nodeData.put("id", parser.getString());
                    break;
                case "labels":
                    nodeData.put("labels", parseStringArray(parser));
                    break;
                case "properties":
                    nodeData.put("properties", parseProperties(parser));
                    break;
                default:
                    warnUnknownField("node", fieldName);
                    parser.skipChildren();
                    break;
            }
        }

        // Create CJ node
        cjStream.node(node -> {
            String id = (String) nodeData.get("id");
            if (id != null) {
                node.id(id);
            }

            @SuppressWarnings("unchecked")
            List<String> labels = (List<String>) nodeData.get("labels");
            if (labels != null) {
                for (String label : labels) {
                    node.addLabelWithoutLanguage(label);
                }
            }

            @SuppressWarnings("unchecked")
            Map<String, List<Object>> properties = (Map<String, List<Object>>) nodeData.get("properties");
            if (properties != null) {
                addPropertiesToData(node, properties);
            }
        });
    }

    private void processEdges(JsonParser parser, ICjStream cjStream) throws IOException {
        if (parser.currentToken() != JsonToken.START_ARRAY) {
            throw new IOException("Expected array for 'edges'");
        }

        while (parser.nextToken() != JsonToken.END_ARRAY) {
            processEdge(parser, cjStream);
        }
    }

    private void processEdge(JsonParser parser, ICjStream cjStream) throws IOException {
        Map<String, Object> edgeData = new HashMap<>();

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String fieldName = parser.currentName();
            parser.nextToken();

            switch (fieldName) {
                case "id":
                    if (parser.currentToken() != JsonToken.VALUE_NULL) {
                        edgeData.put("id", parser.getString());
                    }
                    break;
                case "from":
                    edgeData.put("from", parser.getString());
                    break;
                case "to":
                    edgeData.put("to", parser.getString());
                    break;
                case "undirected":
                    edgeData.put("undirected", parser.getBooleanValue());
                    break;
                case "labels":
                    edgeData.put("labels", parseStringArray(parser));
                    break;
                case "properties":
                    edgeData.put("properties", parseProperties(parser));
                    break;
                default:
                    warnUnknownField("edge", fieldName);
                    parser.skipChildren();
                    break;
            }
        }

        // Create CJ edge
        cjStream.edge(edge -> {
            String id = (String) edgeData.get("id");
            if (id != null) {
                edge.id(id);
            }

            @SuppressWarnings("unchecked")
            List<String> labels = (List<String>) edgeData.get("labels");
            if (labels != null && !labels.isEmpty()) {
                edge.setLabel(lbl -> lbl.addEntry(entry -> entry.value(labels.getFirst())));
            }

            String from = (String) edgeData.get("from");
            String to = (String) edgeData.get("to");
            Boolean undirected = (Boolean) edgeData.get("undirected");

            if (from != null && to != null) {
                if (Boolean.TRUE.equals(undirected)) {
                    // Undirected edge
                    edge.addEndpointUndirected(from);
                    edge.addEndpointUndirected(to);
                } else {
                    // Directed edge: from -> to
                    edge.addEndpointOutgoing(from);
                    edge.addEndpointIncoming(to);
                }
            }

            @SuppressWarnings("unchecked")
            Map<String, List<Object>> properties = (Map<String, List<Object>>) edgeData.get("properties");
            if (properties != null) {
                addPropertiesToData(edge, properties);
            }
        });
    }

    private List<String> parseStringArray(JsonParser parser) throws IOException {
        List<String> list = new ArrayList<>();
        while (parser.nextToken() != JsonToken.END_ARRAY) {
            list.add(parser.getString());
        }
        return list;
    }

    private Map<String, List<Object>> parseProperties(JsonParser parser) throws IOException {
        Map<String, List<Object>> properties = new HashMap<>();

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String key = parser.currentName();
            parser.nextToken();

            List<Object> values = new ArrayList<>();
            while (parser.nextToken() != JsonToken.END_ARRAY) {
                values.add(parseValue(parser));
            }
            properties.put(key, values);
        }

        return properties;
    }

    private Object parseValue(JsonParser parser) throws IOException {
        return switch (parser.currentToken()) {
            case VALUE_STRING -> parser.getString();
            case VALUE_NUMBER_INT -> parser.getIntValue();
            case VALUE_NUMBER_FLOAT -> parser.getDoubleValue();
            case VALUE_TRUE -> Boolean.TRUE;
            case VALUE_FALSE -> Boolean.FALSE;
            case VALUE_NULL -> null;
            default -> null;
        };
    }

    private void addPropertiesToData(ICjHasDataMutable element, Map<String, List<Object>> properties) {
        for (Map.Entry<String, List<Object>> entry : properties.entrySet()) {
            String key = entry.getKey();
            List<Object> values = entry.getValue();

            // PG-JSON allows arrays of values, but CJ typically has single values
            // We'll take the first value if available
            if (values != null && !values.isEmpty()) {
                Object value = values.getFirst();
                if (value != null) {
                    element.dataMutable(data -> {
                        switch (value) {
                            case String s -> data.add(key, s);
                            case Number n -> data.add(key, n);
                            case Boolean b -> data.add(key, b);
                            default -> { }
                        }
                    });
                }
            }
        }
    }

    @Override
    public void setContentErrorHandler(Consumer<ContentError> errorHandler) {
        this.errorHandler = errorHandler;
    }

    private void warnUnknownField(String context, String fieldName) {
        if (errorHandler != null) {
            errorHandler.accept(ContentError.warn("Unknown field in " + context + ": " + fieldName));
        }
    }

    @Override
    public ICjStream createCjStream(OutputSink outputSink) {
        return new PgJsonWriter(outputSink);
    }
}

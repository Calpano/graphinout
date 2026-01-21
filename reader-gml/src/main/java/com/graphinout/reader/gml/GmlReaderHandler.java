package com.graphinout.reader.gml;

import com.graphinout.base.cj.document.CjDirection;
import com.graphinout.base.cj.document.ICjChunkMutable;
import com.graphinout.base.cj.document.ICjDocumentChunk;
import com.graphinout.base.cj.document.ICjDocumentChunkMutable;
import com.graphinout.base.cj.document.ICjEdgeChunkMutable;
import com.graphinout.base.cj.document.ICjGraphChunk;
import com.graphinout.base.cj.document.ICjGraphChunkMutable;
import com.graphinout.base.cj.document.ICjNodeChunkMutable;
import com.graphinout.base.cj.stream.ICjStream;
import com.graphinout.foundation.pure.json.document.IJsonFactory;
import com.graphinout.foundation.pure.json.document.IJsonObjectMutable;
import com.graphinout.foundation.pure.json.document.IJsonValue;
import org.jspecify.annotations.NonNull;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.IdentityHashMap;
import java.util.Map;

public class GmlReaderHandler implements IGmlHandler {

    public static final String LABEL = "label";
    public static final String ID = "id";
    private final ICjStream writer;
    /** {@link ICjChunkMutable} or {@link GmlData} */
    private final Deque<Object> stack = new ArrayDeque<>();
    private final Map<ICjGraphChunkMutable, Boolean> startedGraphs = new IdentityHashMap<>();
    private final ICjDocumentChunkMutable documentChunk;
    private boolean documentStarted;
    private String lastKey;

    public GmlReaderHandler(ICjStream writer) {
        this.writer = writer;
        this.documentChunk = writer.createDocumentChunk();
        this.stack.push(documentChunk);
        // defer documentStart until we have accumulated top-level attributes or first graph
        this.documentStarted = false;
    }

    @Override
    public void close() {
        Object closedChunk = stack.pop();
        if (closedChunk instanceof ICjChunkMutable chunk) {
            switch (chunk) {
                case ICjDocumentChunk doc -> throw new IllegalStateException();
                case ICjGraphChunk graph -> {
                    if (startedGraphs.getOrDefault(graph, Boolean.FALSE) == Boolean.FALSE) {
                        writer.graphStart(graph);
                    }
                    writer.graphEnd();
                    startedGraphs.remove(graph);
                }
                case ICjNodeChunkMutable node -> writer.node(node);
                case ICjEdgeChunkMutable edge -> writer.edge(edge);
                default -> {
                }
            }
        } else {
            assert closedChunk instanceof IJsonObjectMutable;
            IJsonObjectMutable o = (IJsonObjectMutable) closedChunk;
            // nothing to do with it
        }
    }

    public void endDocument() {
        // auto-fix malformed GML: close any still-open contexts except DOCUMENT
        while (!stack.isEmpty() && !(stack.peek() instanceof ICjDocumentChunk)) {
            close();
        }
        ensureDocumentStarted();
        writer.documentEnd();
    }

    @Override
    public void key(String key) {
        this.lastKey = key;
    }

    @Override
    public void open() {
        if (lastKey == null) {
            throw new IllegalStateException("Missing key before brackets");
        }

        switch (lastKey) {
            case Gml.GRAPH -> {
                ensureDocumentStarted();
                ICjGraphChunkMutable graphChunk = writer.createGraphChunk();
                stack.push(graphChunk);
                // defer starting graph until we see first child or on close, so attributes can be applied first
                startedGraphs.put(graphChunk, Boolean.FALSE);
            }
            case Gml.NODE -> {
                ensureGraphStarted();
                ICjNodeChunkMutable nodeChunk = writer.createNodeChunk();
                stack.push(nodeChunk);
            }
            case Gml.EDGE -> {
                ensureGraphStarted();
                ICjEdgeChunkMutable edgeChunk = writer.createEdgeChunk();
                stack.push(edgeChunk);
            }
            default -> {
                // JSON mode, we have some property 'lastKey'
                Object context = stack.peek();
                GmlData parent;
                if (context instanceof GmlData data) {
                    // nest a new sub-object
                    parent = data;
                } else {
                    // start and open a new sub-object
                    parent = new GmlData();
                    stack.push(parent);
                }
                GmlData child = new GmlData();
                parent.add(lastKey, child);
                stack.push(child);
            }
        }
        lastKey = null;
    }

    @Override
    public void value(String value) {
        if (lastKey == null) return;

        String unquotedValue = (value.startsWith("\"") && value.endsWith("\"")) ? value.substring(1, value.length() - 1) : value;

        Object current = stack.peek();
        if (current instanceof ICjChunkMutable chunk) {
            // "key value" on doc/graph/node/edge
            switch (chunk) {
                case ICjDocumentChunk doc -> {
                    assert lastKey != null;
                    final IJsonValue jsonVal = toJsonValue(writer.jsonFactory(), value);
                    doc.dataMutable(d -> d.add(lastKey, jsonVal));
                }
                case ICjGraphChunkMutable graph -> {
                    assert lastKey != null;
                    final IJsonValue jsonVal = toJsonValue(writer.jsonFactory(), value);
                    graph.dataMutable(d -> d.add(lastKey, jsonVal));
                }
                case ICjNodeChunkMutable node -> {
                    assert lastKey != null;
                    if (ID.equalsIgnoreCase(lastKey)) {
                        node.id(unquotedValue);
                    } else if (LABEL.equalsIgnoreCase(lastKey)) {
                        node.addLabelWithoutLanguage(unquotedValue);
                    } else {
                        final IJsonValue jsonVal = toJsonValue(writer.jsonFactory(), value);
                        node.dataMutable(d -> d.add(lastKey, jsonVal));
                    }
                }
                case ICjEdgeChunkMutable edge -> {
                    assert lastKey != null;
                    if (Gml.SOURCE.equalsIgnoreCase(lastKey)) {
                        edge.addEndpoint(ep -> ep.node(unquotedValue).direction(CjDirection.IN));
                    } else if (Gml.TARGET.equalsIgnoreCase(lastKey)) {
                        edge.addEndpoint(ep -> ep.node(unquotedValue).direction(CjDirection.OUT));
                    } else if (LABEL.equalsIgnoreCase(lastKey)) {
                        edge.addLabelWithoutLanguage(unquotedValue);
                    } else {
                        final IJsonValue jsonVal = toJsonValue(writer.jsonFactory(), value);
                        edge.dataMutable(d -> d.add(lastKey, jsonVal));
                    }
                }
                default -> throw new IllegalStateException();
            }
        } else {
            assert current instanceof GmlData;
            GmlData gmlData = (GmlData) current;
            gmlData.add(lastKey, unquotedValue);
        }

        lastKey = null;
    }


    private void ensureDocumentStarted() {
        if (!documentStarted) {
            writer.documentStart(documentChunk);
            documentStarted = true;
        }
    }

    /** ensure current graph is started before adding nodes/edges/subgraphs */
    private void ensureGraphStarted() {
        ICjGraphChunkMutable currentGraph = (ICjGraphChunkMutable) stack.peek();
        if (startedGraphs.getOrDefault(currentGraph, Boolean.FALSE) == Boolean.FALSE) {
            writer.graphStart(currentGraph);
            startedGraphs.put(currentGraph, Boolean.TRUE);
        }
    }


    /**
     *
     * @param jsonFactory
     * @param raw         GML strings cannot be null
     * @return
     */
    private IJsonValue toJsonValue(IJsonFactory jsonFactory, @NonNull String raw) {
        // if already quoted text, strip quotes and make string
        if (raw.length() >= 2 && raw.startsWith("\"") && raw.endsWith("\"")) {
            return jsonFactory.createString(raw.substring(1, raw.length() - 1));
        }
        // numeric? create number
        if (raw.matches("-?\\d+(\\.\\d+)?")) {
            return jsonFactory.createNumberFromString(raw);
        }
        // default: string
        return jsonFactory.createString(raw);
    }

}

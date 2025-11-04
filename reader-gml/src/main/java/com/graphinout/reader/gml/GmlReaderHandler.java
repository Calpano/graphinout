package com.graphinout.reader.gml;

import com.graphinout.base.cj.document.CjDirection;
import com.graphinout.base.cj.document.ICjDocumentChunkMutable;
import com.graphinout.base.cj.document.ICjEdgeChunkMutable;
import com.graphinout.base.cj.document.ICjGraphChunkMutable;
import com.graphinout.base.cj.document.ICjNodeChunkMutable;
import com.graphinout.base.cj.stream.ICjStream;
import com.graphinout.foundation.json.path.IJsonContainerNavigationStep;
import com.graphinout.foundation.json.value.IJsonFactory;
import com.graphinout.foundation.json.value.IJsonValue;
import com.graphinout.foundation.json.value.IJsonObjectMutable;
import com.graphinout.foundation.json.value.IJsonArrayMutable;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

public class GmlReaderHandler implements IGmlHandler {

    private enum Context {
        DOCUMENT, GRAPH, NODE, EDGE, UNKNOWN
    }

    private final ICjStream writer;
    private final Deque<Context> contextStack = new ArrayDeque<>();
    private final Deque<Object> chunkStack = new ArrayDeque<>();
    private final Deque<String> blockNameStack = new ArrayDeque<>(); // holds nested unknown block names
    private final Deque<Integer> blockIndexStack = new ArrayDeque<>(); // aligns with blockNameStack; index within siblings
    private final Map<ICjGraphChunkMutable, Boolean> startedGraphs = new IdentityHashMap<>();
    private final java.util.Map<String, Integer> siblingCounters = new java.util.HashMap<>();
    private ICjDocumentChunkMutable documentChunk;
    private boolean documentStarted = false;
    private String lastKey;

    public GmlReaderHandler(ICjStream writer) {
        this.writer = writer;
        this.contextStack.push(Context.DOCUMENT);
        this.documentChunk = writer.createDocumentChunk();
        this.chunkStack.push(documentChunk);
        // defer documentStart until we have accumulated top-level attributes or first graph
        this.documentStarted = false;
    }

    @Override
    public void key(String key) {
        this.lastKey = key;
    }

    @Override
    public void value(String value) {
        if (lastKey == null) return;

        Object currentChunk = chunkStack.peek();
        String raw = value;
        String unquotedValue = (raw.startsWith("\"") && raw.endsWith("\"")) ? raw.substring(1, raw.length() - 1) : raw;
        // normalize numeric values for certain keys (strip trailing .0)
        if (unquotedValue.matches("-?\\d+\\.0") && (
                "id".equalsIgnoreCase(lastKey) ||
                "source".equalsIgnoreCase(lastKey) ||
                "target".equalsIgnoreCase(lastKey) ||
                "directed".equalsIgnoreCase(lastKey) ||
                "hierarchic".equalsIgnoreCase(lastKey)
        )) {
            unquotedValue = unquotedValue.substring(0, unquotedValue.length() - 2);
        }
        IJsonFactory jsonFactory = writer.jsonFactory();

        Context ctx = contextStack.peek();
        // Build path for JSON data based on nested unknown blocks
        List<IJsonContainerNavigationStep> currentPath = buildPathWithLastKey();
        // some keys should normalize trailing .0 when stored as numbers
        String effectiveRaw = raw;
        if (raw.matches("-?\\d+\\.0") && (
                "directed".equalsIgnoreCase(lastKey) ||
                "hierarchic".equalsIgnoreCase(lastKey) ||
                "id".equalsIgnoreCase(lastKey) ||
                "source".equalsIgnoreCase(lastKey) ||
                "target".equalsIgnoreCase(lastKey)
        )) {
            effectiveRaw = raw.substring(0, raw.length() - 2);
        }

        switch (ctx) {
            case NODE -> {
                ICjNodeChunkMutable node = (ICjNodeChunkMutable) currentChunk;
                if ("id".equalsIgnoreCase(lastKey)) {
                    node.id(unquotedValue);
                } else if ("label".equalsIgnoreCase(lastKey)) {
                    String val = unquotedValue;
                    if (val.matches("-?\\d+\\.0")) val = val.substring(0, val.length() - 2);
                    node.addLabelWithoutLanguage(val);
                } else if (ctx == Context.UNKNOWN) {
                    // never reached due to switch
                } else {
                    final IJsonValue jsonVal = toJsonValue(jsonFactory, effectiveRaw);
                    node.dataMutable(d -> d.add(currentPath, jsonVal));
                }
            }
            case EDGE -> {
                ICjEdgeChunkMutable edge = (ICjEdgeChunkMutable) currentChunk;
                if ("source".equalsIgnoreCase(lastKey)) {
                    final String val = unquotedValue;
                    edge.addEndpoint(ep -> ep.node(val).direction(CjDirection.OUT));
                } else if ("target".equalsIgnoreCase(lastKey)) {
                    final String val = unquotedValue;
                    edge.addEndpoint(ep -> ep.node(val).direction(CjDirection.IN));
                } else if ("label".equalsIgnoreCase(lastKey)) {
                    edge.addLabelWithoutLanguage(unquotedValue);
                } else {
                    final IJsonValue jsonVal = toJsonValue(jsonFactory, effectiveRaw);
                    edge.dataMutable(d -> d.add(currentPath, jsonVal));
                }
            }
            case GRAPH -> {
                ICjGraphChunkMutable graph = (ICjGraphChunkMutable) currentChunk;
                final IJsonValue jsonVal = toJsonValue(jsonFactory, effectiveRaw);
                graph.dataMutable(d -> d.add(currentPath, jsonVal));
            }
            case DOCUMENT -> {
                ICjDocumentChunkMutable doc = (ICjDocumentChunkMutable) currentChunk;
                final IJsonValue jsonVal = toJsonValue(jsonFactory, raw);
                doc.dataMutable(d -> d.add(currentPath, jsonVal));
            }
            case UNKNOWN -> {
                // Values inside UNKNOWN blocks are still applied to the nearest non-UNKNOWN chunk at nested path
                Object targetChunk = findNearestNonUnknownChunk();
                final IJsonValue jsonVal = toJsonValue(jsonFactory, raw);
                if (targetChunk instanceof ICjNodeChunkMutable node) {
                    node.dataMutable(d -> d.add(currentPath, jsonVal));
                } else if (targetChunk instanceof ICjEdgeChunkMutable edge) {
                    edge.dataMutable(d -> d.add(currentPath, jsonVal));
                } else if (targetChunk instanceof ICjGraphChunkMutable graph) {
                    graph.dataMutable(d -> d.add(currentPath, jsonVal));
                } else if (targetChunk instanceof ICjDocumentChunkMutable doc) {
                    doc.dataMutable(d -> d.add(currentPath, jsonVal));
                }
            }
        }
        lastKey = null;
    }

    private List<IJsonContainerNavigationStep> buildPathWithLastKey() {
        // Compose path as: name0, idx0, name1, idx1, ..., lastKey (arrays for repeated unknown blocks)
        List<Object> steps = new ArrayList<>();
        Object[] names = blockNameStack.toArray();
        Object[] idxs = blockIndexStack.toArray();
        int n = names.length;
        // Index only the innermost unknown block (current open block) so siblings form arrays at that level
        int indexedLevel = n > 0 ? 0 : -1;
        for (int i = n - 1; i >= 0; i--) {
            String name = (String) names[i];
            steps.add(name);
            if (i == indexedLevel) {
                Integer idx = (Integer) idxs[i];
                if (idx != null && idx >= 0) steps.add(idx);
            }
        }
        if (lastKey != null) steps.add(lastKey);
        return IJsonContainerNavigationStep.pathOf(steps.toArray());
    }

    private String qualifiedUnknownPathWith(String nextName) {
        // Build a string key representing current unknown path plus nextName to count siblings consistently
        // Scope the counter by the nearest non-UNKNOWN chunk to avoid cross-parent collisions
        Object parentChunk = findNearestNonUnknownChunk();
        StringBuilder sb = new StringBuilder();
        sb.append(System.identityHashCode(parentChunk)).append(':');
        Object[] names = blockNameStack.toArray();
        for (int i = names.length - 1; i >= 0; i--) {
            if (sb.length() > 0) sb.append('/');
            sb.append(names[i]);
        }
        if (sb.charAt(sb.length()-1) != ':') sb.append('/');
        sb.append(nextName);
        return sb.toString();
    }

    private Object findNearestNonUnknownChunk() {
        for (Object o : chunkStack) {
            int idx = 0; // just to satisfy compiler
        }
        // Iterate aligned with contextStack from top to bottom
        java.util.Iterator<Context> ctxIt = contextStack.iterator();
        java.util.Iterator<Object> chIt = chunkStack.iterator();
        while (ctxIt.hasNext() && chIt.hasNext()) {
            Context c = ctxIt.next();
            Object ch = chIt.next();
            if (c != Context.UNKNOWN) return ch;
        }
        return documentChunk;
    }

    private IJsonValue toJsonValue(IJsonFactory factory, String raw) {
        if (raw == null) return factory.createString("");
        // if already quoted text, strip quotes and make string
        if (raw.length() >= 2 && raw.startsWith("\"") && raw.endsWith("\"")) {
            return factory.createString(raw.substring(1, raw.length() - 1));
        }
        // numeric? create number
        if (raw.matches("-?\\d+(\\.\\d+)?")) {
            return factory.createNumberFromString(raw);
        }
        // default: string
        return factory.createString(raw);
    }

    private void ensureDocumentStarted() {
        if (!documentStarted) {
            writer.documentStart(documentChunk);
            documentStarted = true;
        }
    }

    @Override
    public void open() {
        if (lastKey == null) {
            contextStack.push(Context.UNKNOWN);
            chunkStack.push(new Object()); // Placeholder unknown
            // no block name to push
            return;
        }

        switch (lastKey) {
            case "graph":
                ensureDocumentStarted();
                contextStack.push(Context.GRAPH);
                ICjGraphChunkMutable graphChunk = writer.createGraphChunk();
                chunkStack.push(graphChunk);
                // defer starting graph until we see first child or on close, so attributes can be applied first
                startedGraphs.put(graphChunk, Boolean.FALSE);
                break;
            case "node":
                // ensure current graph is started before adding nodes
                if (contextStack.peek() == Context.GRAPH) {
                    ICjGraphChunkMutable currentGraph = (ICjGraphChunkMutable) chunkStack.peek();
                    if (startedGraphs.getOrDefault(currentGraph, Boolean.FALSE) == Boolean.FALSE) {
                        writer.graphStart(currentGraph);
                        startedGraphs.put(currentGraph, Boolean.TRUE);
                    }
                }
                contextStack.push(Context.NODE);
                ICjNodeChunkMutable nodeChunk = writer.createNodeChunk();
                chunkStack.push(nodeChunk);
                break;
            case "edge":
                // ensure current graph is started before adding edges
                if (contextStack.peek() == Context.GRAPH) {
                    ICjGraphChunkMutable currentGraph = (ICjGraphChunkMutable) chunkStack.peek();
                    if (startedGraphs.getOrDefault(currentGraph, Boolean.FALSE) == Boolean.FALSE) {
                        writer.graphStart(currentGraph);
                        startedGraphs.put(currentGraph, Boolean.TRUE);
                    }
                }
                contextStack.push(Context.EDGE);
                ICjEdgeChunkMutable edgeChunk = writer.createEdgeChunk();
                chunkStack.push(edgeChunk);
                break;
            default:
                contextStack.push(Context.UNKNOWN);
                chunkStack.push(new Object()); // Placeholder unknown
                // compute and push sibling index for array-like unknown block sequences
                String basePath = qualifiedUnknownPathWith(lastKey);
                int idx = siblingCounters.getOrDefault(basePath, -1) + 1;
                siblingCounters.put(basePath, idx);
                blockNameStack.push(lastKey); // remember the nested block name for path
                // Always track index for innermost unknown block so repeated keys form arrays from the start
                blockIndexStack.push(idx);
                break;
        }
        lastKey = null;
    }

    @Override
    public void close() {
        Context closedContext = contextStack.pop();
        Object closedChunk = chunkStack.pop();

        switch (closedContext) {
            case GRAPH -> {
                ICjGraphChunkMutable graph = (ICjGraphChunkMutable) closedChunk;
                if (startedGraphs.getOrDefault(graph, Boolean.FALSE) == Boolean.FALSE) {
                    // start and end graph to emit graph-level attributes only
                    writer.graphStart(graph);
                }
                writer.graphEnd();
                startedGraphs.remove(graph);
            }
            case NODE -> writer.node((ICjNodeChunkMutable) closedChunk);
            case EDGE -> writer.edge((ICjEdgeChunkMutable) closedChunk);
            case UNKNOWN -> {
                // pop the nested block name and its aligned index if present
                if (!blockNameStack.isEmpty()) blockNameStack.pop();
                if (!blockIndexStack.isEmpty()) blockIndexStack.pop();
            }
            case DOCUMENT -> {
                // shouldn't normally happen via tokenizer; handled in endDocument()
            }
        }
    }

    public void endDocument() throws IOException {
        // end any still-open contexts except DOCUMENT
        while (!contextStack.isEmpty() && contextStack.peek() != Context.DOCUMENT) {
            close();
        }
        ensureDocumentStarted();
        writer.documentEnd();
    }
}

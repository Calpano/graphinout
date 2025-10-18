package com.graphinout.base.cj.stream.api;

import com.graphinout.base.cj.CjDirection;
import com.graphinout.base.cj.CjException;
import com.graphinout.base.cj.CjType;
import com.graphinout.base.cj.ICjEdgeType;
import com.graphinout.base.cj.element.ICjDataMutable;
import com.graphinout.base.cj.element.ICjDocumentChunkMutable;
import com.graphinout.base.cj.element.ICjDocumentMetaMutable;
import com.graphinout.base.cj.element.ICjEdgeChunkMutable;
import com.graphinout.base.cj.element.ICjEndpointMutable;
import com.graphinout.base.cj.element.ICjGraphChunkMutable;
import com.graphinout.base.cj.element.ICjHasDataMutable;
import com.graphinout.base.cj.element.ICjHasIdMutable;
import com.graphinout.base.cj.element.ICjHasLabelMutable;
import com.graphinout.base.cj.element.ICjHasPortsMutable;
import com.graphinout.base.cj.element.ICjLabelEntryMutable;
import com.graphinout.base.cj.element.ICjLabelMutable;
import com.graphinout.base.cj.element.ICjNodeChunkMutable;
import com.graphinout.base.cj.element.ICjPortMutable;
import com.graphinout.base.cj.stream.ICjWriter;
import com.graphinout.foundation.json.JsonException;
import com.graphinout.foundation.json.stream.impl.Json2JavaJsonWriter;
import com.graphinout.foundation.json.value.IJsonValue;
import com.graphinout.foundation.util.PowerStackOnClasses;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Objects;

public class CjWriter2CjStream implements ICjWriter {

    final ICjStream cjStream;

    // Buffer for JSON data values between jsonDataStart/jsonDataEnd
    private final Json2JavaJsonWriter jsonBuffer = new Json2JavaJsonWriter();
    // Unified type-aware stack for ALL elements (document parts, graphs, nodes, edges, endpoints, ports, label, data)
    private final PowerStackOnClasses<Object> stack = PowerStackOnClasses.create();
    // Track whether a given chunk (graph/node/edge) has had its start event sent
    private final Map<Object, Boolean> started = new IdentityHashMap<>();
    private boolean inJsonData = false;
    // Document state
    private ICjDocumentChunkMutable documentChunk;
    private boolean documentStartSent = false;
    private ICjDocumentMetaMutable documentMeta; // when connectedJsonStart/end

    public CjWriter2CjStream(ICjStream cjStream) {this.cjStream = cjStream;}

    @Override
    public void arrayEnd() throws JsonException {
        if (inJsonData) jsonBuffer.arrayEnd();
    }

    @Override
    public void arrayStart() throws JsonException {
        if (inJsonData) jsonBuffer.arrayStart();
    }

    @Override
    public void baseUri(String baseUri) {
        if (documentChunk != null) {
            documentChunk.baseUri(baseUri);
        }
    }

    @Override
    public void connectedJsonEnd() {
        documentMeta = null;
    }

    @Override
    public void connectedJsonStart() {
        if (documentChunk != null) {
            documentChunk.connectedJson(dm -> documentMeta = dm);
        }
    }

    @Override
    public void connectedJson__canonical(boolean b) {
        if (documentMeta != null) documentMeta.canonical(b);
    }

    @Override
    public void connectedJson__versionDate(String s) {
        if (documentMeta != null) documentMeta.versionDate(s);
    }

    @Override
    public void connectedJson__versionNumber(String s) {
        if (documentMeta != null) documentMeta.versionNumber(s);
    }

    @Override
    public void direction(CjDirection direction) {
        ICjEndpointMutable ep = stack.peek(ICjEndpointMutable.class);
        if (ep != null) {
            ep.direction(direction);
        }
    }

    @Override
    public void documentEnd() throws JsonException {
        if (!documentStartSent) {
            // If nothing triggered document start yet, send now
            ensureDocumentStartSent();
        }
        cjStream.documentEnd();
        documentChunk = null;
        documentMeta = null;
        documentStartSent = false; // reset for potential reuse; though normally single doc
        started.clear();
        stack.reset();
    }

    @Override
    public void documentStart() throws JsonException {
        documentChunk = cjStream.createDocumentChunk();
        documentStartSent = false;
        documentMeta = null;
    }

    @Override
    public void edgeEnd() {
        // ensure edge start was sent (for empty edge with only endpoints maybe)
        ensureCurrentEdgeStartSent();
        cjStream.edgeEnd();
        stack.pop(ICjEdgeChunkMutable.class);
    }

    @Override
    public void edgeStart() {
        ensureCurrentGraphStartSent();
        ICjEdgeChunkMutable edge = cjStream.createEdgeChunk();
        stack.push(edge);
    }

    @Override
    public void edgeType(ICjEdgeType edgeType) {
        ICjEdgeChunkMutable edge = currentEdge();
        if (edge != null) {
            edge.edgeType(edgeType);
        }
    }

    @Override
    public void endpointEnd() {
        // attach endpoint to current edge when finished
        ICjEndpointMutable endpoint = stack.pop(ICjEndpointMutable.class);
        ICjEdgeChunkMutable edge = currentEdge();
        if (edge != null) {
            edge.attachEndpoint(endpoint);
        }
    }

    @Override
    public void endpointStart() {
        ICjEdgeChunkMutable edge = currentEdge();
        if (edge != null) {
            // Do NOT send edge start yet; we want endpoints included in the start chunk
            edge.createEndpoint(stack::push);
        }
    }

    @Override
    public void graphEnd() throws CjException {
        // ensure graph start was sent (for empty graph)
        ensureCurrentGraphStartSent();
        cjStream.graphEnd();
        stack.pop(ICjGraphChunkMutable.class);
    }

    @Override
    public void graphStart() throws CjException {
        // Before starting a subgraph, ensure parent node/edge starts are sent so protocol is consistent
        ensureCurrentNodeStartSent();
        ensureCurrentEdgeStartSent();
        // defer actual graph start emission until we see first child or end to allow id/label/data before start
        ICjGraphChunkMutable graph = cjStream.createGraphChunk();
        stack.push(graph);
    }

    @Override
    public void id(String id) {
        ICjHasIdMutable hasId = currentHasId();
        if (hasId != null) hasId.id(id);
    }

    @Override
    public void jsonDataEnd() {
        if (!inJsonData) return;
        IJsonValue json = jsonBuffer.jsonValue();
        jsonBuffer.reset();
        inJsonData = false;
        ICjDataMutable data = stack.pop(ICjDataMutable.class);
        if (data != null && json != null) {
            data.setJsonValue(json);
        }
    }

    @Override
    public void jsonDataStart() {
        ICjHasDataMutable hasData = currentHasData();
        if (hasData != null) {
            hasData.dataMutable(stack::push);
            inJsonData = true;
            jsonBuffer.reset();
        }
    }

    @Override
    public void labelEnd() {
        stack.pop(ICjLabelMutable.class);
    }

    @Override
    public void labelEntryEnd() {
        stack.pop(ICjLabelEntryMutable.class);
    }

    @Override
    public void labelEntryStart() {
        ICjLabelMutable label = stack.peek(ICjLabelMutable.class);
        if (label != null) {
            label.addEntry(stack::push);
        }
    }

    @Override
    public void labelStart() {
        ICjHasLabelMutable hasLabel = currentHasLabel();
        if (hasLabel != null) {
            hasLabel.setLabel(stack::push);
        }
    }

    @Override
    public void language(String language) {
        ICjLabelEntryMutable entry = stack.peek(ICjLabelEntryMutable.class);
        if (entry != null) entry.language(language);
    }

    @Override
    public void listEnd(CjType cjType) {
        // No-op: list boundaries are handled by CjStream2CjWriter based on start events
    }

    @Override
    public void listStart(CjType cjType) {
        // No-op: list boundaries are handled by CjStream2CjWriter based on start events
    }

    @Override
    public void nodeEnd() {
        ensureCurrentNodeStartSent();
        cjStream.nodeEnd();
        stack.pop(ICjNodeChunkMutable.class);
    }

    @Override
    public void nodeId(String nodeId) {
        // This sets the endpoint.node when inside endpoint; otherwise it is a node id on node chunk
        ICjEndpointMutable ep = stack.peek(ICjEndpointMutable.class);
        if (ep != null) {
            ep.node(nodeId);
        } else {
            ICjHasIdMutable hasId = currentHasId();
            if (hasId != null) hasId.id(nodeId);
        }
    }

    @Override
    public void nodeStart() {
        ensureCurrentGraphStartSent();
        ICjNodeChunkMutable node = cjStream.createNodeChunk();
        stack.push(node);
    }

    @Override
    public void objectEnd() throws JsonException {
        if (inJsonData) jsonBuffer.objectEnd();
    }

    @Override
    public void objectStart() throws JsonException {
        if (inJsonData) jsonBuffer.objectStart();
    }

    @Override
    public void onBigDecimal(BigDecimal bigDecimal) throws JsonException {
        if (inJsonData) jsonBuffer.onBigDecimal(bigDecimal);
    }

    @Override
    public void onBigInteger(BigInteger bigInteger) throws JsonException {
        if (inJsonData) jsonBuffer.onBigInteger(bigInteger);
    }

    @Override
    public void onBoolean(boolean b) throws JsonException {
        if (inJsonData) jsonBuffer.onBoolean(b);
    }

    @Override
    public void onDouble(double d) throws JsonException {
        if (inJsonData) jsonBuffer.onDouble(d);
    }

    @Override
    public void onFloat(float f) throws JsonException {
        if (inJsonData) jsonBuffer.onFloat(f);
    }

    @Override
    public void onInteger(int i) throws JsonException {
        if (inJsonData) jsonBuffer.onInteger(i);
    }

    @Override
    public void onKey(String key) throws JsonException {
        if (inJsonData) jsonBuffer.onKey(key);
    }

    @Override
    public void onLong(long l) throws JsonException {
        if (inJsonData) jsonBuffer.onLong(l);
    }

    @Override
    public void onNull() throws JsonException {
        if (inJsonData) jsonBuffer.onNull();
    }

    @Override
    public void onString(String s) throws JsonException {
        if (inJsonData) jsonBuffer.onString(s);
    }

    @Override
    public void portEnd() {
        // Finish current port on node
        stack.pop(ICjPortMutable.class);
    }

    @Override
    public void portId(String portId) {
        ICjEndpointMutable ep = stack.peek(ICjEndpointMutable.class);
        if (ep != null) {
            ep.port(portId);
        }
    }

    @Override
    public void portStart() {
        // Add a port to current port (nested) or node
        ICjHasPortsMutable hasPorts = safePeekPort();
        if (hasPorts == null) {
            try {
                hasPorts = stack.peekSearch(ICjHasPortsMutable.class);
            } catch (IllegalStateException ignore) {
            }
        }
        if (hasPorts != null) {
            hasPorts.addPort(stack::push);
        }
    }

    @Override
    public void value(String value) {
        ICjLabelEntryMutable entry = stack.peek(ICjLabelEntryMutable.class);
        if (entry != null) entry.value(value);
    }

    private ICjEdgeChunkMutable currentEdge() {
        try {
            return stack.peekSearch(ICjEdgeChunkMutable.class);
        } catch (IllegalStateException e) {
            return null;
        }
    }

    private ICjGraphChunkMutable currentGraph() {
        try {
            return stack.peekSearch(ICjGraphChunkMutable.class);
        } catch (IllegalStateException e) {
            return null;
        }
    }

    private ICjHasDataMutable currentHasData() {
        try {
            return stack.peekSearch(ICjEdgeChunkMutable.class);
        } catch (IllegalStateException ignore) {
        }
        try {
            return stack.peekSearch(ICjNodeChunkMutable.class);
        } catch (IllegalStateException ignore) {
        }
        try {
            return stack.peekSearch(ICjGraphChunkMutable.class);
        } catch (IllegalStateException ignore) {
        }
        if (documentChunk != null) return documentChunk;
        return null;
    }

    private ICjHasIdMutable currentHasId() {
        ICjPortMutable port = safePeekPort();
        if (port != null) return port;
        try {
            return stack.peekSearch(ICjEdgeChunkMutable.class);
        } catch (IllegalStateException ignore) {
        }
        try {
            return stack.peekSearch(ICjNodeChunkMutable.class);
        } catch (IllegalStateException ignore) {
        }
        try {
            return stack.peekSearch(ICjGraphChunkMutable.class);
        } catch (IllegalStateException ignore) {
        }
        return null;
    }

    private ICjHasLabelMutable currentHasLabel() {
        // Prefer most nested element: port > edge > node > graph
        ICjPortMutable port = safePeekPort();
        if (port instanceof ICjHasLabelMutable lh) return lh;
        try {
            return stack.peekSearch(ICjEdgeChunkMutable.class);
        } catch (IllegalStateException ignore) {
        }
        try {
            return stack.peekSearch(ICjNodeChunkMutable.class);
        } catch (IllegalStateException ignore) {
        }
        try {
            return stack.peekSearch(ICjGraphChunkMutable.class);
        } catch (IllegalStateException ignore) {
        }
        return null;
    }

    private ICjNodeChunkMutable currentNode() {
        try {
            return stack.peekSearch(ICjNodeChunkMutable.class);
        } catch (IllegalStateException e) {
            return null;
        }
    }

    private void ensureCurrentEdgeStartSent() {
        ICjEdgeChunkMutable e = currentEdge();
        if (e != null && !isStarted(e)) {
            ensureCurrentGraphStartSent();
            cjStream.edgeStart(e);
            markStarted(e);
        }
    }

    private void ensureCurrentGraphStartSent() {
        ICjGraphChunkMutable g = currentGraph();
        if (g != null && !isStarted(g)) {
            ensureDocumentStartSent();
            cjStream.graphStart(g);
            markStarted(g);
        }
    }

    private void ensureCurrentNodeStartSent() {
        ICjNodeChunkMutable n = currentNode();
        if (n != null && !isStarted(n)) {
            ensureCurrentGraphStartSent();
            cjStream.nodeStart(n);
            markStarted(n);
        }
    }

    private void ensureDocumentStartSent() {
        if (!documentStartSent) {
            Objects.requireNonNull(documentChunk, "documentStart() must be called before graphs");
            cjStream.documentStart(documentChunk);
            documentStartSent = true;
        }
    }

    private boolean isStarted(Object o) {return started.getOrDefault(o, false);}

    private void markStarted(Object o) {started.put(o, true);}

    private ICjPortMutable safePeekPort() {
        try {
            return stack.peekSearch(ICjPortMutable.class);
        } catch (IllegalStateException e) {
            return null;
        }
    }

}

package com.graphinout.base.cj.stream.api;

import com.graphinout.base.cj.CjDirection;
import com.graphinout.base.cj.CjException;
import com.graphinout.base.cj.CjType;
import com.graphinout.base.cj.ICjEdgeType;
import com.graphinout.base.cj.element.*;
import com.graphinout.base.cj.stream.ICjWriter;
import com.graphinout.foundation.json.JsonException;
import com.graphinout.foundation.json.stream.impl.Json2JavaJsonWriter;
import com.graphinout.foundation.json.value.IJsonValue;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Objects;

public class CjWriter2CjStream implements ICjWriter {

    final ICjStream cjStream;

    // Buffer for JSON data values between jsonDataStart/jsonDataEnd
    private final Json2JavaJsonWriter jsonBuffer = new Json2JavaJsonWriter();
    private boolean inJsonData = false;

    // Document state
    private ICjDocumentChunkMutable documentChunk;
    private boolean documentStartSent = false;
    private ICjDocumentMetaMutable documentMeta; // when connectedJsonStart/end

    // Graph/Node/Edge state stacks (to support nesting)
    private static class ElemState<T> {
        final T chunk;
        boolean startSent;
        ElemState(T chunk) { this.chunk = chunk; }
    }

    private final Deque<ElemState<ICjGraphChunkMutable>> graphStack = new ArrayDeque<>();
    private final Deque<ElemState<ICjNodeChunkMutable>> nodeStack = new ArrayDeque<>();
    private final Deque<ElemState<ICjEdgeChunkMutable>> edgeStack = new ArrayDeque<>();

    // Label building (applies to graph/node/edge)
    private final Deque<ICjLabelMutable> labelStack = new ArrayDeque<>();
    private final Deque<ICjLabelEntryMutable> labelEntryStack = new ArrayDeque<>();

    // Data building target stack
    private final Deque<ICjDataMutable> dataStack = new ArrayDeque<>();

    // Temporary endpoint being built for current edge
    private final Deque<ICjEndpointMutable> endpointStack = new ArrayDeque<>();
    // Ports within current node (for id assignment)
    private final Deque<ICjPortMutable> portStack = new ArrayDeque<>();

    public CjWriter2CjStream(ICjStream cjStream) {this.cjStream = cjStream;}

    private void ensureDocumentStartSent() {
        if (!documentStartSent) {
            Objects.requireNonNull(documentChunk, "documentStart() must be called before graphs");
            cjStream.documentStart(documentChunk);
            documentStartSent = true;
        }
    }

    private ElemState<ICjGraphChunkMutable> currentGraphState() { return graphStack.peek(); }
    private ElemState<ICjNodeChunkMutable> currentNodeState() { return nodeStack.peek(); }
    private ElemState<ICjEdgeChunkMutable> currentEdgeState() { return edgeStack.peek(); }

    private void ensureCurrentGraphStartSent() {
        ElemState<ICjGraphChunkMutable> gs = currentGraphState();
        if (gs != null && !gs.startSent) {
            ensureDocumentStartSent();
            cjStream.graphStart(gs.chunk);
            gs.startSent = true;
        }
    }

    private void ensureCurrentNodeStartSent() {
        ElemState<ICjNodeChunkMutable> ns = currentNodeState();
        if (ns != null && !ns.startSent) {
            ensureCurrentGraphStartSent();
            cjStream.nodeStart(ns.chunk);
            ns.startSent = true;
        }
    }

    private void ensureCurrentEdgeStartSent() {
        ElemState<ICjEdgeChunkMutable> es = currentEdgeState();
        if (es != null && !es.startSent) {
            ensureCurrentGraphStartSent();
            cjStream.edgeStart(es.chunk);
            es.startSent = true;
        }
    }

    private ICjHasIdMutable currentHasId() {
        if (!portStack.isEmpty()) return portStack.peek();
        if (!edgeStack.isEmpty()) return edgeStack.peek().chunk;
        if (!nodeStack.isEmpty()) return nodeStack.peek().chunk;
        if (!graphStack.isEmpty()) return graphStack.peek().chunk;
        return null;
    }

    private ICjHasLabelMutable currentHasLabel() {
        // Prefer most nested element: port > edge > node > graph
        if (!portStack.isEmpty()) {
            ICjPortMutable port = portStack.peek();
            if (port instanceof ICjHasLabelMutable lh) return lh;
        }
        if (!edgeStack.isEmpty()) return edgeStack.peek().chunk;
        if (!nodeStack.isEmpty()) return nodeStack.peek().chunk;
        if (!graphStack.isEmpty()) return graphStack.peek().chunk;
        return null;
    }

    private ICjHasDataMutable currentHasData() {
        if (!edgeStack.isEmpty()) return edgeStack.peek().chunk;
        if (!nodeStack.isEmpty()) return nodeStack.peek().chunk;
        if (!graphStack.isEmpty()) return graphStack.peek().chunk;
        if (documentChunk != null) return documentChunk;
        return null;
    }

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
    public void direction(CjDirection direction) {
        if (!endpointStack.isEmpty()) {
            endpointStack.peek().direction(direction);
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
        edgeStack.pop();
    }

    @Override
    public void edgeStart() {
        ensureCurrentGraphStartSent();
        ICjEdgeChunkMutable edge = cjStream.createEdgeChunk();
        edgeStack.push(new ElemState<>(edge));
    }

    @Override
    public void edgeType(ICjEdgeType edgeType) {
        if (!edgeStack.isEmpty()) {
            edgeStack.peek().chunk.edgeType(edgeType);
        }
    }

    @Override
    public void endpointEnd() {
        // attach endpoint to current edge when finished
        ICjEndpointMutable endpoint = endpointStack.pop();
        ElemState<ICjEdgeChunkMutable> es = currentEdgeState();
        if (es != null) {
            es.chunk.attachEndpoint(endpoint);
        }
    }

    @Override
    public void endpointStart() {
        ElemState<ICjEdgeChunkMutable> es = currentEdgeState();
        if (es != null) {
            // Do NOT send edge start yet; we want endpoints included in the start chunk
            es.chunk.createEndpoint(endpointStack::push);
        }
    }

    @Override
    public void graphEnd() throws CjException {
        // ensure graph start was sent (for empty graph)
        ensureCurrentGraphStartSent();
        cjStream.graphEnd();
        graphStack.pop();
    }

    @Override
    public void graphStart() throws CjException {
        // Before starting a subgraph, ensure parent node/edge starts are sent so protocol is consistent
        ensureCurrentNodeStartSent();
        ensureCurrentEdgeStartSent();
        // defer actual graph start emission until we see first child or end to allow id/label/data before start
        ICjGraphChunkMutable graph = cjStream.createGraphChunk();
        graphStack.push(new ElemState<>(graph));
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
    public void connectedJsonEnd() {
        documentMeta = null;
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
        ICjDataMutable data = dataStack.pop();
        if (data != null && json != null) {
            data.setJsonValue(json);
        }
    }

    @Override
    public void jsonDataStart() {
        ICjHasDataMutable hasData = currentHasData();
        if (hasData != null) {
            hasData.dataMutable(dataStack::push);
            inJsonData = true;
            jsonBuffer.reset();
        }
    }

    @Override
    public void labelEnd() {
        if (!labelStack.isEmpty()) labelStack.pop();
    }

    @Override
    public void labelEntryEnd() {
        if (!labelEntryStack.isEmpty()) labelEntryStack.pop();
    }

    @Override
    public void labelEntryStart() {
        ICjLabelMutable label = labelStack.peek();
        if (label != null) {
            label.addEntry(labelEntryStack::push);
        }
    }

    @Override
    public void labelStart() {
        ICjHasLabelMutable hasLabel = currentHasLabel();
        if (hasLabel != null) {
            hasLabel.setLabel(labelStack::push);
        }
    }

    @Override
    public void language(String language) {
        ICjLabelEntryMutable entry = labelEntryStack.peek();
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
        nodeStack.pop();
    }

    @Override
    public void nodeId(String nodeId) {
        // This sets the endpoint.node when inside endpoint; otherwise it is a node id on node chunk
        if (!endpointStack.isEmpty()) {
            endpointStack.peek().node(nodeId);
        } else {
            ICjHasIdMutable hasId = currentHasId();
            if (hasId != null) hasId.id(nodeId);
        }
    }

    @Override
    public void nodeStart() {
        ensureCurrentGraphStartSent();
        ICjNodeChunkMutable node = cjStream.createNodeChunk();
        nodeStack.push(new ElemState<>(node));
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
        if (!portStack.isEmpty()) portStack.pop();
    }

    @Override
    public void portId(String portId) {
        if (!endpointStack.isEmpty()) {
            endpointStack.peek().port(portId);
        }
    }

    @Override
    public void portStart() {
        // Add a port to current port (nested) or node
        ICjHasPortsMutable hasPorts = null;
        if (!portStack.isEmpty()) {
            hasPorts = portStack.peek();
        } else if (!nodeStack.isEmpty()) {
            hasPorts = nodeStack.peek().chunk;
        }
        if (hasPorts != null) {
            hasPorts.addPort(portStack::push);
        }
    }

    @Override
    public void value(String value) {
        ICjLabelEntryMutable entry = labelEntryStack.peek();
        if (entry != null) entry.value(value);
    }

}

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
import java.util.Objects;

public class CjWriter2CjStream implements ICjWriter {

    // Start-state markers live on the same stack right above their corresponding chunk
    private static class StartState {

        boolean started;

    }

    private static class StartGraph extends StartState {}

    private static class StartNode extends StartState {}

    private static class StartEdge extends StartState {}

    private static class StartDocument extends StartState {}

    final ICjStream cjStream;
    // Buffer for JSON data values between jsonDataStart/jsonDataEnd
    private final Json2JavaJsonWriter jsonBuffer = new Json2JavaJsonWriter();
    // Unified type-aware stack for ALL elements (document parts, graphs, nodes, edges, endpoints, ports, label, data)
    private final PowerStackOnClasses<Object> stack = PowerStackOnClasses.create();
    private boolean inJsonData = false;

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
        stack.peekSearch(ICjDocumentChunkMutable.class).baseUri(baseUri);
    }

    @Override
    public void connectedJsonEnd() {
        stack.pop(ICjDocumentMetaMutable.class);
    }

    @Override
    public void connectedJsonStart() {
        ICjDocumentChunkMutable dc;
        try {
            dc = stack.peekSearch(ICjDocumentChunkMutable.class);
        } catch (IllegalStateException e) {
            dc = null;
        }
        if (dc != null) {
            dc.connectedJson(stack::push);
        }
    }

    @Override
    public void connectedJson__canonical(boolean b) {
        ICjDocumentMetaMutable dm = stack.peek(ICjDocumentMetaMutable.class);
        if (dm != null) dm.canonical(b);
    }

    @Override
    public void connectedJson__versionDate(String s) {
        ICjDocumentMetaMutable dm = stack.peek(ICjDocumentMetaMutable.class);
        if (dm != null) dm.versionDate(s);
    }

    @Override
    public void connectedJson__versionNumber(String s) {
        ICjDocumentMetaMutable dm = stack.peek(ICjDocumentMetaMutable.class);
        if (dm != null) dm.versionNumber(s);
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
        // If nothing triggered document start yet, send now
        StartDocument sd;
        try {
            sd = stack.peekSearch(StartDocument.class);
        } catch (IllegalStateException e) {
            sd = null;
        }
        if (sd != null && !sd.started) {
            ensureDocumentStartSent();
        }
        cjStream.documentEnd();
        // reset for potential reuse; clears StartDocument marker too
        stack.reset();
    }

    @Override
    public void documentStart() throws JsonException {
        ICjDocumentChunkMutable dc = cjStream.createDocumentChunk();
        // Push the document chunk, then StartDocument marker to coordinate deferred emission
        stack.push(dc);
        stack.push(new StartDocument());
    }

    @Override
    public void edgeEnd() {
        // ensure edge start was sent (for empty edge with only endpoints maybe)
        ensureCurrentEdgeStartSent();
        cjStream.edgeEnd();
        // pop start marker then the edge chunk
        stack.pop(StartEdge.class);
        stack.pop(ICjEdgeChunkMutable.class);
    }

    @Override
    public void edgeStart() {
        ensureCurrentGraphStartSent();
        ICjEdgeChunkMutable edge = cjStream.createEdgeChunk();
        stack.push(edge);
        stack.push(new StartEdge());
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
        stack.pop(StartGraph.class);
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
        stack.push(new StartGraph());
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
        stack.pop(StartNode.class);
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
        stack.push(new StartNode());
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
        return stack.peekSearch(ICjHasDataMutable.class);
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
        if (e != null) {
            StartEdge se;
            try {
                se = stack.peekSearch(StartEdge.class);
            } catch (IllegalStateException ignore) {
                return;
            }
            if (!se.started) {
                ensureCurrentGraphStartSent();
                cjStream.edgeStart(e);
                se.started = true;
            }
        }
    }

    private void ensureCurrentGraphStartSent() {
        ICjGraphChunkMutable g = currentGraph();
        if (g != null) {
            StartGraph sg;
            try {
                sg = stack.peekSearch(StartGraph.class);
            } catch (IllegalStateException ignore) {
                return;
            }
            if (!sg.started) {
                ensureDocumentStartSent();
                cjStream.graphStart(g);
                sg.started = true;
            }
        }
    }

    private void ensureCurrentNodeStartSent() {
        ICjNodeChunkMutable n = currentNode();
        if (n != null) {
            StartNode sn;
            try {
                sn = stack.peekSearch(StartNode.class);
            } catch (IllegalStateException ignore) {
                return;
            }
            if (!sn.started) {
                ensureCurrentGraphStartSent();
                cjStream.nodeStart(n);
                sn.started = true;
            }
        }
    }

    private void ensureDocumentStartSent() {
        StartDocument sd;
        try {
            sd = stack.peekSearch(StartDocument.class);
        } catch (IllegalStateException e) {
            return;
        }
        if (!sd.started) {
            ICjDocumentChunkMutable dc;
            try {
                dc = stack.peekSearch(ICjDocumentChunkMutable.class);
            } catch (IllegalStateException e) {
                dc = null;
            }
            Objects.requireNonNull(dc, "documentStart() must be called before graphs");
            cjStream.documentStart(dc);
            sd.started = true;
        }
    }


    private ICjPortMutable safePeekPort() {
        try {
            return stack.peekSearch(ICjPortMutable.class);
        } catch (IllegalStateException e) {
            return null;
        }
    }

}

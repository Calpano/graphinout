package com.graphinout.base.cj.stream.impl;

import com.graphinout.base.cj.CjConstants;
import com.graphinout.base.cj.CjDirection;
import com.graphinout.base.cj.CjException;
import com.graphinout.base.cj.CjType;
import com.graphinout.base.cj.ICjEdgeType;
import com.graphinout.base.cj.stream.ICjWriter;
import com.graphinout.foundation.json.JsonException;
import com.graphinout.foundation.json.JsonType;
import com.graphinout.foundation.json.stream.JsonWriter;
import com.graphinout.foundation.json.stream.impl.DelegatingJsonWriter;

import java.util.Arrays;
import java.util.Stack;

public class Cj2JsonWriter extends DelegatingJsonWriter implements ICjWriter {

    enum Mode {Json, Cj}

    /** the version of CJ that this writer writes */
    private static final String CJ_VERSION_NUMBER = "5.0.0";
    private static final String CJ_VERSION_DATA = "2025-07-14";
    private final Stack<Object> stack = new Stack<>();
    private Mode mode = Mode.Cj;


    public Cj2JsonWriter(JsonWriter jsonWriter) {
        super(jsonWriter);
    }

    @Override
    public void arrayEnd() throws JsonException {
        super.arrayEnd();
        pop(JsonType.Array, CjType.ArrayOfLabelEntries, CjType.ArrayOfGraphs, CjType.ArrayOfGraphs, CjType.ArrayOfPorts, CjType.ArrayOfEdges, CjType.ArrayOfNodes, CjType.ArrayOfEndpoints);
    }

    @Override
    public void arrayStart() throws JsonException {
        super.arrayStart();
        push(JsonType.Array);
    }

    @Override
    public void baseUri(String baseUri) {
        assert baseUri != null;
        super.onKey(CjConstants.ROOT__BASE_URI);
        onString(baseUri);
    }

    @Override
    public void connectedJsonEnd() {
        super.objectEnd();
        pop(CjType.ConnectedJson);
    }

    @Override
    public void connectedJsonStart() {
        push(CjType.ConnectedJson);
        super.onKey(CjConstants.ROOT__CONNECTED_JSON);
        super.objectStart();
    }

    @Override
    public void connectedJson__canonical(boolean b) {
        super.onKey(CjConstants.CONNECTED_JSON__CANONICAL);
        super.onBoolean(b);
    }

    @Override
    public void connectedJson__versionDate(String versionDate) {
        super.onKey(CjConstants.CONNECTED_JSON__VERSION_DATE);
        onString(versionDate);
    }

    @Override
    public void connectedJson__versionNumber(String versionNumber) {
        super.onKey(CjConstants.CONNECTED_JSON__VERSION_NUMBER);
        onString(versionNumber);
    }

    @Override
    public void direction(CjDirection direction) {
        if (direction != CjDirection.DEFAULT) {
            super.onKey(CjConstants.ENDPOINT__DIRECTION);
            onString(direction.value());
        }
    }

    public void documentEnd() throws CjException {
        // end the CJ root object
        super.objectEnd();
        super.documentEnd();
    }

    @Override
    public void documentStart() throws CjException {
        super.documentStart();
        // start the CJ root object
        super.objectStart();

//        //   "$schema": "https://calpano.github.io/connected-json/_attachments/cj-schema.json",
//        //  "$id": "https://j-s-o-n.org/schema/connected-json/5.0.0",
//        super.onKey(JsonConstants.DOLLAR_SCHEMA);
//        onString(CjConstants.CJ_SCHEMA_LOCATION);
//        super.onKey(JsonConstants.DOLLAR_ID);
//        onString(CjConstants.CJ_SCHEMA_ID);
//
//        // "connectedJson" : { ...
//        super.onKey(CjConstants.ROOT__CONNECTED_JSON);
//        super.objectStart();
//        super.onKey(CjConstants.CONNECTED_JSON__VERSION_NUMBER);
//        onString(CJ_VERSION_NUMBER);
//        super.onKey(CjConstants.CONNECTED_JSON__VERSION_DATE);
//        onString(CJ_VERSION_DATA);
//        super.objectEnd();
    }

    public void edgeEnd() throws CjException {
        super.objectEnd();
        pop(CjType.Edge);
    }

    public void edgeStart() throws CjException {
        push(CjType.Edge);
        super.objectStart();
    }

    @Override
    public void edgeType(ICjEdgeType edgeType) {
        switch (edgeType.source()) {
            case URI -> super.onKey(CjConstants.EDGE_OR_ENDPOINT__TYPE_URI);
            case Node -> super.onKey(CjConstants.EDGE_OR_ENDPOINT__TYPE_NODE);
            case String -> super.onKey(CjConstants.EDGE_OR_ENDPOINT__TYPE);
            default -> throw new IllegalStateException("Unexpected value: " + edgeType.source());
        }
        onString(edgeType.type());
    }

    @Override
    public void endpointEnd() {
        super.objectEnd();
        pop(CjType.Endpoint);
    }

    @Override
    public void endpointStart() {
        push(CjType.Endpoint);
        super.objectStart();
    }

    @Override
    public void graphEnd() throws CjException {
        super.objectEnd();
        pop(CjType.Graph);
    }

    @Override
    public void graphStart() throws CjException {
        push(CjType.Graph);
        super.objectStart();
        // TODO compoundNode from gioGraph (not yet there)
        // TODO label from gioGraph (not yet there)
        // TODO meta ??? from gioGraph (not yet there)
    }

    @Override
    public void id(String id) {
        super.onKey(CjConstants.ID);
        onString(id);
    }

    @Override
    public void jsonDataEnd() {
        assert mode == Mode.Json;
        mode = Mode.Cj;
    }

    @Override
    public void jsonDataStart() {
        assert mode == Mode.Cj : "Expected Cj mode";
        mode = Mode.Json;
        super.onKey(CjConstants.DATA);
    }

    @Override
    public void labelEntryEnd() {
        super.objectEnd();
        pop(CjType.LabelEntry);
    }

    @Override
    public void labelEntryStart() {
        push(CjType.LabelEntry);
        super.objectStart();
    }

    @Override
    public void language(String language) {
        super.onKey(CjConstants.LANGUAGE);
        onString(language);
    }

    public void listEnd(CjType cjType) {
        CjType type = pop();
        assert type == cjType : "Expected " + cjType + " but found " + type + ".";
        super.arrayEnd();
    }

    public void listStart(CjType cjArrayType) {
        assert cjArrayType.isArray();
        push(cjArrayType);
        switch (cjArrayType) {
            case ArrayOfEdges -> super.onKey(CjConstants.GRAPH__EDGES);
            case ArrayOfNodes -> super.onKey(CjConstants.GRAPH__NODES);
            case ArrayOfGraphs -> super.onKey(CjConstants.GRAPHS);
            case ArrayOfPorts -> super.onKey(CjConstants.PORTS);
            case ArrayOfEndpoints -> super.onKey(CjConstants.EDGE__ENDPOINTS);
            case ArrayOfLabelEntries -> super.onKey(CjConstants.LABEL);
            default -> throw new IllegalStateException("Unknown cjArrayType " + cjArrayType);
        }
        super.arrayStart();
    }

    @Override
    public void nodeEnd() {
        super.objectEnd();
        pop(CjType.Node);
    }

    @Override
    public void nodeId(String nodeId) {
        super.onKey(CjConstants.ENDPOINT__NODE);
        onString(nodeId);
    }

    @Override
    public void nodeStart() {
        push(CjType.Node);
        super.objectStart();
    }

    @Override
    public void objectEnd() throws JsonException {
        super.objectEnd();
        pop(JsonType.Object);
    }

    @Override
    public void objectStart() throws JsonException {
        super.objectStart();
        push(JsonType.Object);
    }

    @Override
    public void portEnd() throws CjException {
        super.objectEnd();
        pop(CjType.Port);
    }

    @Override
    public void portId(String portId) {
        super.onKey(CjConstants.ENDPOINT__PORT);
        onString(portId);
    }

    @Override
    public void portStart() {
        push(CjType.Port);
        super.objectStart();
    }

    @Override
    public void value(String value) {
        super.onKey(CjConstants.VALUE);
        onString(value);
    }

    private boolean isActiveArray() {
        if (stack.isEmpty()) return false;
        Object o = stack.peek();
        return o instanceof CjType && ((CjType) o).isArray() || o instanceof JsonType && ((JsonType) o) == JsonType.Array;
    }

    private <T> T pop() {
        return (T) stack.pop();
    }

    private void pop(CjType cjType) {
        Object o = stack.pop();
        assert o instanceof CjType : "Expected " + cjType + " but found " + o + ".";
        CjType x = (CjType) o;
        assert x == cjType;
    }

    private void pop(Object... expectedCjTypes) {
        Object o = stack.pop();
        if (expectedCjTypes == null || expectedCjTypes.length == 0) return;
        assert o instanceof JsonType || o instanceof CjType : "Expected one of " + Arrays.toString(expectedCjTypes) + " but found " + o + ".";
        for (Object cjType : expectedCjTypes) {
            if (o == cjType) return;
        }
        throw new AssertionError("Expected one of " + Arrays.toString(expectedCjTypes) + " but found " + o + ".");
    }

    private void push(CjType cjType) {
        stack.push(cjType);
    }

    private void push(JsonType jsonType) {
        stack.push(jsonType);
    }

}

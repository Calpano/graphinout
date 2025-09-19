package com.calpano.graphinout.base.cj.stream.impl;

import com.calpano.graphinout.base.cj.CjConstants;
import com.calpano.graphinout.base.cj.CjDirection;
import com.calpano.graphinout.base.cj.ICjEdgeType;
import com.calpano.graphinout.base.cj.CjException;
import com.calpano.graphinout.base.cj.CjType;
import com.calpano.graphinout.base.cj.stream.ICjWriter;
import com.calpano.graphinout.foundation.json.JsonConstants;
import com.calpano.graphinout.foundation.json.JsonException;
import com.calpano.graphinout.foundation.json.JsonType;
import com.calpano.graphinout.foundation.json.stream.impl.DelegatingJsonWriter;
import com.calpano.graphinout.foundation.json.stream.JsonWriter;

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
        pop(JsonType.Array, CjType.ArrayOfLabelEntries, CjType.ArrayOfGraphs,
                CjType.ArrayOfGraphs, CjType.ArrayOfPorts, CjType.ArrayOfEdges, CjType.ArrayOfNodes, CjType.ArrayOfEndpoints);
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
    public void direction(CjDirection direction) {
        super.onKey(CjConstants.ENDPOINT__DIRECTION);
        onString(direction.value());
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
        //   "$schema": "https://calpano.github.io/connected-json/_attachments/cj-schema.json",
        //  "$id": "https://j-s-o-n.org/schema/connected-json/5.0.0",
        super.onKey(JsonConstants.DOLLAR_SCHEMA);
        onString(CjConstants.CJ_SCHEMA_LOCATION);
        super.onKey(JsonConstants.DOLLAR_ID);
        onString(CjConstants.CJ_SCHEMA_ID);

        // "connectedJson" : { ...
        super.onKey(CjConstants.ROOT__CONNECTED_JSON);
        super.objectStart();
        super.onKey(CjConstants.VERSION_NUMBER);
        onString(CJ_VERSION_NUMBER);
        super.onKey(CjConstants.VERSION_DATE);
        onString(CJ_VERSION_DATA);
        super.objectEnd();
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
    public void meta__canonical(boolean b) {
        super.onKey(CjConstants.META__CANONICAL);
        super.onBoolean(b);
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
    public void labelEnd() {
        // FIXME is this right?
        //super.arrayEnd();
        pop(CjType.ArrayOfLabelEntries);
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
    public void labelStart() {
        // FIXME is this right?
        push(CjType.ArrayOfLabelEntries);
    //    super.arrayStart();
    }

    @Override
    public void language(String language) {
        super.onKey(CjConstants.LANGUAGE);
        onString(language);
    }

    private <T> T pop() {
        return (T) stack.pop();
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
    public void metaEnd() {
        super.objectEnd();
        pop(CjType.Meta);
    }

    @Override
    public void metaStart() {
        push(CjType.Meta);
        super.onKey(CjConstants.GRAPH__META);
        super.objectStart();
    }

    @Override
    public void meta__edgeCountInGraph(long number) {
        super.onKey(CjConstants.META__EDGE_COUNT_IN_GRAPH);
        super.onLong(number);
    }

    @Override
    public void meta__edgeCountTotal(long number) {
        super.onKey(CjConstants.META__EDGE_COUNT_TOTAL);
        super.onLong(number);
    }

    @Override
    public void meta__nodeCountInGraph(long number) {
        super.onKey(CjConstants.META__NODE_COUNT_IN_GRAPH);
        super.onLong(number);
    }

    @Override
    public void meta__nodeCountTotal(long number) {
        super.onKey(CjConstants.META__NODE_COUNT_TOTAL);
        super.onLong(number);
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

    /** can be called multiple times per data string */
    public void rawDataCharacters(String data) {
        // FIXME this concept does not exist in CJ, only in Gio (for now)
    }

    /** start a JSON-value like raw data object */
    public void rawDataEnd() {
        // FIXME this concept does not exist in CJ, only in Gio (for now)
    }

    /** end a JSON-value like raw data object */
    public void rawDataStart() {
        // FIXME this concept does not exist in CJ, only in Gio (for now)
    }

    @Override
    public void value(String value) {
        super.onKey(CjConstants.VALUE);
        onString(value);
    }

    private boolean isActiveArray() {
        if (stack.isEmpty())
            return false;
        Object o = stack.peek();
        return o instanceof CjType && ((CjType) o).isArray() || o instanceof JsonType && ((JsonType) o) == JsonType.Array;
    }

    private void pop(CjType cjType) {
        Object o = stack.pop();
        assert o instanceof CjType : "Expected " + cjType + " but found " + o + ".";
        CjType x = (CjType) o;
        assert x == cjType;
    }

    private void pop(Object... expectedCjTypes) {
        Object o = stack.pop();
        if (expectedCjTypes == null || expectedCjTypes.length == 0)
            return;
        assert o instanceof JsonType || o instanceof CjType : "Expected one of " + Arrays.toString(expectedCjTypes) + " but found " + o + ".";
        for (Object cjType : expectedCjTypes) {
            if (o == cjType)
                return;
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

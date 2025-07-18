package com.calpano.graphinout.base.cj.impl;

import com.calpano.graphinout.base.cj.CjConstants;
import com.calpano.graphinout.base.cj.CjDirection;
import com.calpano.graphinout.base.cj.CjEdgeType;
import com.calpano.graphinout.base.cj.CjException;
import com.calpano.graphinout.base.cj.CjType;
import com.calpano.graphinout.base.cj.CjWriter;
import com.calpano.graphinout.foundation.json.JsonConstants;
import com.calpano.graphinout.foundation.json.JsonWriter;
import com.calpano.graphinout.foundation.json.impl.DelegatingJsonWriter;

import java.util.Stack;

/**
 * NOTE CjWriter used fooStart/fooEnd but GioWriter uses startFoo/endFoo
 */
public class Cj2JsonWriter extends DelegatingJsonWriter implements CjWriter {

    enum Mode {Json, Cj}

    /** the version of CJ that this writer writes */
    private static final String CJ_VERSION_NUMBER = "5.0.0";
    private static final String CJ_VERSION_DATA = "2025-07-14";
    private final Stack<CjType> stack = new Stack<>();
    private Mode mode = Mode.Cj;

    public Cj2JsonWriter(JsonWriter jsonWriter) {
        super(jsonWriter);
    }

    @Override
    public void baseUri(String baseUri) {
        super.onKey(CjConstants.BASE_URI);
        super.string(baseUri);
    }

    @Override
    public void direction(CjDirection direction) {
        super.onKey(CjConstants.DIRECTION);
        super.string(direction.name());
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
        super.string(CjConstants.CJ_SCHEMA_LOCATION);
        super.onKey(JsonConstants.DOLLAR_ID);
        super.string(CjConstants.CJ_SCHEMA_ID);

        // "connectedJson" : { ...
        super.onKey(CjConstants.CONNECTED_JSON);
        super.objectStart();
        super.onKey(CjConstants.VERSION_NUMBER);
        super.string(CJ_VERSION_NUMBER);
        super.onKey(CjConstants.VERSION_DATE);
        super.string(CJ_VERSION_DATA);
        super.objectEnd();
    }

    public void edgeEnd() throws CjException {
        super.objectEnd();
        pop(CjType.Edge);
    }

    public void edgeStart() throws CjException {
        ensureInArrayOf(CjType.ArrayOfEdges);
        stack.push(CjType.Edge);
        super.objectStart();
    }

    @Override
    public void edgeType(CjEdgeType edgeType) {
        switch (edgeType.source()) {
            case URI -> super.onKey(CjConstants.TYPE_URI);
            case Node -> super.onKey(CjConstants.TYPE_NODE);
            case String -> super.onKey(CjConstants.TYPE);
            default -> throw new IllegalStateException("Unexpected value: " + edgeType.source());
        }
        super.string(edgeType.type());
    }

    @Override
    public void endpointEnd() {
        super.objectEnd();
        pop(CjType.Endpoint);
    }

    @Override
    public void endpointStart() {
        ensureInArrayOf(CjType.ArrayOfEndpoints);
        stack.push(CjType.Endpoint);
        super.objectStart();
    }

    @Override
    public void graphEnd() throws CjException {
        super.objectEnd();
        pop(CjType.Graph);
    }

    @Override
    public void graphStart() throws CjException {
        ensureInArrayOf(CjType.ArrayOfGraphs);
        stack.push(CjType.Graph);
        super.objectStart();
        // TODO compoundNode from gioGraph (not yet there)
        // TODO label from gioGraph (not yet there)
        // TODO meta ??? from gioGraph (not yet there)
    }

    @Override
    public void id(String id) {
        super.onKey(CjConstants.ID);
        super.string(id);
    }

    @Override
    public void jsonEnd() {
        assert mode == Mode.Json;
        mode = Mode.Cj;
    }

    @Override
    public void jsonStart() {
        assert mode == Mode.Cj;
        mode = Mode.Json;
    }

    @Override
    public void labelEnd() {
        // FIXME is this right?
        super.objectEnd();
        pop(CjType.ArrayOfLabelEntries);
    }

    @Override
    public void labelEntryEnd() {
        super.objectEnd();
        pop(CjType.LabelEntry);
    }

    @Override
    public void labelEntryStart() {
        stack.push(CjType.LabelEntry);
        super.objectStart();
    }

    @Override
    public void labelStart() {
        // FIXME is this right?
        ensureInArrayOf(CjType.ArrayOfLabelEntries);
        stack.push(CjType.ArrayOfLabelEntries);
        super.objectStart();
    }

    @Override
    public void language(String language) {
        super.onKey(CjConstants.LANGUAGE);
        super.string(language);
    }

    public void listEnd(CjType cjType) {
        assert !stack.isEmpty();
        CjType type = stack.pop();
        assert type == cjType;
    }

    public void listStart(CjType cjType) {
        stack.push(cjType);
    }

    @Override
    public void nodeEnd() {
        super.objectEnd();
        pop(CjType.Node);
    }

    @Override
    public void nodeId(String nodeId) {
        super.onKey(CjConstants.NODE);
        super.string(nodeId);
    }

    @Override
    public void nodeStart() {
        ensureInArrayOf(CjType.ArrayOfNodes);
        stack.push(CjType.Node);
        super.objectStart();
    }

    @Override
    public void portEnd() throws CjException {
        super.objectEnd();
        pop(CjType.Port);
    }

    @Override
    public void portId(String portId) {
        super.onKey(CjConstants.ID);
        super.string(portId);
    }

    @Override
    public void portStart() {
        ensureInArrayOf(CjType.ArrayOfPorts);
        stack.push(CjType.Port);
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
        super.string(value);
    }

    /**
     * Opens an array with the correct property, if not already open
     */
    private void ensureInArrayOf(CjType desiredArray) {
        assert desiredArray.isArray();
        CjType cjType = stack.peek();
        if (cjType.isArray()) {
            if (cjType == desiredArray) {
                return;
            }
            // close the open, wrong array
            super.arrayEnd();
        }
        switch (desiredArray) {
            case ArrayOfEdges -> super.onKey(CjConstants.EDGES);
            case ArrayOfNodes -> super.onKey(CjConstants.NODES);
            case ArrayOfGraphs -> super.onKey(CjConstants.GRAPHS);
            case ArrayOfPorts -> super.onKey(CjConstants.PORTS);
            case ArrayOfEndpoints -> super.onKey(CjConstants.ENDPOINTS);
            case ArrayOfLabelEntries -> super.onKey(CjConstants.LABEL);
            default -> throw new IllegalStateException("Unknown desired array " + desiredArray);
        }
        super.arrayStart();
    }

    private void pop(CjType cjType) {
        CjType x = stack.pop();
        assert x == cjType;
    }

}

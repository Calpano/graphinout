package com.calpano.graphinout.base.cj.stream.impl;

import com.calpano.graphinout.base.cj.CjDirection;
import com.calpano.graphinout.base.cj.CjException;
import com.calpano.graphinout.base.cj.CjType;
import com.calpano.graphinout.base.cj.ICjEdgeType;
import com.calpano.graphinout.base.cj.element.ICjDataMutable;
import com.calpano.graphinout.base.cj.element.ICjDocument;
import com.calpano.graphinout.base.cj.element.ICjDocumentMetaMutable;
import com.calpano.graphinout.base.cj.element.ICjDocumentMutable;
import com.calpano.graphinout.base.cj.element.ICjEdgeMutable;
import com.calpano.graphinout.base.cj.element.ICjElement;
import com.calpano.graphinout.base.cj.element.ICjEndpointMutable;
import com.calpano.graphinout.base.cj.element.ICjGraphMutable;
import com.calpano.graphinout.base.cj.element.ICjHasDataMutable;
import com.calpano.graphinout.base.cj.element.ICjHasGraphsMutable;
import com.calpano.graphinout.base.cj.element.ICjHasIdMutable;
import com.calpano.graphinout.base.cj.element.ICjHasLabelMutable;
import com.calpano.graphinout.base.cj.element.ICjHasPortsMutable;
import com.calpano.graphinout.base.cj.element.ICjLabelEntryMutable;
import com.calpano.graphinout.base.cj.element.ICjLabelMutable;
import com.calpano.graphinout.base.cj.element.ICjNodeMutable;
import com.calpano.graphinout.base.cj.element.ICjPortMutable;
import com.calpano.graphinout.base.cj.element.impl.CjDocumentElement;
import com.calpano.graphinout.base.cj.stream.ICjWriter;
import com.calpano.graphinout.foundation.json.JsonException;
import com.calpano.graphinout.foundation.json.stream.impl.Json2JavaJsonWriter;
import com.calpano.graphinout.foundation.json.value.IJsonValue;
import com.calpano.graphinout.foundation.util.PowerStackOnClasses;

public class Cj2ElementsWriter extends Json2JavaJsonWriter implements ICjWriter {

    private final PowerStackOnClasses<ICjElement> stack = PowerStackOnClasses.create();
    private ICjDocument resultDoc;

    @Override
    public void baseUri(String baseUri) {
        stack.peek(ICjDocumentMutable.class).baseUri(baseUri);
    }

    @Override
    public void connectedJsonEnd() {
        stack.pop(ICjDocumentMetaMutable.class);
    }

    @Override
    public void connectedJsonStart() {
        stack.peek(ICjDocumentMutable.class).connectedJson(stack::push);
    }

    @Override
    public void connectedJson__canonical(boolean b) {
        stack.peek(ICjDocumentMetaMutable.class).canonical(b);
    }

    @Override
    public void connectedJson__versionDate(String versionDate) {
        stack.peek(ICjDocumentMetaMutable.class).versionDate(versionDate);
    }

    @Override
    public void connectedJson__versionNumber(String versionNumber) {
        stack.peek(ICjDocumentMetaMutable.class).versionNumber(versionNumber);
    }

    @Override
    public void direction(CjDirection direction) {
        stack.peek(ICjEndpointMutable.class).direction(direction);
    }

    @Override
    public void documentEnd() throws JsonException {
        stack.pop(ICjDocumentMutable.class);
    }

    @Override
    public void documentStart() throws JsonException {
        CjDocumentElement document = new CjDocumentElement();
        this.resultDoc = document;
        stack.push(document);
    }

    @Override
    public void edgeEnd() {
        stack.pop(ICjEdgeMutable.class);
    }

    @Override
    public void edgeStart() {
        stack.peek(ICjGraphMutable.class).addEdge(stack::push);
    }

    @Override
    public void edgeType(ICjEdgeType edgeType) {
        stack.peek(ICjEdgeMutable.class).edgeType(edgeType);
    }

    @Override
    public void endpointEnd() {
        stack.pop(ICjEndpointMutable.class);
    }

    @Override
    public void endpointStart() {
        stack.peek(ICjEdgeMutable.class).addEndpoint(stack::push);
    }

    @Override
    public void graphEnd() throws CjException {
        stack.pop(ICjGraphMutable.class);
    }

    @Override
    public void graphStart() throws CjException {
        stack.peek(ICjHasGraphsMutable.class).addGraph(stack::push);
    }

    @Override
    public void id(String id) {
        stack.peek(ICjHasIdMutable.class).id(id);
    }

    @Override
    public void jsonDataEnd() {
        // need to attach resulting json to element on stack
        ICjDataMutable dataElement = stack.pop(ICjDataMutable.class);
        IJsonValue json = super.jsonValue();
        dataElement.jsonNode(json);
        super.reset();
    }

    @Override
    public void jsonDataStart() {
        // prepare buffering json data
        stack.peek(ICjHasDataMutable.class).addDataElement(stack::push);
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
        stack.peek(ICjLabelMutable.class).addEntry(stack::push);
    }

    @Override
    public void labelStart() {
        stack.peek(ICjHasLabelMutable.class).setLabel(stack::push);
    }

    @Override
    public void language(String language) {
        stack.peek(ICjLabelEntryMutable.class).language(language);
    }

    @Override
    public void listEnd(CjType cjType) {

    }

    @Override
    public void listStart(CjType cjType) {
    }

    @Override
    public void nodeEnd() {
        stack.pop(ICjNodeMutable.class);
    }

    @Override
    public void nodeId(String nodeId) {
        stack.peek(ICjEndpointMutable.class).node(nodeId);
    }

    @Override
    public void nodeStart() {
        stack.peek(ICjGraphMutable.class).addNode(stack::push);
    }

    @Override
    public void portEnd() {
        stack.pop(ICjPortMutable.class);
    }

    @Override
    public void portId(String portId) {
        stack.peek(ICjEndpointMutable.class).port(portId);
    }

    @Override
    public void portStart() {
        stack.peek(ICjHasPortsMutable.class).addPort(stack::push);
    }

    public ICjDocument resultDoc() {
        return resultDoc;
    }

    @Override
    public void value(String value) {
        stack.peek(ICjLabelEntryMutable.class).value(value);
    }

}

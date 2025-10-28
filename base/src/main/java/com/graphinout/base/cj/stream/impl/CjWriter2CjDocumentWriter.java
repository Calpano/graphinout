package com.graphinout.base.cj.stream.impl;

import com.graphinout.base.cj.CjDirection;
import com.graphinout.base.cj.CjException;
import com.graphinout.base.cj.CjType;
import com.graphinout.base.cj.ICjEdgeType;
import com.graphinout.base.cj.element.ICjDataMutable;
import com.graphinout.base.cj.element.ICjDocument;
import com.graphinout.base.cj.element.ICjDocumentMetaMutable;
import com.graphinout.base.cj.element.ICjDocumentMutable;
import com.graphinout.base.cj.element.ICjEdgeMutable;
import com.graphinout.base.cj.element.ICjElement;
import com.graphinout.base.cj.element.ICjEndpointMutable;
import com.graphinout.base.cj.element.ICjGraphMutable;
import com.graphinout.base.cj.element.ICjHasDataMutable;
import com.graphinout.base.cj.element.ICjHasGraphsMutable;
import com.graphinout.base.cj.element.ICjHasIdMutable;
import com.graphinout.base.cj.element.ICjHasLabelMutable;
import com.graphinout.base.cj.element.ICjHasPortsMutable;
import com.graphinout.base.cj.element.ICjLabelEntryMutable;
import com.graphinout.base.cj.element.ICjNodeMutable;
import com.graphinout.base.cj.element.ICjPortMutable;
import com.graphinout.base.cj.element.impl.CjDocumentElement;
import com.graphinout.base.cj.stream.ICjWriter;
import com.graphinout.foundation.json.JsonException;
import com.graphinout.foundation.json.stream.impl.Json2JavaJsonWriter;
import com.graphinout.foundation.json.value.IJsonValue;
import com.graphinout.foundation.util.PowerStackOnClasses;

import javax.annotation.Nullable;
import java.util.function.Consumer;

import static com.graphinout.foundation.util.Nullables.ifConsumerPresentAccept;

/**
 * {@link ICjWriter} to {@link ICjDocument}
 */
public class CjWriter2CjDocumentWriter extends Json2JavaJsonWriter implements ICjWriter {

    private final PowerStackOnClasses<ICjElement> stack = PowerStackOnClasses.create();
    private final @Nullable Consumer<ICjDocument> onDone;
    private ICjDocument resultDoc;

    public CjWriter2CjDocumentWriter(@Nullable Consumer<ICjDocument> onDone) {
        this.onDone = onDone;
    }

    public CjWriter2CjDocumentWriter() {
        this(null);
    }

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
        ifConsumerPresentAccept(onDone, resultDoc);
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
        // attach endpoint now, because edge validates it
        ICjEndpointMutable endpoint = stack.pop(ICjEndpointMutable.class);
        stack.peek(ICjEdgeMutable.class).attachEndpoint(endpoint);
    }

    @Override
    public void endpointStart() {
        stack.peek(ICjEdgeMutable.class).createEndpoint(stack::push);

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
        IJsonValue json = super.jsonValue();
        // need to attach resulting json to element on stack
        ICjDataMutable dataElement = stack.pop(ICjDataMutable.class);
        dataElement.setJsonValue(json);
        super.reset();
    }

    @Override
    public void jsonDataStart() {
        // prepare buffering json data
        ICjHasDataMutable hasDataMutable = stack.peek(ICjHasDataMutable.class);
        hasDataMutable.dataMutable(stack::push);
    }

    @Override
    public void labelEntryEnd() {
        stack.pop(ICjLabelEntryMutable.class);
    }

    @Override
    public void labelEntryStart() {
        ICjHasLabelMutable hasLabelMutable = stack.peek(ICjHasLabelMutable.class);
        hasLabelMutable.labelMutable().addEntry(stack::push);
    }

    @Override
    public void nodeStart() {
        stack.peek(ICjGraphMutable.class).addNode(stack::push);
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

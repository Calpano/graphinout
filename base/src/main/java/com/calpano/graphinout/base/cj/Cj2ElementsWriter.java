package com.calpano.graphinout.base.cj;

import com.calpano.graphinout.base.cj.element.ICjDataMutable;
import com.calpano.graphinout.base.cj.element.ICjDocument;
import com.calpano.graphinout.base.cj.element.ICjDocumentMutable;
import com.calpano.graphinout.base.cj.element.ICjEdgeMutable;
import com.calpano.graphinout.base.cj.element.ICjElement;
import com.calpano.graphinout.base.cj.element.ICjEndpointMutable;
import com.calpano.graphinout.base.cj.element.ICjGraphMetaMutable;
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
import com.calpano.graphinout.base.cj.impl.CjJson2JavaJsonWriter;
import com.calpano.graphinout.foundation.json.JsonException;
import com.calpano.graphinout.foundation.json.value.IJsonValue;

import java.util.Stack;

public class Cj2ElementsWriter extends CjJson2JavaJsonWriter implements CjWriter {

    private final Stack<ICjElement> elements = new Stack<>();
    private ICjDocument resultDoc;

    @Override
    public void baseUri(String baseUri) {
        peek(ICjDocumentMutable.class).baseUri(baseUri);
    }

    @Override
    public void direction(CjDirection direction) {
        peek(ICjEndpointMutable.class).direction(direction);
    }

    @Override
    public void documentEnd() throws JsonException {
        pop(ICjDocumentMutable.class);
    }

    @Override
    public void documentStart() throws JsonException {
        CjDocumentElement document = new CjDocumentElement();
        this.resultDoc = document;
        elements.push(document);
    }

    @Override
    public void edgeEnd() {
        pop(ICjEdgeMutable.class);
    }

    @Override
    public void edgeStart() {
        peek(ICjGraphMutable.class).addEdge(elements::push);
    }

    @Override
    public void edgeType(CjEdgeType edgeType) {
        peek(ICjEdgeMutable.class).edgeType(edgeType);
    }

    @Override
    public void endpointEnd() {
        pop(ICjEndpointMutable.class);
    }

    @Override
    public void endpointStart() {
        peek(ICjEdgeMutable.class).addEndpoint(elements::push);
    }

    @Override
    public void graphEnd() throws CjException {
        pop(ICjGraphMutable.class);
    }

    @Override
    public void graphStart() throws CjException {
        peek(ICjHasGraphsMutable.class).addGraph(elements::push);
    }

    @Override
    public void id(String id) {
        peek(ICjHasIdMutable.class).id(id);
    }

    @Override
    public void jsonDataEnd() {
        // need to attach resulting json to element on stack
        ICjDataMutable dataElement = pop(ICjDataMutable.class);
        IJsonValue json = super.jsonValue();
        dataElement.jsonNode(json);
        super.reset();
    }

    @Override
    public void jsonDataStart() {
        // prepare buffering json data
        peek(ICjHasDataMutable.class).dataElement(elements::push);
    }

    @Override
    public void labelEnd() {
        pop(ICjLabelMutable.class);
    }

    @Override
    public void labelEntryEnd() {
        pop(ICjLabelEntryMutable.class);
    }

    @Override
    public void labelEntryStart() {
        peek(ICjLabelMutable.class).entry(elements::push);
    }

    @Override
    public void labelStart() {
        peek(ICjHasLabelMutable.class).label(elements::push);
    }

    @Override
    public void language(String language) {
        peek(ICjLabelEntryMutable.class).language(language);
    }

    @Override
    public void listEnd(CjType cjType) {

    }

    @Override
    public void listStart(CjType cjType) {
    }

    @Override
    public void metaEnd() {
        pop(ICjGraphMetaMutable.class);
    }

    @Override
    public void metaStart() {
        peek(ICjGraphMutable.class).meta(elements::push);
    }

    @Override
    public void meta__canonical(boolean b) {
        peek(ICjGraphMetaMutable.class).canonical(b);
    }

    @Override
    public void meta__edgeCountInGraph(long number) {
        peek(ICjGraphMetaMutable.class).edgeCountInGraph(number);
    }

    @Override
    public void meta__edgeCountTotal(long number) {
        peek(ICjGraphMetaMutable.class).edgeCountTotal(number);
    }

    @Override
    public void meta__nodeCountInGraph(long number) {
        peek(ICjGraphMetaMutable.class).nodeCountInGraph(number);
    }

    @Override
    public void meta__nodeCountTotal(long number) {
        peek(ICjGraphMetaMutable.class).nodeCountTotal(number);
    }

    @Override
    public void nodeEnd() {
        pop(ICjNodeMutable.class);
    }

    @Override
    public void nodeId(String nodeId) {
        peek(ICjEndpointMutable.class).node(nodeId);
    }

    @Override
    public void nodeStart() {
        peek(ICjGraphMutable.class).addNode(elements::push);
    }

    @Override
    public void portEnd() {
        pop(ICjPortMutable.class);
    }

    @Override
    public void portId(String portId) {
        peek(ICjEndpointMutable.class).port(portId);
    }

    @Override
    public void portStart() {
        peek(ICjHasPortsMutable.class).addPort(elements::push);
    }

    public ICjDocument resultDoc() {
        return resultDoc;
    }

    @Override
    public void value(String value) {
        peek(ICjLabelEntryMutable.class).value(value);
    }

    private <T extends ICjElement> T peek(Class<T> clazz) {
        ICjElement e = elements.peek();
        if (clazz.isInstance(e)) {
            return clazz.cast(e);
        }
        throw new IllegalStateException("Expected " + clazz + " but was " + e);
    }

    private <T extends ICjElement> T pop(Class<T> clazz) {
        ICjElement e = elements.pop();
        if (clazz.isInstance(e)) {
            return clazz.cast(e);
        }
        throw new IllegalStateException("Expected " + clazz + " but was " + e);
    }

}

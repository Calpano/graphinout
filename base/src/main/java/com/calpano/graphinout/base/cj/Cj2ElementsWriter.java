package com.calpano.graphinout.base.cj;

import com.calpano.graphinout.base.cj.element.ICjDocument;
import com.calpano.graphinout.base.cj.element.ICjElement;
import com.calpano.graphinout.base.cj.element.ICjWithMutableGraphs;
import com.calpano.graphinout.base.cj.element.ICjWithMutableId;
import com.calpano.graphinout.base.cj.element.ICjWithMutablePorts;
import com.calpano.graphinout.base.cj.element.impl.CjDataElement;
import com.calpano.graphinout.base.cj.element.impl.CjDocumentElement;
import com.calpano.graphinout.base.cj.element.impl.CjEdgeElement;
import com.calpano.graphinout.base.cj.element.impl.CjElement;
import com.calpano.graphinout.base.cj.element.impl.CjEndpointElement;
import com.calpano.graphinout.base.cj.element.impl.CjGraphElement;
import com.calpano.graphinout.base.cj.element.impl.CjGraphMetaElement;
import com.calpano.graphinout.base.cj.element.impl.CjLabelElement;
import com.calpano.graphinout.base.cj.element.impl.CjLabelEntryElement;
import com.calpano.graphinout.base.cj.element.impl.CjNodeElement;
import com.calpano.graphinout.base.cj.element.impl.CjPortElement;
import com.calpano.graphinout.base.cj.element.impl.CjWithDataAndLabelElement;
import com.calpano.graphinout.base.cj.element.impl.CjWithDataElement;
import com.calpano.graphinout.base.cj.impl.CjJson2JavaJsonWriter;
import com.calpano.graphinout.foundation.json.JsonException;
import com.calpano.graphinout.foundation.json.value.IJsonValue;

import java.util.Stack;

public class Cj2ElementsWriter extends CjJson2JavaJsonWriter implements CjWriter {

    private final Stack<ICjElement> elements = new Stack<>();
    private ICjDocument resultDoc;

    @Override
    public void baseUri(String baseUri) {
        peek(CjDocumentElement.class).baseUri(baseUri);
    }

    @Override
    public void direction(CjDirection direction) {
        peek(CjEndpointElement.class).direction(direction);
    }

    @Override
    public void documentEnd() throws JsonException {
        pop(CjDocumentElement.class);
    }

    @Override
    public void documentStart() throws JsonException {
        CjDocumentElement document = new CjDocumentElement();
        this.resultDoc = document;
        elements.push(document);
    }

    @Override
    public void edgeEnd() {
        pop(CjEdgeElement.class);
    }

    @Override
    public void edgeStart() {
        peek(CjGraphElement.class).addEdge(elements::push);
    }

    @Override
    public void edgeType(CjEdgeType edgeType) {
        peek(CjEdgeElement.class).edgeType(edgeType);
    }

    @Override
    public void endpointEnd() {
        pop(CjEndpointElement.class);
    }

    @Override
    public void endpointStart() {
        peek(CjEdgeElement.class).addEndpoint(elements::push);
    }

    @Override
    public void graphEnd() throws CjException {
        pop(CjGraphElement.class);
    }

    @Override
    public void graphStart() throws CjException {
        peek(ICjWithMutableGraphs.class).addGraph(elements::push);
    }

    @Override
    public void id(String id) {
        peek(ICjWithMutableId.class).id(id);
    }

    @Override
    public void jsonDataEnd() {
        // need to attach resulting json to element on stack
        CjDataElement dataElement = pop(CjDataElement.class);
        IJsonValue json = super.jsonValue();
        dataElement.jsonNode(json);
        super.reset();
    }

    @Override
    public void jsonDataStart() {
        // prepare buffering json data
        peek(CjWithDataElement.class).dataElement(elements::push);
    }

    @Override
    public void labelEnd() {
        pop(CjLabelElement.class);
    }

    @Override
    public void labelEntryEnd() {
        pop(CjLabelEntryElement.class);
    }

    @Override
    public void labelEntryStart() {
        peek(CjLabelElement.class).entry(elements::push);
    }

    @Override
    public void labelStart() {
        peek(CjWithDataAndLabelElement.class).label(elements::push);
    }

    @Override
    public void language(String language) {
        peek(CjLabelEntryElement.class).language(language);
    }

    @Override
    public void listEnd(CjType cjType) {

    }

    @Override
    public void listStart(CjType cjType) {
    }

    @Override
    public void metaEnd() {
        pop(CjGraphMetaElement.class);
    }

    @Override
    public void metaStart() {
        peek(CjGraphElement.class).meta(elements::push);
    }

    @Override
    public void meta__canonical(boolean b) {
        peek(CjGraphMetaElement.class).canonical(b);
    }

    @Override
    public void meta__edgeCountInGraph(long number) {
        peek(CjGraphMetaElement.class).edgeCountInGraph(number);
    }

    @Override
    public void meta__edgeCountTotal(long number) {
        peek(CjGraphMetaElement.class).edgeCountTotal(number);
    }

    @Override
    public void meta__nodeCountInGraph(long number) {
        peek(CjGraphMetaElement.class).nodeCountInGraph(number);
    }

    @Override
    public void meta__nodeCountTotal(long number) {
        peek(CjGraphMetaElement.class).nodeCountTotal(number);
    }

    @Override
    public void nodeEnd() {
        pop(CjNodeElement.class);
    }

    @Override
    public void nodeId(String nodeId) {
        peek(CjEndpointElement.class).node(nodeId);
    }

    @Override
    public void nodeStart() {
        peek(CjGraphElement.class).addNode(elements::push);
    }

    @Override
    public void portEnd() {
        pop(CjPortElement.class);
    }

    @Override
    public void portId(String portId) {
        peek(CjEndpointElement.class).port(portId);
    }

    @Override
    public void portStart() {
        peek(ICjWithMutablePorts.class).addPort(elements::push);
    }

    public ICjDocument resultDoc() {
        return resultDoc;
    }

    @Override
    public void value(String value) {
        peek(CjLabelEntryElement.class).value(value);
    }

    private <T extends ICjElement> T peek(Class<T> clazz) {
        ICjElement e = elements.peek();
        if (clazz.isInstance(e)) {
            return clazz.cast(e);
        }
        throw new IllegalStateException("Expected " + clazz + " but was " + e);
    }

    private <T extends CjElement> T pop(Class<T> clazz) {
        ICjElement e = elements.pop();
        if (clazz.isInstance(e)) {
            return clazz.cast(e);
        }
        throw new IllegalStateException("Expected " + clazz + " but was " + e);
    }

}

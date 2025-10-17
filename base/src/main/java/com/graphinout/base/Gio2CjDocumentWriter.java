package com.graphinout.base;

import com.graphinout.base.cj.element.ICjDataMutable;
import com.graphinout.base.cj.element.ICjDocument;
import com.graphinout.base.cj.element.ICjDocumentMutable;
import com.graphinout.base.cj.element.ICjEdgeMutable;
import com.graphinout.base.cj.element.ICjElement;
import com.graphinout.base.cj.element.ICjGraphMutable;
import com.graphinout.base.cj.element.ICjHasDataMutable;
import com.graphinout.base.cj.element.ICjHasGraphsMutable;
import com.graphinout.base.cj.element.ICjHasIdMutable;
import com.graphinout.base.cj.element.ICjHasPortsMutable;
import com.graphinout.base.cj.element.ICjNodeMutable;
import com.graphinout.base.cj.element.ICjPortMutable;
import com.graphinout.base.cj.element.impl.CjDocumentElement;
import com.graphinout.base.gio.GioData;
import com.graphinout.base.gio.GioDocument;
import com.graphinout.base.gio.GioEdge;
import com.graphinout.base.gio.GioElement;
import com.graphinout.base.gio.GioElementWithDescription;
import com.graphinout.base.gio.GioElementWithId;
import com.graphinout.base.gio.GioGraph;
import com.graphinout.base.gio.GioKey;
import com.graphinout.base.gio.GioNode;
import com.graphinout.base.gio.GioPort;
import com.graphinout.base.gio.GioWriter;
import com.graphinout.base.graphml.CjGraphmlMapping.CjDataProperty;
import com.graphinout.foundation.json.stream.impl.Json2JavaJsonWriter;
import com.graphinout.foundation.util.PowerStackOnClasses;

import javax.annotation.Nullable;
import java.io.IOException;
import java.net.URL;

import static com.graphinout.base.graphml.CjGraphmlMapping.CjDataProperty.CustomXmlAttributes;
import static com.graphinout.foundation.json.path.IJsonContainerNavigationStep.pathOf;
import static com.graphinout.foundation.util.Nullables.ifPresentAccept;
import static java.util.Optional.ofNullable;

public class Gio2CjDocumentWriter extends Json2JavaJsonWriter implements GioWriter {

    private final PowerStackOnClasses<ICjElement> stack = PowerStackOnClasses.create();
    private ICjDocumentMutable document;

    private static void copyCustomAttributes(GioElement gio, ICjHasDataMutable cj) {
        ifPresentAccept(gio.getCustomAttributes(), customAttributes -> //
                cj.dataMutable(dataMutable -> //
                        customAttributes.forEach((key, value) -> //
                                dataMutable.add(pathOf(CustomXmlAttributes.cjPropertyKey, key), value))));
    }

    private static void copyDesc(GioElementWithDescription gio, ICjHasDataMutable cj) {
        ifPresentAccept(gio.getDescription(), desc -> //
                cj.dataMutable(dataMutable -> //
                        dataMutable.add(pathOf(CjDataProperty.Description.cjPropertyKey), desc)));
    }

    private static void copyId(GioElementWithId gio, ICjHasIdMutable cj) {
        ofNullable(gio.getId()).ifPresent(cj::id);
    }

    @Override
    public void baseUri(String baseUri) throws IOException {
        peek(ICjDocumentMutable.class).baseUri(baseUri);
    }

    @Override
    public void data(GioData data) throws IOException {
        pop(ICjDataMutable.class);
    }

    @Override
    public void endDocument() throws IOException {
        pop(ICjDocumentMutable.class);
    }

    @Override
    public void endEdge() throws IOException {
        pop(ICjEdgeMutable.class);
    }

    @Override
    public void endGraph(@Nullable URL locator) throws IOException {
        pop(ICjGraphMutable.class);
    }

    @Override
    public void endNode(@Nullable URL locator) throws IOException {
        pop(ICjNodeMutable.class);
    }

    @Override
    public void endPort() throws IOException {
        pop(ICjPortMutable.class);
    }

    @Override
    public void key(GioKey gioKey) throws IOException {

    }

    public @Nullable ICjDocument resultDocument() {
        return document;
    }

    @Override
    public void startDocument(GioDocument document) throws IOException {
        assert this.document == null;
        CjDocumentElement cjDoc = new CjDocumentElement();
        copyDesc(document, cjDoc);
        copyCustomAttributes(document, cjDoc);
        this.document = cjDoc;
        push(cjDoc);
    }

    @Override
    public void startEdge(GioEdge gioEdge) throws IOException {
        peek(ICjGraphMutable.class).addEdge(cjEdge -> {
            copyId(gioEdge, cjEdge);
            copyDesc(gioEdge, cjEdge);
            copyCustomAttributes(gioEdge, cjEdge);
            // FIXME gioGraph.isEdgedefaultDirected();
            // TODO ports
            // TODO endpoints

            gioEdge.getEndpoints().forEach(gioEndpoint -> {
                cjEdge.addEndpoint(cjEndpoint -> {
                    // GIO endpoint ID is a custom CJ data property
                    cjEndpoint.dataMutable(m-> m.addProperty( "id", gioEndpoint.getId()));
                    ofNullable(gioEndpoint.getPort()).ifPresent(cjEndpoint::port);
                    cjEndpoint.node(gioEndpoint.getNode());
                    cjEndpoint.direction(gioEndpoint.getType().toCjDirection());
                });
            });

            push(cjEdge);
        });
    }

    @Override
    public void startGraph(GioGraph gioGraph) throws IOException {
        peek(ICjHasGraphsMutable.class).addGraph(cjGraph -> {
            copyId(gioGraph, cjGraph);
            copyDesc(gioGraph, cjGraph);
            copyCustomAttributes(gioGraph, cjGraph);
            // FIXME gioGraph.isEdgedefaultDirected();
            push(cjGraph);
        });
    }

    @Override
    public void startNode(GioNode gioNode) throws IOException {
        peek(ICjGraphMutable.class).addNode(cjNode -> {
            copyId(gioNode, cjNode);
            copyDesc(gioNode, cjNode);
            copyCustomAttributes(gioNode, cjNode);
            // FIXME gioGraph.isEdgedefaultDirected();
            // TODO ports
            push(cjNode);
        });
    }

    @Override
    public void startPort(GioPort gioPort) throws IOException {
        peek(ICjHasPortsMutable.class).addPort(cjPort -> {
            ofNullable(gioPort.getName()).ifPresent(cjPort::id);
            copyDesc(gioPort, cjPort);
            copyCustomAttributes(gioPort, cjPort);
            // FIXME gioGraph.isEdgedefaultDirected();
            // TODO ports
            push(cjPort);
        });
    }

    private <T extends ICjElement> T peek(Class<T> elementType) {
        return stack.peek(elementType);
    }

    private void pop(Class<? extends ICjElement> elementType) {
        stack.pop(elementType);
    }

    private void push(ICjElement element) {
        stack.push(element);
    }

}

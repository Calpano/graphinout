package com.calpano.graphinout.reader.graphml.cj;

import com.calpano.graphinout.base.cj.CjType;
import com.calpano.graphinout.base.cj.CjWriter;
import com.calpano.graphinout.base.cj.element.impl.CjDocumentElement;
import com.calpano.graphinout.base.cj.element.impl.CjHasDataElement;
import com.calpano.graphinout.base.cj.element.ICjGraphMutable;
import com.calpano.graphinout.base.graphml.GraphmlWriter;
import com.calpano.graphinout.base.graphml.IGraphmlData;
import com.calpano.graphinout.base.graphml.IGraphmlDescription;
import com.calpano.graphinout.base.graphml.IGraphmlDocument;
import com.calpano.graphinout.base.graphml.IGraphmlEdge;
import com.calpano.graphinout.base.graphml.IGraphmlGraph;
import com.calpano.graphinout.base.graphml.IGraphmlHyperEdge;
import com.calpano.graphinout.base.graphml.IGraphmlKey;
import com.calpano.graphinout.base.graphml.IGraphmlNode;
import com.calpano.graphinout.base.graphml.IGraphmlPort;
import com.calpano.graphinout.base.graphml.impl.GraphmlKey;
import com.calpano.graphinout.foundation.json.value.IJsonValue;
import com.calpano.graphinout.foundation.json.value.jackson.JacksonFactory;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Stack;

import static com.calpano.graphinout.base.cj.CjDirection.IN;
import static com.calpano.graphinout.base.cj.CjDirection.OUT;
import static com.calpano.graphinout.base.cj.CjDirection.UNDIR;

public class Graphml2CjWriter implements GraphmlWriter {

    static class GraphmlGraph {

        final IGraphmlGraph.EdgeDefault edgeDefault;

        GraphmlGraph(IGraphmlGraph.EdgeDefault edgeDefault) {this.edgeDefault = edgeDefault;}

    }

    private final Map<String, GraphmlKey> keyDefinitions = new HashMap<>();
    private final Graphml2CjStack parseBuffer = new Graphml2CjStack();
    private final CjWriter cjWriter;
    private final Stack<GraphmlGraph> graphStack = new Stack<>();

    public Graphml2CjWriter(CjWriter cjWriter) {this.cjWriter = cjWriter;}

    /** desc goes to data: <graphml><desc> -> .data.description */
    private static void graphmlDesc(@Nullable IGraphmlDescription desc, CjHasDataElement cjWithData) {
        if (desc == null) return;
        cjWithData.data(mm -> mm.addProperty("description", desc.value()));
    }

    @Override
    public void data(IGraphmlData data) throws IOException {
        // process keyDefs to find mapped attName
        assert !keyDefinitions.isEmpty();
        GraphmlKey key = keyDefinitions.get(data.key());
        assert key != null : "Found no <key> for '" + data.key() + "'";
        String propName = key.attrName();
        assert propName != null;

        // TODO ignore data.id() ?
        IJsonValue value;
        if (data.isRawXml()) {
            // FIXME document this enhanced typed string construct
            value = JacksonFactory.INSTANCE.createObjectAppendable().addProperty("type", "xml").addProperty("value", data.value());
        } else {
            value = JacksonFactory.INSTANCE.createString(data.value());
        }

        // attach to next element on the stack
        parseBuffer.peek_().asWithData().data(mm -> mm.addProperty(propName, value));
    }

    @Override
    public void documentEnd() throws IOException {
        writeAllTo(cjWriter);
    }

    @Override
    public void documentStart(IGraphmlDocument document) throws IOException {
        CjDocumentElement documentEvent = parseBuffer.pushRoot();

        graphmlDesc(document.desc(), documentEvent);

        // GraphML document attributes, like XML namespaces, become /data/cj:attributes
        // <graphml ATTS> ->  /data/cj:attributes/{attName}
        document.xmlPlusGraphmlAttributesNormalized().forEach((key, value) -> //
                documentEvent.data(mm -> mm.addProperty(key, value)));
    }

    @Override
    public void edgeEnd() throws IOException {
        parseBuffer.pop(CjType.Edge);
    }

    @Override
    public void edgeStart(IGraphmlEdge graphmlEdge) throws IOException {
        ICjGraphMutable cjGraph = parseBuffer.peek_().asGraph();
        cjGraph.addEdge(cjEdge -> {
            cjEdge.id(graphmlEdge.id());
            Boolean dir = Objects.requireNonNullElse(graphmlEdge.directed(),
                    // FIXME use graph default
                    false);
            cjEdge.addEndpoint(ep -> ep.node(graphmlEdge.source()).port(graphmlEdge.sourcePort()).direction(dir ? IN : UNDIR));
            cjEdge.addEndpoint(ep -> ep.node(graphmlEdge.target()).port(graphmlEdge.targetPort()).direction(dir ? OUT : UNDIR));
            parseBuffer.push(cjEdge);
        });
    }

    @Override
    public void graphEnd() throws IOException {

    }

    @Override
    public void graphStart(IGraphmlGraph graphmlGraph) throws IOException {
        // remember for coming edges
        graphStack.push(new GraphmlGraph(graphmlGraph.edgeDefault()));

        CjDocumentElement cjDoc = parseBuffer.peek_().asDocument();
        cjDoc.addGraph(graphElement -> {
            graphElement.id(graphElement.id());
            graphmlDesc(graphmlGraph.desc(), graphElement);
            parseBuffer.push(graphElement);
        });

        // FIXME
        if (graphmlGraph.locator() != null) {
            throw new IllegalArgumentException("CJ has no locator support");
        }
    }

    @Override
    public void hyperEdgeEnd() throws IOException {

    }

    @Override
    public void hyperEdgeStart(IGraphmlHyperEdge edge) throws IOException {

    }

    @Override
    public void key(IGraphmlKey key) throws IOException {
        keyDefinitions.put(key.id(), (GraphmlKey) key);
    }

    @Override
    public void nodeEnd() throws IOException {

    }

    @Override
    public void nodeStart(IGraphmlNode node) throws IOException {

    }

    @Override
    public void portEnd() throws IOException {

    }

    @Override
    public void portStart(IGraphmlPort port) throws IOException {

    }

    public void writeAllTo(CjWriter cjWriter) {
        parseBuffer.root_().asDocument().fire(cjWriter);
    }

}

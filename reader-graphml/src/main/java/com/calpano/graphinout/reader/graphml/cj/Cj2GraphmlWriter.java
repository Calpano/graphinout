package com.calpano.graphinout.reader.graphml.cj;

import com.calpano.graphinout.base.cj.CjDirection;
import com.calpano.graphinout.base.cj.CjEdgeType;
import com.calpano.graphinout.base.cj.CjException;
import com.calpano.graphinout.base.cj.CjType;
import com.calpano.graphinout.base.cj.CjWriter;
import com.calpano.graphinout.base.cj.impl.CjJson2StringWriter;
import com.calpano.graphinout.base.cj.jackson.CjJacksonEdge;
import com.calpano.graphinout.base.cj.jackson.CjJacksonEndpoint;
import com.calpano.graphinout.base.gio.GioDataType;
import com.calpano.graphinout.base.graphml.GraphmlDirection;
import com.calpano.graphinout.base.graphml.GraphmlKeyForType;
import com.calpano.graphinout.base.graphml.GraphmlWriter;
import com.calpano.graphinout.base.graphml.IGraphmlData;
import com.calpano.graphinout.base.graphml.IGraphmlDescription;
import com.calpano.graphinout.base.graphml.IGraphmlDocument;
import com.calpano.graphinout.base.graphml.IGraphmlEdge;
import com.calpano.graphinout.base.graphml.IGraphmlEndpoint;
import com.calpano.graphinout.base.graphml.IGraphmlGraph;
import com.calpano.graphinout.base.graphml.IGraphmlHyperEdge;
import com.calpano.graphinout.base.graphml.IGraphmlKey;
import com.calpano.graphinout.base.graphml.IGraphmlNode;
import com.calpano.graphinout.base.graphml.IGraphmlPort;
import com.calpano.graphinout.base.graphml.builder.GraphmlEndpointBuilder;
import com.calpano.graphinout.base.graphml.builder.GraphmlGraphBuilder;
import com.calpano.graphinout.base.graphml.builder.GraphmlHyperEdgeBuilder;
import com.calpano.graphinout.base.graphml.builder.GraphmlKeyBuilder;
import com.calpano.graphinout.base.graphml.builder.GraphmlNodeBuilder;
import com.calpano.graphinout.base.graphml.builder.GraphmlPortBuilder;
import com.calpano.graphinout.base.graphml.impl.GraphmlEdge;
import com.calpano.graphinout.base.graphml.impl.GraphmlEndpoint;
import com.calpano.graphinout.foundation.json.JsonException;
import com.calpano.graphinout.foundation.json.impl.StringBuilderJsonWriter;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

/**
 *
 */
public class Cj2GraphmlWriter extends CjJson2StringWriter implements CjWriter {

    /** used for temporary buffering of JSON data */
    private final StringBuilderJsonWriter jsonWriter = new StringBuilderJsonWriter();
    /** data@id -> key@for -> <key> */
    private final Map<String, Map<GraphmlKeyForType, IGraphmlKey>> dataId_for_key = new HashMap<>();
    /** downstream writer */
    private final Cj2GraphmlStack elementStack = new Cj2GraphmlStack();
    private final GraphmlWriter graphmlWriter;

    public Cj2GraphmlWriter(GraphmlWriter graphmlWriter) {this.graphmlWriter = graphmlWriter;}


    @Override
    public void baseUri(String baseUri) {
        // cj:Document.baseUri -> graphml has no baseuri, so: <data>
        // FIXME put baseUri to <data>, prepare <key>
    }

    @Override
    public void direction(CjDirection direction) {
        GraphmlDirection dir = GraphmlDirection.ofCj(direction);
        this.elementStack.peek_(CjType.Endpoint).endpointBuilder().type(dir);
    }

    @Override
    public void documentEnd() throws JsonException {
        Cj2GraphmlContext ctxRoot = elementStack.pop(CjType.RootObject);
        try {
            writeTo(ctxRoot, graphmlWriter);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void documentStart() {
        elementStack.push(CjType.RootObject, IGraphmlDocument.builder());
    }

    @Override
    public void edgeEnd() {
        elementStack.pop(CjType.Edge);
    }

    public void edgeStart() {
        elementStack.push(CjType.Edge, IGraphmlHyperEdge.builder());
    }

    @Override
    public void edgeType(CjEdgeType edgeType) {
        // CJ edge type encoded as Graphml:DATA
        Cj2GraphmlContext ctx = elementStack.peek_(CjType.Edge, CjType.Endpoint);
        if (ctx.cjType == CjType.Edge) {
            // TODO attach to data
        } else {
            assert ctx.cjType == CjType.Endpoint;
            // TODO attach to data
        }
    }

    @Override
    public void endpointEnd() {
        Cj2GraphmlContext endpoint = elementStack.pop(CjType.Endpoint);
        GraphmlEndpoint ep = endpoint.endpointBuilder().build();
        Cj2GraphmlContext parentContext = elementStack.peek_();
        assert parentContext.hyperEdgeBuilder() != null;
        parentContext.hyperEdgeBuilder().addEndpoint(ep);
    }

    @Override
    public void endpointStart() {
        GraphmlEndpointBuilder builder = IGraphmlEndpoint.builder();
        elementStack.push(CjType.Endpoint, builder);
    }

    @Override
    public void graphEnd() throws CjException {
        elementStack.pop(CjType.Graph);
    }

    @Override
    public void graphStart() throws CjException {
        GraphmlGraphBuilder builder = IGraphmlGraph.builder();
        elementStack.push(CjType.Graph, builder);
    }

    @Override
    public void meta__canonical(boolean b) {
        // FIXME peek & set prop
    }

    @Override
    public void id(String id) {
        Cj2GraphmlContext element = elementStack.peek_(CjType.Graph, CjType.Node, CjType.Edge);
        element.idBuilder().id(id);
    }

    @Override
    public void jsonDataEnd() {
        Cj2GraphmlContext ctxData = elementStack.pop(CjType.Data);
        String json = super.jsonString();
        super.reset();
        ctxData.dataBuilder().value(json);
    }

    @Override
    public void jsonDataStart() {
        // jsonSink is ready to collect JSON data
        // No specific initialization needed as jsonSink is reset in jsonEnd()
        elementStack.push(CjType.Data, IGraphmlData.builder());
    }

    @Override
    public void labelEnd() {
        elementStack.pop(CjType.ArrayOfLabelEntries);
    }

    @Override
    public void labelEntryEnd() {
        elementStack.pop(CjType.LabelEntry);
    }

    @Override
    public void labelEntryStart() {
        elementStack.pop(CjType.LabelEntry);
    }

    @Override
    public void labelStart() {
        elementStack.push(CjType.ArrayOfLabelEntries, null);
    }

    @Override
    public void language(String lang) {
        // TODO what to do with label-language in GraphML ? Maybe put into data.
    }

    @Override
    public void listEnd(CjType cjType) {
        switch (cjType) {
            case Node -> elementStack.pop(CjType.ArrayOfNodes);
            case Graph -> elementStack.pop(CjType.ArrayOfGraphs);
            case ArrayOfLabelEntries -> elementStack.pop(CjType.ArrayOfLabelEntries);
            case Edge -> elementStack.pop(CjType.ArrayOfEdges);
            case Port -> elementStack.pop(CjType.ArrayOfPorts);
            case Endpoint -> elementStack.pop(CjType.ArrayOfEndpoints);
        }
    }

    @Override
    public void listStart(CjType cjType) {
        switch (cjType) {
            case Node -> elementStack.push(CjType.ArrayOfNodes, null);
            case Graph -> elementStack.push(CjType.ArrayOfGraphs, null);
            case ArrayOfLabelEntries -> elementStack.push(CjType.ArrayOfLabelEntries, null);
            case Edge -> elementStack.push(CjType.ArrayOfEdges, null);
            case Port -> elementStack.push(CjType.ArrayOfPorts, null);
            case Endpoint -> elementStack.push(CjType.ArrayOfEndpoints, null);
        }
    }

    // FIXME use it
    public IGraphmlKey lookupKey(String id, GraphmlKeyForType forType) {
        Map<GraphmlKeyForType, IGraphmlKey> subMap = dataId_for_key.getOrDefault(id, Collections.emptyMap());
        return subMap.getOrDefault(
                // 1) look up exact forType
                forType,
                // 2) look up ALL forType
                subMap.get(GraphmlKeyForType.All));
    }

    @Override
    public void metaEnd() {
        // TODO GraphML extensions for graph stats
    }

    @Override
    public void metaStart() {
        // TODO GraphML extensions for graph stats
    }

    @Override
    public void meta__edgeCountInGraph(long number) {
        // TODO GraphML extensions for graph stats
    }

    @Override
    public void meta__edgeCountTotal(long number) {
        // TODO GraphML extensions for graph stats
    }

    @Override
    public void meta__nodeCountInGraph(long number) {
        // TODO GraphML extensions for graph stats
    }

    @Override
    public void meta__nodeCountTotal(long number) {
        // TODO GraphML extensions for graph stats
    }

    @Override
    public void nodeEnd() {
        elementStack.pop(CjType.Node);
    }

    @Override
    public void nodeId(String nodeId) {
        elementStack.peek_(CjType.Endpoint).endpointBuilder().node(nodeId);
    }

    @Override
    public void nodeStart() {
        GraphmlNodeBuilder builder = IGraphmlNode.builder();
        elementStack.push(CjType.Node, builder);
    }

    @Override
    public void portEnd() {
        elementStack.pop(CjType.Port);
    }

    @Override
    public void portId(String portId) {
        elementStack.peek_(CjType.Endpoint).endpointBuilder().port(portId);
    }

    @Override
    public void portStart() {
        GraphmlPortBuilder builder = IGraphmlPort.builder();
        elementStack.push(CjType.Port, builder);
    }

    @Override
    public void value(String value) {
        // TODO ????????????
    }

    public void writeEdge(CjJacksonEdge cjEdge, GraphmlWriter graphmlWriter) throws IOException {
        if (cjEdge.getEndpoints().size() == 2) {
            CjJacksonEndpoint s = cjEdge.getEndpoints().get(0);
            CjJacksonEndpoint t = cjEdge.getEndpoints().get(1);
            Boolean directed;
            if (s.cjDirection().isDirected() && t.cjDirection().isDirected()) {
                directed = true;
            } else if (s.cjDirection().isUndirected() && t.cjDirection().isUndirected()) {
                directed = false;
            } else {
                // edge can only be represented as hyper-edge
                directed = null;
            }
            if (directed != null) {
                GraphmlEdge edge = IGraphmlEdge.builder().id(cjEdge.getId()).directed(directed) //
                        .sourceId(s.getNode())//
                        .sourcePortId(s.getPort()).targetId(t.getNode()) //
                        .targetPortId(t.getPort()).build();
                graphmlWriter.edgeStart(edge);
            }
        } else {
            assert cjEdge.getEndpoints() != null;
            assert cjEdge.getEndpoints().size() > 2;
            // default case: hyperedge
            GraphmlHyperEdgeBuilder builder = IGraphmlHyperEdge.builder().id(cjEdge.getId());
            graphmlWriter.hyperEdgeStart(builder.build());
        }

        // TODO data, desc

        if (cjEdge.getEndpoints().size() == 2) {
            graphmlWriter.edgeEnd();
        } else {
            graphmlWriter.hyperEdgeEnd();
        }
    }

    public void writeTo(Cj2GraphmlContext rootContext, GraphmlWriter graphmlWriter) throws IOException {
        graphmlWriter.documentStart(rootContext.documentBuilder().build());
        // TODO other doc level parts
        // TODO set graph XML attributes from ."data"."cj:attributes"

        // IMPROVE merge forType (node+edge+graph) to 'all'
        Stream<IGraphmlKey> allKeys = dataId_for_key.values().stream().flatMap(x -> x.values().stream());
        allKeys.forEach(key -> {
            try {
                graphmlWriter.key(key);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        rootContext.child1(CjType.Data, ctxData -> {
            try {
                graphmlWriter.key(IGraphmlKey.builder().forType(GraphmlKeyForType.All) //
                        .attrName("json")//
                        .attrType(GioDataType.typeString.graphmlName())//
                        .desc(IGraphmlDescription.builder().value("Connected JSON allows custom JSON properties in all objects").build())//
                        .build());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        });


        // TODO write graph
        for (Cj2GraphmlContext g : rootContext.children(CjType.Graph)) {
            g.writeEndTo(graphmlWriter);
        }

        graphmlWriter.documentEnd();
    }

    private void indexKey(String id, GraphmlKeyForType forType, IGraphmlKey key) {
        dataId_for_key.computeIfAbsent(id, k -> new HashMap<>()).put(forType, key);
    }

    /** TODO call from writeGraph */
    private void writeKeyElement(String attName, String attType, GraphmlKeyForType graphmlKeyForType) throws IOException {
        GraphmlKeyBuilder builder = IGraphmlKey.builder();
        builder.forType(graphmlKeyForType);
        builder.attrName(attName);
        builder.attrType(attType);
        IGraphmlKey graphmlKey = builder.build();
        graphmlWriter.key(graphmlKey);
    }


}

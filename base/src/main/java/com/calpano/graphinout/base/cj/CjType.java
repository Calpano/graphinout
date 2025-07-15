package com.calpano.graphinout.base.cj;

import com.calpano.graphinout.foundation.json.JsonType;
import edu.umd.cs.findbugs.annotations.Nullable;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public enum CjType {

    Document(JsonType.Document),//
    Graph(JsonType.Object),//
    ArrayOfGraphs(JsonType.Array),//
    Node(JsonType.Object),//
    ArrayOfNodes(JsonType.Array),//
    Edge(JsonType.Object),//
    ArrayOfEdges(JsonType.Array), //
    Id(JsonType.String, JsonType.Number),
    Port(JsonType.Object),//
    ArrayOfPorts(JsonType.Array),//
    Endpoint(JsonType.Object),//
    ArrayOfEndpoints(JsonType.Array),//
    LabelMonoLang(JsonType.String),//
    LabelMultiLang(JsonType.Array),//
    LabelMultiLangEntry(JsonType.Object),//
    Value(JsonType.String),
    Language(JsonType.String),
    Direction(JsonType.String),
    EdgeType(JsonType.String),
    EdgeDefault(JsonType.String),
    Directed(JsonType.Boolean),
    BaseUri(JsonType.String);

    public static class CjProperty {

        final Set<String> keys;
        Set<CjType> expected;

        public CjProperty(Set<String> keys) {
            this.keys = keys;
        }

        public Set<CjType> expected() {
            return expected;
        }

        /** expected types for given property */
        public void is(CjType... types) {
            // check that JSON types are disjoint, otherwise JSON tokens cannot be mapped to expected types
            EnumSet<JsonType> used = EnumSet.noneOf(JsonType.class);
            this.expected = java.util.Set.of(types);
            for (CjType type : expected) {
                for (JsonType json : type.jsonTypes) {
                    boolean isNew = used.add(json);
                    if (!isNew) {
                        throw new IllegalArgumentException("The JSON type '" + json + "' is already used for another expected type");
                    }
                }
            }
        }

    }

    static {
        Document.property("graph", "graphs").is(Graph, ArrayOfGraphs);
        ArrayOfGraphs.item(Graph);
        Graph.property("id").is(Id);
        Graph.property("label").is(LabelMonoLang, LabelMultiLang);
        Graph.property("edgeDefault").is(EdgeDefault);
        Graph.property("directed").is(Directed);
        Graph.property("baseUri").is(BaseUri);
        Graph.property("node", "nodes").is(Node, ArrayOfNodes);
        Graph.property("edge", "edges").is(Edge, ArrayOfEdges);
        Graph.property("port", "ports").is(Port, ArrayOfPorts);

        Node.property("id").is(Id);
        Node.property("port", "ports").is(Port, ArrayOfPorts);
        Node.property("graph", "graphs").is(Graph, ArrayOfGraphs);
        Node.property("label").is(LabelMonoLang, LabelMultiLang);
        Node.property("graph", "graphs").is(Graph, ArrayOfGraphs);

        Port.property("id").is(Id);
        Port.property("port", "ports").is(Port, ArrayOfPorts);
        Port.property("label").is(LabelMonoLang, LabelMultiLang);

        Edge.property("id").is(Id);
        Edge.property("label").is(LabelMonoLang, LabelMultiLang);
        Edge.property("edgeDefault").is(EdgeDefault);
        Edge.property("directed").is(Directed);
        Edge.property("endpoints").is(Endpoint, ArrayOfEndpoints);

        Endpoint.property("node").is(Id);
        Endpoint.property("port").is(Id);
        Endpoint.property("direction").is(Direction);
        Endpoint.property("edgeType").is(EdgeType);

        LabelMonoLang.property("lang").is(Language);
        LabelMonoLang.property("value").is(Value);

        LabelMultiLang.item(LabelMultiLangEntry);
        LabelMultiLangEntry.property("value").is(Value);
        LabelMultiLangEntry.property("lang").is(Language);

        ArrayOfEdges.item(Edge);
        ArrayOfNodes.item(Node);
        ArrayOfPorts.item(Port);
        ArrayOfEndpoints.item(Endpoint);
    }

    public final Map<String, CjProperty> properties = new HashMap<>();
    public final JsonType[] jsonTypes;
    public @Nullable CjType[] itemTypes;

    CjType(JsonType... jsonTypes) {
        this.jsonTypes = jsonTypes;
    }

    public boolean hasJsonType(JsonType jsonType) {
        for (JsonType type : jsonTypes) {
            if (type == jsonType) return true;
        }
        return false;
    }

    public boolean isArray() {
        return jsonTypes.length == 1 && hasJsonType(JsonType.Array);
    }

    private void item(CjType... itemTypes) {
        if (jsonTypes.length != 1 || jsonTypes[0] != JsonType.Array) {
            throw new IllegalArgumentException("Can only define items for array types.");
        }
        this.itemTypes = itemTypes;

    }

    private CjProperty property(String... keys) {
        CjProperty p = new CjProperty(Set.of(keys));
        p.keys.forEach(key -> properties.put(key, p));
        return p;
    }
}

package com.graphinout.base.cj;

import com.graphinout.foundation.json.JsonConstants;
import com.graphinout.foundation.json.JsonType;
import edu.umd.cs.findbugs.annotations.Nullable;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.graphinout.base.cj.CjConstants.DATA;
import static com.graphinout.base.cj.CjConstants.EDGE_OR_ENDPOINT__TYPE;
import static com.graphinout.base.cj.CjConstants.EDGE_OR_ENDPOINT__TYPE_NODE;
import static com.graphinout.base.cj.CjConstants.EDGE_OR_ENDPOINT__TYPE_URI;
import static com.graphinout.base.cj.CjConstants.EDGE__ENDPOINTS;
import static com.graphinout.base.cj.CjConstants.ENDPOINT__DIRECTION;
import static com.graphinout.base.cj.CjConstants.ENDPOINT__NODE;
import static com.graphinout.base.cj.CjConstants.ENDPOINT__PORT;
import static com.graphinout.base.cj.CjConstants.GRAPHS;
import static com.graphinout.base.cj.CjConstants.GRAPH__EDGES;
import static com.graphinout.base.cj.CjConstants.GRAPH__META;
import static com.graphinout.base.cj.CjConstants.GRAPH__NODES;
import static com.graphinout.base.cj.CjConstants.ID;
import static com.graphinout.base.cj.CjConstants.LABEL;
import static com.graphinout.base.cj.CjConstants.LANGUAGE;
import static com.graphinout.base.cj.CjConstants.PORTS;
import static com.graphinout.base.cj.CjConstants.ROOT__BASE_URI;
import static com.graphinout.base.cj.CjConstants.ROOT__CONNECTED_JSON;
import static com.graphinout.base.cj.CjConstants.VALUE;

/**
 * The CJ JSON types. On which JSON types they are based, and which properties they have (for the object-types).
 */
public enum CjType {

    /** aka Document */
    RootObject(JsonType.Object),//
    JsonSchemaId(JsonType.String),//
    JsonSchemaLocation(JsonType.String),//
    ConnectedJson(JsonType.Object), //
    ConnectedJson__VersionNumber(JsonType.String),//
    ConnectedJson__VersionDate(JsonType.String),//
    ConnectedJson__Canonical(JsonType.Boolean),//
    BaseUri(JsonType.String),//

    /* This type is a Label. A Label really *is* an array or label entries. */
    ArrayOfLabelEntries(JsonType.Array),//
    LabelEntry(JsonType.Object),//
    Language(JsonType.String), Value(JsonType.String), /* Id */
    Id(JsonType.String, JsonType.Number),

    ArrayOfGraphs(JsonType.Array),//
    Graph(JsonType.Object),//
    Meta(JsonType.Object),//

    ArrayOfNodes(JsonType.Array),//
    Node(JsonType.Object),//

    ArrayOfEdges(JsonType.Array), //
    Edge(JsonType.Object),//

    ArrayOfPorts(JsonType.Array),//
    Port(JsonType.Object),//

    ArrayOfEndpoints(JsonType.Array),//
    Endpoint(JsonType.Object),//
    Direction(JsonType.String), //
    NodeId(JsonType.String), //
    PortId(JsonType.String), //
    EdgeTypeUri(JsonType.String), //
    EdgeTypeNodeId(JsonType.String), //
    EdgeTypeString(JsonType.String), //

    Data(JsonType.Object, JsonType.Null, JsonType.Number, JsonType.String, JsonType.Boolean, JsonType.Array),
    ;

    /** A JSON object property as used in CJ */
    public static class CjProperty {

        final Set<String> keys;
        Set<CjType> expected;

        public CjProperty(Set<String> keys) {
            this.keys = keys;
        }

        public Set<CjType> expected() {
            return expected;
        }

        /** expected types for the given property */
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

        @Override
        public String toString() {
            return "CjProperty{" + "keys=" + keys + ", expected=" + expected + '}';
        }

    }

    static {
        RootObject.property(JsonConstants.DOLLAR_ID).is(JsonSchemaId);
        RootObject.property(JsonConstants.DOLLAR_SCHEMA).is(JsonSchemaLocation);
        RootObject.property(ROOT__CONNECTED_JSON).is(ConnectedJson);
        ConnectedJson.property(CjConstants.CONNECTED_JSON__VERSION_NUMBER).is(ConnectedJson__VersionNumber);
        ConnectedJson.property(CjConstants.CONNECTED_JSON__VERSION_DATE).is(ConnectedJson__VersionDate);
        ConnectedJson.property(CjConstants.CONNECTED_JSON__CANONICAL).is(ConnectedJson__Canonical);
        RootObject.property(ROOT__BASE_URI).is(BaseUri);
        RootObject.property(GRAPHS).is(ArrayOfGraphs);
        RootObject.property(DATA).is(Data);

        ArrayOfGraphs.item(Graph);
        Graph.property(ID).is(Id);
        Graph.property(LABEL).is(ArrayOfLabelEntries);
        Graph.property(GRAPH__META).is(Meta);
        Graph.property(DATA).is(Data);
        Graph.property(GRAPH__NODES).is(ArrayOfNodes);
        Graph.property(GRAPH__EDGES).is(ArrayOfEdges);
        Graph.property(GRAPHS).is(ArrayOfGraphs);

        ArrayOfNodes.item(Node);
        Node.property(ID).is(Id);
        Node.property(PORTS).is(ArrayOfPorts);
        Node.property(GRAPHS).is(ArrayOfGraphs);
        Node.property(LABEL).is(ArrayOfLabelEntries);
        Node.property(DATA).is(Data);
        Node.property(GRAPHS).is(ArrayOfGraphs);

        ArrayOfPorts.item(Port);
        Port.property(ID).is(Id);
        Port.property(PORTS).is(ArrayOfPorts);
        Port.property(LABEL).is(ArrayOfLabelEntries);
        Port.property(DATA).is(Data);

        ArrayOfEdges.item(Edge);
        Edge.property(ID).is(Id);
        Edge.property(LABEL).is(ArrayOfLabelEntries);
        Edge.property(EDGE_OR_ENDPOINT__TYPE).is(EdgeTypeString);
        Edge.property(EDGE_OR_ENDPOINT__TYPE_NODE).is(EdgeTypeNodeId);
        Edge.property(EDGE_OR_ENDPOINT__TYPE_URI).is(EdgeTypeUri);
        Edge.property(EDGE__ENDPOINTS).is(ArrayOfEndpoints);
        Edge.property(DATA).is(Data);

        ArrayOfEndpoints.item(Endpoint);
        Endpoint.property(ENDPOINT__NODE).is(NodeId);
        Endpoint.property(ENDPOINT__PORT).is(PortId);
        Endpoint.property(ENDPOINT__DIRECTION).is(Direction);
        Endpoint.property(EDGE_OR_ENDPOINT__TYPE).is(EdgeTypeString);
        Endpoint.property(EDGE_OR_ENDPOINT__TYPE_NODE).is(EdgeTypeNodeId);
        Endpoint.property(EDGE_OR_ENDPOINT__TYPE_URI).is(EdgeTypeUri);
        Endpoint.property(DATA).is(Data);

        ArrayOfLabelEntries.item(LabelEntry);
        LabelEntry.property(VALUE).is(Value);
        LabelEntry.property(LANGUAGE).is(Language);
    }

    /** which properties are allowed in this type (if it is an object) */
    public final Map<String, CjProperty> properties = new HashMap<>();
    public final JsonType[] jsonTypes;
    /** which types are allowed in this type (if it is an array) */
    public @Nullable CjType[] itemTypes;

    CjType(JsonType... jsonTypes) {
        this.jsonTypes = jsonTypes;
    }

    public static CjType findExactlyOne(Set<CjType> set, JsonType jsonType) {
        CjType found = null;
        for (CjType element : set) {
            if (element.hasJsonType(jsonType)) {
                if (found != null) {
                    throw new IllegalArgumentException("Expected exactly one element matching '" + jsonType + "', but found at least two in " + set);
                }
                found = element;
            }
        }
        if (found == null) {
            throw new IllegalArgumentException("Expected exactly one element matching '" + jsonType + "', but found none in " + set);
        }
        return found;
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

    public boolean isContainer() {
        return !properties.isEmpty() || (itemTypes != null && itemTypes.length > 0);
    }

    /** All allowed nested types */
    public Set<CjType> nested() {
        Set<CjType> nested = new HashSet<>();
        if (itemTypes != null) {
            nested.addAll(List.of(itemTypes));
        }
        for (CjProperty property : properties.values()) {
            nested.addAll(property.expected);
        }
        return nested;
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

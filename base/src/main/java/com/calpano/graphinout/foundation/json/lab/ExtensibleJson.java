package com.calpano.graphinout.foundation.json.lab;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static com.calpano.graphinout.foundation.json.lab.ExtensibleJson.JsonType.ARRAY;
import static com.calpano.graphinout.foundation.json.lab.ExtensibleJson.JsonType.BOOLEAN;
import static com.calpano.graphinout.foundation.json.lab.ExtensibleJson.JsonType.NUMBER;
import static com.calpano.graphinout.foundation.json.lab.ExtensibleJson.JsonType.OBJECT;
import static com.calpano.graphinout.foundation.json.lab.ExtensibleJson.JsonType.STRING;

public class ExtensibleJson {

    enum JsonType {ARRAY, OBJECT, STRING, BOOLEAN, NUMBER, NULL}

    class Step {

        private final List<String> propertyKeys;
        private final List<JsonType> typeQueries;
        private final List<Step> children = new ArrayList<>();

        public Step(List<String> propertyKeys, List<JsonType> typeQueries) {
            this.propertyKeys = propertyKeys;
            this.typeQueries = typeQueries;
        }

        public Step inMaybe(JsonType typeFilter) {
            Step child = new Step(List.of(), List.of(typeFilter));
            this.children.add(child);
            return child;
        }

        public Value value(ExtensibleJson customType) {
            return new Value(
                    // FIXME
                    null);
        }


            public Value value(JsonType... typeQueries) {
            return new Value(List.of(typeQueries));
        }

        public Value valueStringEnum(String ... values) {
            // FIXME values
            return new Value(List.of(STRING));
        }

    }

    class Value {

        private final List<JsonType> typeQueries;

        public Value(List<JsonType> typeQueries) {
            this.typeQueries = typeQueries;
        }

        public Value doc(String doc) {
            // FIXME
            return this;
        }

        public void object(Consumer<ExtensibleJson> consumer) {
            // FIXME
        }

    }

    public static ExtensibleJson create() {
        return new ExtensibleJson();
    }

    public static void label(ExtensibleJson element) {
        element.in("label").value(STRING);
        element.in("label").value(OBJECT).object(label -> {
            label.doc("Multilingual Labels");
            label.in("language").value(STRING);
            label.in("value").value(STRING);
        });
    }


    private void doc(String doc) {}

    private Step in(String... propertyKeys) {
        return new Step(List.of(propertyKeys), List.of());
    }

    public static void main(String[] args) {
        ExtensibleJson.create().in("graph", "graphs").inMaybe(ARRAY).value(OBJECT).object(graph -> {
            graph.in("baseUri").value(STRING);
            graph.in("edgeDefault").value(STRING);
            graph.in("id").value(STRING, NUMBER);
            label(graph);
            graph.in("edge", "edges", "hyperedges").inMaybe(ARRAY).value(OBJECT).object(edge -> {
                edge.in("id").value(STRING);
                label(edge);
                edge.in("directed").value(BOOLEAN);
                edge.in("type").value(STRING);
                edge.in("typeNode").value(STRING, NUMBER);
                edge.in("typeUri").value(STRING);
                edge.in("source", "sources", "from").value(STRING, NUMBER);
                edge.in("target", "targets", "to").value(STRING, NUMBER);
                edge.in("endpoint", "endpoints").inMaybe(ARRAY).value(OBJECT).object(endpoint -> {
                    endpoint.in("direction", "dir").valueStringEnum("in","out","undir");
                    endpoint.in("node").value(STRING, NUMBER).doc("referenced node id");
                    endpoint.in("port").value(STRING, NUMBER).doc("referenced port id");
                    endpoint.in("type").value(STRING);
                    endpoint.in("typeNode").value(STRING, NUMBER);
                    endpoint.in("typeUri").value(STRING);
                });
                edge.in("graph","graphs").value(graph).doc("edge can contain graph");
            });
        });
    }

    // ----
    //("graph" | "graphs") ARRAY? | GRAPH = OBJECT
    //    "(edges" | "edge" | "hyperedges") ARRAY? = OBJECT
    //        "id" = STRING
    //        "label" = STRING | OBJECT
    //            "language" = STRING // Multilingual Labels
    //            "value" = STRING // Multilingual Labels
    //        "directed" = BOOLEAN
    //        "type" = STRING
    //        "typeNode" = STRING | NUMBER
    //        "typeUri" = STRING
    //        "source" | "sources" | "from" = STRING | NUMBER
    //        "target" | "targets" | "to" = STRING | NUMBER
    //        ("endpoints" | "endpoint") ARRAY? = OBJECT
    //            "direction" | "dir" = { "in" | "out" | "undir" }
    //            "node" = STRING | NUMBER // referenced node id
    //            "port" = STRING | NUMBER // referenced port id
    //            "type" = STRING
    //            "typeNode" = STRING | NUMBER
    //            "typeUri" = STRING
    //        "graph" --> GRAPH
    //    ("nodes"|"node") ARRAY? = OBJECT
    //        "id" = STRING | NUMBER
    //        "label" = STRING | OBJECT
    //            "language" = STRING // Multilingual Labels
    //            "value" = STRING // Multilingual Labels
    //        "content" = STRING | OBJECT // Knowledge graph extension
    //        "contentType" = STRING // Knowledge graph extension
    //        "graph" --> GRAPH
    //        "ports" ARRAY? --> PORT
    //PORT = OBJECT
    //    "id" = STRING | NUMBER
    //    "label" = STRING | OBJECT
    //        "language" = STRING // Multilingual Labels
    //        "value" = STRING // Multilingual Labels
    //    "ports" ARRAY --> PORT
    //----
}

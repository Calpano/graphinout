package com.graphinout.reader.plantuml;

import com.graphinout.base.cj.document.CjDirection;
import com.graphinout.base.cj.document.ICjDocument;
import com.graphinout.base.cj.document.ICjEndpoint;
import com.graphinout.base.cj.stream.CjStream2CjWriter;
import com.graphinout.base.cj.writer.CjWriter2CjDocumentWriter;
import com.graphinout.base.input.SingleInputSource;
import com.graphinout.foundation.pure.json.document.IJsonValue;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Round-trips a PlantUML class diagram: parse (via the PlantUML library) -> CjDocument -> {@link PlantUmlWriter} ->
 * parse again, asserting the class graph (nodes with kinds + typed/directed relationships) survives and that a second
 * serialization equals the first (stable output).
 */
class PlantUmlRoundtripTest {

    private static final String INPUT = """
            @startuml
            class Dog
            class Animal
            interface Pet
            abstract class Mammal
            enum Color
            class "Big Cat" as Cat
            class Account {
            +id : int
            +deposit(amount)
            }
            package model {
            class User
            class Role
            User --> Role
            }
            Dog --|> Mammal
            Mammal --|> Animal
            Dog ..|> Pet
            Animal *-- Color
            Dog --> Color : likes
            Pet ..> Color
            @enduml
            """;

    @Test
    void roundtripClassDiagram() throws IOException {
        ICjDocument doc1 = read(INPUT);
        String out1 = PlantUmlWriter.toPlantUml(doc1);
        ICjDocument doc2 = read(out1);
        String out2 = PlantUmlWriter.toPlantUml(doc2);

        assertEquals(out1, out2, "second serialization should equal the first");
        assertEquals(structure(doc1), structure(doc2), "class graph should survive the round-trip");

        // sanity: kinds and relationships are captured
        String s = structure(doc1);
        assertTrue(s.contains("Pet:INTERFACE"), "interface kind preserved: " + s);
        assertTrue(s.contains("Mammal:ABSTRACT_CLASS"), "abstract class kind preserved: " + s);
        assertTrue(s.contains("Dog->Mammal:extension"), "extension relation preserved: " + s);
        assertTrue(s.contains("Dog->Pet:realization"), "realization relation preserved: " + s);
        assertTrue(s.contains("Animal->Color:composition"), "composition relation preserved: " + s);
        assertTrue(s.contains("Dog->Color:association-directed"), "directed association preserved: " + s);
        assertTrue(s.contains("Pet->Color:dependency"), "dependency relation preserved: " + s);

        // alias (display name) and members survive
        assertTrue(out1.contains("class \"Big Cat\" as Cat"), "alias/display name preserved:\n" + out1);
        assertTrue(out1.contains("+id : int"), "class member preserved:\n" + out1);
        assertTrue(out1.contains("+deposit(amount)"), "class method preserved:\n" + out1);

        // package grouping survives (as a nested CJ graph)
        assertTrue(out1.contains("package model {"), "package preserved:\n" + out1);
        assertTrue(s.contains("User:CLASS"), "class inside package preserved: " + s);
        assertTrue(s.contains("User->Role:association-directed"), "edge inside package preserved: " + s);
    }

    private static ICjDocument read(String content) throws IOException {
        PlantUmlReader reader = new PlantUmlReader();
        CjWriter2CjDocumentWriter docWriter = new CjWriter2CjDocumentWriter();
        reader.read(SingleInputSource.of("test.puml", content), new CjStream2CjWriter(docWriter, true));
        return docWriter.resultDoc();
    }

    private static String structure(ICjDocument doc) {
        TreeSet<String> nodes = new TreeSet<>();
        doc.nodesAllIncludingImplied().forEach(n -> nodes.add(n.id() + ":" + prop(n.jsonValue(), "uml:kind")));
        List<String> edges = new ArrayList<>();
        doc.edgesAll().forEach(e -> {
            String[] st = sourceTarget(e.endpoints().toList());
            String rel = e.edgeType() != null ? e.edgeType().type() : "?";
            if (st != null) edges.add(st[0] + "->" + st[1] + ":" + rel);
        });
        edges.sort(String::compareTo);
        return "nodes=" + nodes + " edges=" + edges;
    }

    private static String prop(IJsonValue json, String key) {
        if (json == null || !json.isObject()) return "?";
        IJsonValue v = json.resolve(key);
        return (v != null && v.isString()) ? v.asString() : "?";
    }

    private static String[] sourceTarget(List<ICjEndpoint> eps) {
        ICjEndpoint in = null, out = null;
        for (ICjEndpoint ep : eps) {
            if (ep.direction() == CjDirection.IN && in == null) in = ep;
            else if (ep.direction() == CjDirection.OUT && out == null) out = ep;
        }
        if (in != null && out != null) return new String[]{in.node(), out.node()};
        if (eps.size() == 2) return new String[]{eps.get(0).node(), eps.get(1).node()};
        return null;
    }

}

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
        // relationships use standard UML names: `--|>`=generalization, `..|>`=realization, `*--`=composition
        assertTrue(s.contains("Dog->Mammal:generalization"), "generalization relation preserved: " + s);
        assertTrue(s.contains("Dog->Pet:realization"), "realization relation preserved: " + s);
        assertTrue(s.contains("Animal->Color:composition"), "composition relation preserved: " + s);
        // a plain association's colon-label is the relation name (DDot predicate semantics): `Dog --> Color : likes`
        assertTrue(s.contains("Dog->Color:likes"), "labeled association uses the label as the relation: " + s);
        // dashed open-arrow `Pet ..> Color` is a UML dependency
        assertTrue(s.contains("Pet->Color:dependency"), "dashed arrow becomes a UML dependency: " + s);

        // alias (display name) and members survive
        assertTrue(out1.contains("class \"Big Cat\" as Cat"), "alias/display name preserved:\n" + out1);
        assertTrue(out1.contains("+id : int"), "class member preserved:\n" + out1);
        assertTrue(out1.contains("+deposit(amount)"), "class method preserved:\n" + out1);

        // package grouping survives (as a nested CJ graph)
        assertTrue(out1.contains("package model {"), "package preserved:\n" + out1);
        assertTrue(s.contains("User:?"), "class inside package preserved (plain class carries no kind): " + s);
        // a plain solid arrow `User --> Role` is a (directed) UML association
        assertTrue(s.contains("User->Role:association"), "plain solid arrow is a UML association: " + s);
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

    /**
     * Every PlantUML link form parses, serializes to a stable canonical arrow, and round-trips (the reversed form
     * normalises to the canonical one). {@code input -> expected canonical relationship line}.
     */
    @Test
    void roundtripLinkVocabulary() throws IOException {
        String[][] cases = {
                // association (solid) / dependency (dashed); direction normalised, undirected/bidirectional preserved
                {"A --> B", "A --> B"}, {"A <-- B", "B --> A"}, {"A <--> B", "A <--> B"}, {"A -- B", "A -- B"},
                {"A ..> B", "A ..> B"}, {"A <.. B", "B ..> A"}, {"A <..> B", "A <..> B"}, {"A .. B", "A .. B"},
                // decorated UML relations (reversed forms normalise to the canonical orientation)
                {"A --|> B", "A --|> B"}, {"A <|-- B", "B --|> A"},      // generalization
                {"A ..|> B", "A ..|> B"}, {"A <|.. B", "B ..|> A"},      // realization
                {"A *-- B", "A *-- B"}, {"A --* B", "B *-- A"},          // composition
                {"A o-- B", "A o-- B"}, {"A --o B", "B o-- A"},          // aggregation
                // exotic decorations preserved (crowfoot / nested / not-navigable)
                {"A }-- B", "B --{ A"}, {"A --+ B", "A --+ B"}, {"A x-- B", "B --x A"},
                // combined decoration + navigability arrow
                {"A *--> B", "A *--> B"}, {"A o--> B", "A o--> B"},
                // presentation styling (dotted/bold) is preserved, orthogonal to the relation
                {"A -[dotted]-> B", "A -[dotted]-> B"}, {"A -[bold]-> B", "A -[bold]-> B"},
                // a labeled relation: the label is the predicate, the UML kind rides as `line` metadata
                {"A *-- B : owns", "A *-- B : owns"}, {"A --> B : knows", "A --> B : knows"},
        };
        for (String[] c : cases) {
            String input = "@startuml\nclass A\nclass B\n" + c[0] + "\n@enduml\n";
            String out1 = PlantUmlWriter.toPlantUml(read(input));
            String out2 = PlantUmlWriter.toPlantUml(read(out1));
            assertEquals(out1, out2, "unstable round-trip for: " + c[0]);
            assertTrue(out1.contains(c[1]), "expected '" + c[1] + "' for input '" + c[0] + "' but got:\n" + out1);
        }
    }

}

package com.graphinout.reader.mermaid;

import com.graphinout.base.cj.document.ICjDocument;
import com.graphinout.base.cj.document.ICjEdge;
import com.graphinout.base.input.SingleInputSource;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Mermaid relations keep their direction: the CJ source (endpoint direction "in") is where the arrow starts, the
 * target ("out") where its head is. Inheritance triangles mark the target, composition/aggregation diamonds the
 * source (the whole), as in the PlantUML reader.
 */
class MermaidDirectionTest {

    private static List<String> edges(String mermaid) throws IOException {
        ICjDocument doc = new MermaidReader().readToCjDocument(SingleInputSource.of("test.mmd", mermaid));
        return doc.edgesAll().map(ICjEdge.class::cast)
                .map(e -> e.source().node() + "->" + e.target().node()).sorted().collect(Collectors.toList());
    }

    @Test
    void classDiagramInheritance() throws IOException {
        // Dog inherits from Animal, written both ways
        assertEquals(List.of("Dog->Animal"), edges("classDiagram\n    Animal <|-- Dog"));
        assertEquals(List.of("Dog->Animal"), edges("classDiagram\n    Dog --|> Animal"));
    }

    @Test
    void classDiagramArrows() throws IOException {
        assertEquals(List.of("B->A"), edges("classDiagram\n    A <-- B"));
        assertEquals(List.of("A->B"), edges("classDiagram\n    A --> B"));
        assertEquals(List.of("B->A"), edges("classDiagram\n    A <.. B"));
        assertEquals(List.of("A->B"), edges("classDiagram\n    A ..> B"));
    }

    @Test
    void classDiagramDiamondMarksTheWhole() throws IOException {
        assertEquals(List.of("Car->Wheel"), edges("classDiagram\n    Car *-- Wheel"));
        assertEquals(List.of("Car->Wheel"), edges("classDiagram\n    Wheel --* Car"));
        assertEquals(List.of("Pond->Duck"), edges("classDiagram\n    Pond o-- Duck"));
        assertEquals(List.of("Pond->Duck"), edges("classDiagram\n    Duck --o Pond"));
    }

    @Test
    void flowchartLeftArrow() throws IOException {
        assertEquals(List.of("A->B"), edges("flowchart LR\n    A --> B"));
        assertEquals(List.of("B->A"), edges("flowchart LR\n    A <-- B"));
    }

    @Test
    void architectureLeftArrow() throws IOException {
        assertEquals(List.of("a->b"), edges("architecture-beta\n    service a\n    service b\n    a:R --> L:b"));
        assertEquals(List.of("b->a"), edges("architecture-beta\n    service a\n    service b\n    a:R <-- L:b"));
    }

    @Test
    void architectureSideHintBeforeRightId() throws IOException {
        // the side hint of the right service precedes its id: "R:server" is the service "server", not a node "R"
        assertEquals(List.of("db->server"), edges("architecture-beta\n    service db\n    service server\n    db:L -- R:server"));
    }
}

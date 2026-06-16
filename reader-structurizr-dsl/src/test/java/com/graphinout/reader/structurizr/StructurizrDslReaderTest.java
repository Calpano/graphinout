package com.graphinout.reader.structurizr;

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
 * Parses a small Structurizr DSL C4 workspace and asserts the resulting graph: every model element becomes a node
 * (with its C4 kind), relationships become directed edges, and containment becomes {@code contains} edges.
 */
class StructurizrDslReaderTest {

    private static final String DSL = """
            workspace "Example" {
                model {
                    user = person "User"
                    system = softwareSystem "Software System" {
                        web = container "Web Application" "Delivers content" "Java"
                        db = container "Database" "Stores data" "PostgreSQL"
                    }
                    user -> web "Uses" "HTTPS"
                    web -> db "Reads from and writes to" "JDBC"
                }
            }
            """;

    @Test
    void parsesC4Model() throws IOException {
        ICjDocument doc = read(DSL);

        // 4 elements: 1 person, 1 software system, 2 containers
        TreeSet<String> kinds = new TreeSet<>();
        doc.nodesAllIncludingImplied().forEach(n -> kinds.add(n.id() + ":" + prop(n.jsonValue(), "c4:kind")));
        long persons = kinds.stream().filter(s -> s.endsWith(":Person")).count();
        long systems = kinds.stream().filter(s -> s.endsWith(":SoftwareSystem")).count();
        long containers = kinds.stream().filter(s -> s.endsWith(":Container")).count();
        assertEquals(1, persons, kinds.toString());
        assertEquals(1, systems, kinds.toString());
        assertEquals(2, containers, kinds.toString());

        // edges: 2 relationships + 2 containment (system contains web, system contains db)
        List<String> rels = new ArrayList<>();
        List<String> contains = new ArrayList<>();
        doc.edgesAll().forEach(e -> {
            String[] st = sourceTarget(e.endpoints().toList());
            if (st == null) return;
            String type = prop(e.jsonValue(), "c4:rel");
            (type.equals("contains") ? contains : rels).add(st[0] + "->" + st[1] + ":" + type);
        });
        assertEquals(2, contains.size(), "two containment edges (system contains web, db): " + contains);
        // 2 explicit (user->web, web->db) + 1 implied by Structurizr (user->system, since web is in system).
        assertEquals(3, rels.size(), "explicit + implied relationship edges: " + rels);
        assertTrue(rels.stream().allMatch(r -> r.endsWith(":uses")), "tech'd relationships typed 'uses': " + rels);
    }

    private static ICjDocument read(String content) throws IOException {
        StructurizrDslReader reader = new StructurizrDslReader();
        CjWriter2CjDocumentWriter docWriter = new CjWriter2CjDocumentWriter();
        reader.read(SingleInputSource.of("test.dsl", content), new CjStream2CjWriter(docWriter, true));
        return docWriter.resultDoc();
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

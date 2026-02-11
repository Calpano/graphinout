package com.graphinout.reader.graphml.cj;

import com.graphinout.base.cj.document.ICjDocument;
import com.graphinout.base.cj.document.impl.CjDocumentElement;
import com.graphinout.base.cj.stream.CjStream2CjWriter;
import com.graphinout.base.cj.writer.Cj2JsonWriter;
import com.graphinout.base.cj.writer.CjWriter2CjStream;
import com.graphinout.foundation.pure.json.writer.impl.Json2StringWriter;
import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;

class CjStream2GraphmlWriterTest {


    static String runPipeline(ICjDocument doc) {
        Json2StringWriter jsonWriter = new Json2StringWriter();
        Cj2JsonWriter cj2JsonWriter = new Cj2JsonWriter(jsonWriter);
        CjStream2CjWriter cjStream2CjWriter = new CjStream2CjWriter(cj2JsonWriter, true);
        CjWriter2CjStream cjWriter2CjStream = new CjWriter2CjStream(cjStream2CjWriter);
        doc.fire(cjWriter2CjStream, false);
        return jsonWriter.jsonString();
    }

    @Test
    void test1() {
        // a nested doc / graph / node / graph / node
        CjDocumentElement doc = new CjDocumentElement();
        doc.addGraph(graph1 -> {
            graph1.id("graph-1");
            graph1.addNode(node1 -> {
                node1.id("node-1A-withGraph");
                node1.addGraph(graph2 -> {
                    graph2.id("graph-1A-1");
                });
            });
            graph1.addNode(node2 -> {
                node2.id("node-1A-empty");
            });
        });
        String json = runPipeline(doc);
        assertThat(json).isEqualTo("""
                {"$schema":"https://j-s-o-n.org/schema/cj-7.0.0.json","connectedJson":{"versionDate":"2026-01-15","versionNumber":"7.0.0"},"graphs":[{"id":"graph-1","nodes":[{"id":"node-1A-withGraph","graphs":[{"id":"graph-1A-1"}]},{"id":"node-1A-empty"}]}]}""");
    }

    @Test
    void test2() {
        CjDocumentElement doc = new CjDocumentElement();
        doc.addGraph(graph1 -> {
            graph1.id("graph-1");
            graph1.addNode(node1 -> node1.id("node-1A"));
            graph1.addNode(node2 -> node2.id("node-1B"));
            graph1.addBiEdge("node-1A","node-1B");
        });
        String json = runPipeline(doc);
        assertThat(json).isEqualTo("""
                {"$schema":"https://j-s-o-n.org/schema/cj-7.0.0.json","connectedJson":{"versionDate":"2026-01-15","versionNumber":"7.0.0"},"graphs":[{"id":"graph-1","nodes":[{"id":"node-1A"},{"id":"node-1B"}],"edges":[{"endpoints":[{"node":"node-1A","direction":"in"},{"node":"node-1B","direction":"out"}]}]}]}""");

    }

}

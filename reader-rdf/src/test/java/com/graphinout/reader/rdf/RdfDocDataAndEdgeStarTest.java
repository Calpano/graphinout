package com.graphinout.reader.rdf;

import com.graphinout.base.cj.document.CjDocuments;
import com.graphinout.base.cj.document.ICjDocument;
import com.graphinout.base.cj.document.ICjEdge;
import com.graphinout.base.cj.stream.CjStream2CjWriter;
import com.graphinout.base.cj.stream.ICjStream;
import com.graphinout.base.cj.writer.CjWriter2CjDocumentWriter;
import com.graphinout.base.cj.writer.CjWriter2CjStream;
import com.graphinout.base.cj.writer.ICjWriter;
import com.graphinout.base.input.SingleInputSource;
import com.graphinout.base.output.InMemoryOutputSink;
import com.graphinout.foundation.pure.json.document.IJsonValue;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;

import java.io.IOException;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * E10 (document metadata via the {@code cj:thisDocument} sentinel) and E8 (per-edge metadata via
 * RDF-star) round-trips: CJ -&gt; RDF (Turtle) -&gt; CJ.
 */
class RdfDocDataAndEdgeStarTest {

    private static final Logger log = getLogger(RdfDocDataAndEdgeStarTest.class);

    /** CJ -&gt; RDF (Turtle) -&gt; CJ. */
    private static ICjDocument roundTrip(ICjDocument cjDoc) throws IOException {
        RdfTurtleReader rdf = new RdfTurtleReader();

        // CJ -> RDF (Turtle)
        InMemoryOutputSink sink = InMemoryOutputSink.create();
        ICjStream outStream = rdf.createCjStream(sink);
        ICjWriter cjWriter = new CjWriter2CjStream(outStream);
        cjDoc.fire(cjWriter, false);
        String turtle = sink.getBufferAsUtf8String();
        log.info("Turtle:\n{}", turtle);

        // RDF (Turtle) -> CJ
        CjWriter2CjDocumentWriter cj2doc = new CjWriter2CjDocumentWriter();
        ICjStream inStream = new CjStream2CjWriter(cj2doc, true);
        rdf.read(SingleInputSource.of("rt.ttl", turtle), inStream);
        ICjDocument result = cj2doc.resultDoc();
        assertNotNull(result);
        return result;
    }

    private static ICjEdge firstEdge(ICjDocument doc) {
        return doc.graphs().findFirst().orElseThrow().edges().findFirst().orElseThrow();
    }

    @Test
    void documentDataRoundTrips() throws IOException {
        String cjJson = """
                {
                  "@context": { "ex": "http://example.org/" },
                  "data": { "ex:author": "Alice", "ex:year": "2020" },
                  "graphs": [ { "nodes": [ { "id": "ex:a" } ] } ]
                }
                """;
        ICjDocument cjDoc = CjDocuments.parseCjJsonString("in.cj.json", cjJson);
        ICjDocument result = roundTrip(cjDoc);

        IJsonValue data = result.data().jsonValue();
        assertNotNull(data, "document data must survive the RDF round-trip");
        assertThat(data.isObject()).isTrue();
        assertThat(data.asObject().keys()).containsExactly("ex:author", "ex:year");
        assertThat(data.asObject().get("ex:author").asString()).isEqualTo("Alice");
        assertThat(data.asObject().get("ex:year").asString()).isEqualTo("2020");
    }

    @Test
    void documentDataRepeatedKeyAccumulatesToArray() throws IOException {
        String cjJson = """
                {
                  "@context": { "ex": "http://example.org/" },
                  "data": { "ex:author": [ "Alice", "Bob" ] },
                  "graphs": [ { "nodes": [ { "id": "ex:a" } ] } ]
                }
                """;
        ICjDocument cjDoc = CjDocuments.parseCjJsonString("in.cj.json", cjJson);
        ICjDocument result = roundTrip(cjDoc);

        IJsonValue data = result.data().jsonValue();
        assertNotNull(data);
        IJsonValue authors = data.asObject().get("ex:author");
        assertNotNull(authors);
        assertThat(authors.isArray()).isTrue();
        assertThat(authors.asArray().stream().map(IJsonValue::asString).toList())
                .containsExactly("Alice", "Bob");
    }

    @Test
    void edgePropsAndTextRoundTripViaRdfStar() throws IOException {
        String cjJson = """
                {
                  "@context": { "ex": "http://example.org/" },
                  "graphs": [ {
                    "nodes": [ { "id": "ex:alice" }, { "id": "ex:bob" } ],
                    "edges": [ {
                      "endpoints": [ { "node": "ex:alice", "direction": "in" },
                                     { "node": "ex:bob", "direction": "out" } ],
                      "type": "ex:knows",
                      "data": { "ddot-it:props": { "ex:since": "2020" }, "ddot-it:text": "a note" }
                    } ]
                  } ]
                }
                """;
        ICjDocument cjDoc = CjDocuments.parseCjJsonString("in.cj.json", cjJson);
        ICjDocument result = roundTrip(cjDoc);

        ICjEdge edge = firstEdge(result);
        assertThat(edge.type()).isEqualTo("ex:knows");
        IJsonValue edgeData = edge.data().jsonValue();
        assertNotNull(edgeData, "edge data must survive the RDF-star round-trip");
        assertThat(edgeData.isObject()).isTrue();

        IJsonValue props = edgeData.asObject().get("ddot-it:props");
        assertNotNull(props, "ddot-it:props must round-trip");
        assertThat(props.asObject().get("ex:since").asString()).isEqualTo("2020");

        IJsonValue text = edgeData.asObject().get("ddot-it:text");
        assertNotNull(text, "ddot-it:text must round-trip");
        assertThat(text.asString()).isEqualTo("a note");
    }

    @Test
    void edgeTextWithMultipleNotesRoundTrips() throws IOException {
        String cjJson = """
                {
                  "@context": { "ex": "http://example.org/" },
                  "graphs": [ {
                    "nodes": [ { "id": "ex:alice" }, { "id": "ex:bob" } ],
                    "edges": [ {
                      "endpoints": [ { "node": "ex:alice", "direction": "in" },
                                     { "node": "ex:bob", "direction": "out" } ],
                      "type": "ex:knows",
                      "data": { "ddot-it:text": [ "note one", "note two" ] }
                    } ]
                  } ]
                }
                """;
        ICjDocument cjDoc = CjDocuments.parseCjJsonString("in.cj.json", cjJson);
        ICjDocument result = roundTrip(cjDoc);

        IJsonValue text = firstEdge(result).data().jsonValue().asObject().get("ddot-it:text");
        assertNotNull(text);
        assertThat(text.isArray()).isTrue();
        assertThat(text.asArray().stream().map(IJsonValue::asString).toList())
                .containsExactly("note one", "note two");
    }

    @Test
    void edgeWithoutDataEmitsNoStarAnnotations() throws IOException {
        String cjJson = """
                {
                  "@context": { "ex": "http://example.org/" },
                  "graphs": [ {
                    "nodes": [ { "id": "ex:alice" }, { "id": "ex:bob" } ],
                    "edges": [ {
                      "endpoints": [ { "node": "ex:alice", "direction": "in" },
                                     { "node": "ex:bob", "direction": "out" } ],
                      "type": "ex:knows"
                    } ]
                  } ]
                }
                """;
        ICjDocument cjDoc = CjDocuments.parseCjJsonString("in.cj.json", cjJson);
        ICjDocument result = roundTrip(cjDoc);

        ICjEdge edge = firstEdge(result);
        assertThat(edge.type()).isEqualTo("ex:knows");
        assertThat(edge.data().isEmpty()).isTrue();
    }
}

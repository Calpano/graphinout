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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * Per-syntax serialisation and metadata round-trips (Tasks 1-3). Verifies that the writer emits each
 * requested RDF syntax (not Turtle for everything) and that BOTH per-edge metadata (E8) and document
 * metadata (E10) round-trip CJ -&gt; RDF[syntax] -&gt; CJ in RDF-star syntaxes (Turtle/TriG) AND in the
 * non-star syntaxes (RDF/XML, JSON-LD) via classic rdf:Statement reification.
 */
class RdfPerSyntaxMetadataTest {

    private static final Logger log = getLogger(RdfPerSyntaxMetadataTest.class);

    /** All readers (one per syntax) under test for edge + document metadata round-trips. */
    static Stream<Arguments> readers() {
        return Stream.of(
                Arguments.of("Turtle", (Supplier<RdfReader>) RdfTurtleReader::new, "rt.ttl"),
                Arguments.of("TriG", (Supplier<RdfReader>) RdfTriGReader::new, "rt.trig"),
                Arguments.of("RDF/XML", (Supplier<RdfReader>) RdfXmlReader::new, "rt.rdf"),
                Arguments.of("JSON-LD", (Supplier<RdfReader>) JsonLdReader::new, "rt.jsonld"),
                Arguments.of("N-Triples", (Supplier<RdfReader>) RdfNTriplesReader::new, "rt.nt"));
    }

    private static String serialize(ICjDocument cjDoc, RdfReader rdf) throws IOException {
        InMemoryOutputSink sink = InMemoryOutputSink.create();
        ICjStream outStream = rdf.createCjStream(sink);
        ICjWriter cjWriter = new CjWriter2CjStream(outStream);
        cjDoc.fire(cjWriter, false);
        return sink.getBufferAsUtf8String();
    }

    private static ICjDocument readBack(RdfReader rdf, String name, String rdfText) throws IOException {
        CjWriter2CjDocumentWriter cj2doc = new CjWriter2CjDocumentWriter();
        ICjStream inStream = new CjStream2CjWriter(cj2doc, true);
        rdf.read(SingleInputSource.of(name, rdfText), inStream);
        ICjDocument result = cj2doc.resultDoc();
        assertNotNull(result);
        return result;
    }

    private static ICjEdge firstEdge(ICjDocument doc) {
        return doc.graphs().findFirst().orElseThrow().edges().findFirst().orElseThrow();
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("readers")
    void edgeMetadataRoundTripsPerSyntax(String label, Supplier<RdfReader> readerFactory, String name) throws IOException {
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
        String rdfText = serialize(cjDoc, readerFactory.get());
        log.info("{}:\n{}", label, rdfText);

        ICjDocument result = readBack(readerFactory.get(), name, rdfText);

        ICjEdge edge = firstEdge(result);
        // Prefix-less syntaxes (N-Triples/N-Quads) carry no @prefix, so ids stay as full IRIs;
        // prefix-carrying syntaxes compact back to ex:knows. Accept either faithful form.
        assertThat(edge.type()).isAnyOf("ex:knows", "http://example.org/knows");
        boolean compacted = "ex:knows".equals(edge.type());
        String sinceKey = compacted ? "ex:since" : "http://example.org/since";

        IJsonValue edgeData = edge.data().jsonValue();
        assertNotNull(edgeData, label + ": edge data must survive the round-trip");
        IJsonValue props = edgeData.asObject().get("ddot-it:props");
        assertNotNull(props, label + ": ddot-it:props must round-trip");
        assertThat(props.asObject().get(sinceKey).asString()).isEqualTo("2020");
        IJsonValue text = edgeData.asObject().get("ddot-it:text");
        assertNotNull(text, label + ": ddot-it:text must round-trip");
        assertThat(text.asString()).isEqualTo("a note");
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("readers")
    void documentMetadataRoundTripsPerSyntax(String label, Supplier<RdfReader> readerFactory, String name) throws IOException {
        String cjJson = """
                {
                  "@context": { "ex": "http://example.org/" },
                  "data": { "ex:author": "Alice", "ex:year": "2020" },
                  "graphs": [ { "nodes": [ { "id": "ex:a" } ] } ]
                }
                """;
        ICjDocument cjDoc = CjDocuments.parseCjJsonString("in.cj.json", cjJson);
        String rdfText = serialize(cjDoc, readerFactory.get());
        ICjDocument result = readBack(readerFactory.get(), name, rdfText);

        IJsonValue data = result.data().jsonValue();
        assertNotNull(data, label + ": document data must survive the round-trip");
        // Prefix-carrying syntaxes compact to ex:author; prefix-less ones keep full IRIs.
        boolean compacted = data.asObject().keys().contains("ex:author");
        String authorKey = compacted ? "ex:author" : "http://example.org/author";
        String yearKey = compacted ? "ex:year" : "http://example.org/year";
        assertThat(data.asObject().keys()).containsExactly(authorKey, yearKey);
        assertThat(data.asObject().get(authorKey).asString()).isEqualTo("Alice");
        assertThat(data.asObject().get(yearKey).asString()).isEqualTo("2020");
    }

    /** The writer must emit the requested syntax, not Turtle for everything (Task 1). */
    @Test
    void writerEmitsRequestedSyntax() throws IOException {
        String cjJson = """
                {
                  "@context": { "ex": "http://example.org/" },
                  "graphs": [ {
                    "nodes": [ { "id": "ex:alice" }, { "id": "ex:bob" } ],
                    "edges": [ { "endpoints": [ { "node": "ex:alice", "direction": "in" },
                                                { "node": "ex:bob", "direction": "out" } ],
                                 "type": "ex:knows" } ]
                  } ]
                }
                """;
        ICjDocument cjDoc = CjDocuments.parseCjJsonString("in.cj.json", cjJson);

        String xml = serialize(cjDoc, new RdfXmlReader());
        assertTrue(xml.contains("<rdf:RDF"), "RDF/XML must contain <rdf:RDF; got:\n" + xml);

        String jsonld = serialize(cjDoc, new JsonLdReader());
        assertTrue(jsonld.contains("@") && jsonld.trim().startsWith("{"),
                "JSON-LD must look like JSON with @keywords; got:\n" + jsonld);

        String nt = serialize(cjDoc, new RdfNTriplesReader());
        // N-Triples: each non-empty line is a full triple ending with " ."
        assertTrue(nt.lines().filter(l -> !l.isBlank()).allMatch(l -> l.trim().endsWith(".")),
                "N-Triples must be one triple per line ending with '.'; got:\n" + nt);
        assertTrue(!nt.contains("@prefix"), "N-Triples must not use @prefix; got:\n" + nt);

        String trig = serialize(cjDoc, new RdfTriGReader());
        assertTrue(trig.contains("ex:knows") || trig.contains("http://example.org/knows"),
                "TriG must contain the knows predicate; got:\n" + trig);
    }
}

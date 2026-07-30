package com.graphinout.reader.ddot;

import com.graphinout.base.cj.document.CjDirection;
import com.graphinout.base.cj.document.CjDocuments;
import com.graphinout.base.cj.document.ICjDocument;
import com.graphinout.base.cj.document.ICjEdge;
import com.graphinout.base.cj.document.ICjGraph;
import com.graphinout.base.input.SingleInputSource;
import com.graphinout.foundation.pure.json.document.IJsonValue;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * {@code ddot.it/block} (shorthand {@code !!block}): the object value is the following lines until the end
 * marker — a blank line by default, or a custom {@code ?end=MARKER}. Within a block, triples are NOT
 * recognised (a {@code ..} line is literal content). A {@code ,,} on the opening line is the block's
 * metadata (and may flag it an RDF literal). See https://ddot.it/block.
 */
class DDotBlockTest {

    private static ICjDocument parse(String ddot) throws IOException {
        return DDotReader.parseDDotToCjDocument(SingleInputSource.of("in.ddot", ddot));
    }

    private static ICjGraph graph(String ddot) throws IOException {
        return parse(ddot).graphs().findFirst().orElseThrow();
    }

    private static String targetOf(ICjGraph g, String type) {
        ICjEdge e = g.edges().filter(x -> type.equals(x.type())).findFirst().orElseThrow();
        return e.endpoints().filter(ep -> ep.direction() == CjDirection.OUT).findFirst().orElseThrow().node();
    }

    @Test
    void blankLineTerminatesBlock() throws IOException {
        ICjGraph g = graph("""
                john ..address.. ddot.it/block
                Broadway 1
                Berlin
                Germany

                john ..age.. 11
                """);
        assertEquals(2, g.edges().count(), "address + age");
        assertEquals("Broadway 1\nBerlin\nGermany", targetOf(g, "address"), "multi-line block value");
        assertEquals("11", targetOf(g, "age"), "the triple after the blank-line terminator is parsed normally");
    }

    @Test
    void endOfFileTerminatesBlock() throws IOException {
        ICjGraph g = graph("""
                x ..note.. !!block
                line one
                line two
                """);
        assertEquals("line one\nline two", targetOf(g, "note"));
    }

    @Test
    void customEndMarkerAndDottedContent() throws IOException {
        // content may contain `..` lines and even a blank line; only the `?end=` marker terminates
        ICjGraph g = graph("""
                s ..p.. ddot.it/block?end=END
                a ..b.. c

                still in block
                END
                s ..q.. after
                """);
        assertEquals("a ..b.. c\n\nstill in block", targetOf(g, "p"), "dotted and blank lines are content");
        assertEquals("after", targetOf(g, "q"));
    }

    @Test
    void metadataAfterBlockFlagsLiteralBlock() throws IOException {
        // A `,, ..rdf:datatype..` marker makes the multi-line value an RDF literal. It CANNOT sit on the
        // opening line: a `!!block` opener must end its physical line (tokenizer BLOCK_OPENER: `… WS*$`;
        // parse spec "block-as-field"), so `..addr.. ddot.it/block ,, …` would make `ddot.it/block` a
        // literal object string, exactly as corpus case 30/34 put the block LAST on the line. The
        // metadata goes on a continuation line instead (corpus case 32: `,, ..since.. 2016` after a block).
        ICjDocument doc = parse("""
                john ..addr.. ddot.it/block
                123 Main
                Apt 4

                ,, ..rdf:datatype.. http://www.w3.org/2001/XMLSchema#string
                """);
        IJsonValue lit = doc.graphs().findFirst().orElseThrow()
                .nodes().filter(n -> "john".equals(n.id())).findFirst().orElseThrow()
                .data().jsonValue().asObject().get("rdf:data").asObject().get("addr");
        assertEquals("123 Main\nApt 4", lit.asObject().get("value").asString());
        assertEquals("http://www.w3.org/2001/XMLSchema#string", lit.asObject().get("datatype").asString());
        assertEquals(0, doc.graphs().findFirst().orElseThrow().edges().count(), "a literal block is not an edge");
    }

    @Test
    void blockRoundTrips() throws IOException {
        // a value containing a blank line forces a custom end marker on write; it must still round-trip
        ICjDocument cj1 = parse("""
                s ..p.. ddot.it/block?end=Z
                first
                a ..b.. c

                last
                Z
                s ..q.. x
                """);
        ICjDocument cj2 = parse(new DDotOutput(cj1).toDDot());
        assertEquals(CjDocuments.toJsonString(cj1), CjDocuments.toJsonString(cj2));
    }
}

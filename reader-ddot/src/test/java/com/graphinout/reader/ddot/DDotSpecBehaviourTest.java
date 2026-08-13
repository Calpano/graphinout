package com.graphinout.reader.ddot;

import com.graphinout.base.cj.document.CjDirection;
import com.graphinout.base.cj.document.CjDocuments;
import com.graphinout.base.cj.document.ICjDocument;
import com.graphinout.base.cj.document.ICjEdge;
import com.graphinout.base.cj.document.ICjGraph;
import com.graphinout.base.input.SingleInputSource;
import com.graphinout.foundation.pure.json.document.IJsonObject;
import com.graphinout.foundation.pure.json.document.IJsonValue;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The spec-conformant behaviours the reader gained when it stopped hand-rolling the ddot.it grammar and
 * started folding {@code it.ddot:ddotit-core}'s event stream. Each test below is a case the old
 * line-splitting parser got wrong; the corresponding shared-corpus case is named on each.
 *
 * @see DDotCorpusConformanceTest
 */
class DDotSpecBehaviourTest {

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

    private static IJsonObject props(ICjEdge edge) {
        return edge.data().jsonValue().asObject().get(DDotOutput.LINK_PROPS_KEY).asObject();
    }

    /** {@code ;;} separates further inline metadata pairs (corpus 22-inline-meta-separator). */
    @Test
    void semicolonSeparatesInlineMetaPairs() throws IOException {
        ICjEdge e = graph("John Doe ..leads.. Project Eagle ,, ..since.. 2025 ;; ..until.. 2027\n")
                .edges().findFirst().orElseThrow();
        assertEquals("2025", props(e).get("since").asString());
        assertEquals("2027", props(e).get("until").asString());
    }

    /** Inside a {@code ,,} block, {@code ;;} is ordinary content (corpus 28-semicolon-in-meta-block). */
    @Test
    void semicolonInsideMetaBlockIsContent() throws IOException {
        ICjEdge e = graph("""
                Dirk Hagemann ..works at.. SAP ,,
                ..note.. first ;; second
                ,,
                """).edges().findFirst().orElseThrow();
        assertEquals("first ;; second", props(e).get("note").asString());
    }

    /**
     * An object may contain {@code ..}: only EXACTLY-two-dot runs are separators, and the run closing the
     * relation is always a plain {@code ..} (corpus 20-object-dotdot). The old reader rejected both lines
     * as "invalid triple" because it split on every {@code \s*\.\.\s*}.
     */
    @Test
    void objectMayContainDoubleDots() throws IOException {
        ICjGraph g = graph("""
                a ..spec.. http://h/x..y
                a ..links to.. ../../b.adoc
                """);
        assertEquals("http://h/x..y", targetOf(g, "spec"));
        assertEquals("../../b.adoc", targetOf(g, "links to"));
    }

    /**
     * Prose is not a triple: a line qualifies only with a complete operator skeleton — two {@code ..} runs
     * or one {@code ....} (corpus 23-not-a-triple). Notably {@code Wait...} is a THREE-dot run and so is
     * not a separator at all; the old reader split it into a bogus triple.
     */
    @Test
    void proseIsNotATriple() throws IOException {
        ICjGraph g = graph("""
                Just some ordinary prose here.
                Wait... what did you say?
                A lone .. in prose, only one marker.
                Mr. Smith went to U.S.A. today.
                """);
        assertEquals(0, g.edges().count());
        assertEquals(0, g.nodes().count());
    }

    /**
     * ddot.it has no {@code #} line-comment rule — the old reader invented one. A {@code #} line is
     * ordinary text: it is ignored when it is not a triple, and read as one when it is.
     */
    @Test
    void hashIsNotAComment() throws IOException {
        ICjGraph g = graph("""
                # just a note
                # Alice ..knows.. Bob
                """);
        assertEquals(1, g.edges().count(), "the second line IS a triple");
        assertTrue(g.nodes().anyMatch(n -> "# Alice".equals(n.id())), "`#` is part of the subject");
    }

    /**
     * {@code off}/{@code on} are recognised anywhere on a line, because the usual way to write them is
     * inside a host-language comment (corpus 31-off-in-comment). The old reader only matched them alone on
     * a trimmed line, so the muted region leaked into the graph.
     */
    @Test
    void offAndOnAreRecognisedInsideHostComments() throws IOException {
        ICjGraph g = graph("""
                Active ..likes.. Coffee
                <!-- ddot.it/off -->
                Draft ..maybe.. ship it
                # !!on
                Back ..works.. now
                """);
        assertEquals(2, g.edges().count(), "the region between the two directives is excluded");
        assertTrue(g.nodes().noneMatch(n -> "Draft".equals(n.id())));
    }

    /** A {@code !!block} may fill the SUBJECT slot (corpus 29-block-subject). */
    @Test
    void blockCanFillTheSubjectSlot() throws IOException {
        ICjGraph g = graph("""
                !!block
                Alice
                Anderson

                ..knows.. Bob
                """);
        assertEquals(1, g.edges().count());
        assertTrue(g.nodes().anyMatch(n -> "Alice\nAnderson".equals(n.id())), "the block is the subject id");
    }

    /**
     * Payloads ride on one line separated by {@code ;;} (only the first {@code ,,} is a delimiter), and
     * since ddot.it 1.3.0 that holds for a free-text note as much as for a typed pair: {@code ;;} ends the
     * entry it stands in whatever that entry is, so pairs and notes mix inline in any order. Both must
     * round-trip losslessly.
     */
    @Test
    void severalPayloadsPickTheReadableSpelling() throws IOException {
        ICjDocument cj1 = parse("""
                A ..works at.. B ,, ..since.. 2010 ;; ..until.. 2020
                C ..leads.. D ,,
                ..k.. v
                a free note
                ,,
                """);
        String ddot2 = new DDotOutput(cj1).toDDot();
        assertTrue(ddot2.contains(",, ..since.. 2010 ;; ..until.. 2020"),
                () -> "two typed pairs belong inline, separated by `;;`:\n" + ddot2);
        assertTrue(ddot2.contains(",, ..k.. v ;; a free note"),
                () -> "a pair and a note mix inline, parted by `;;`:\n" + ddot2);
        ICjDocument cj2 = parse(ddot2);
        assertEquals(CjDocuments.toJsonString(cj1), CjDocuments.toJsonString(cj2), () -> ddot2);
    }

    /**
     * Several notes on one link stay SEVERAL. A link may carry more than one note — inline they are parted
     * by {@code ;;}, in a block by any pair line between them — so they are held apart in CJ (an array under
     * {@code ddot-it:text}, the same string-or-array shape the repeated keys under {@code ddot-it:props}
     * use) and written back apart. Joining them into one value made three notes come back as one.
     */
    @Test
    void severalNotesOnOneLinkStayApart() throws IOException {
        String src = "Dirk ..works at.. SAP ,, first note ;; second note ;; third note\n";
        ICjDocument cj1 = parse(src);
        ICjEdge edge = cj1.graphs().findFirst().orElseThrow().edges().findFirst().orElseThrow();
        IJsonValue text = edge.data().jsonValue().asObject().get(DDotOutput.LINK_TEXT_KEY);
        assertNotNull(text);
        assertTrue(text.isArray(), () -> "three notes are three entries, not one joined value: " + text);
        assertEquals(3, text.asArray().size());
        // …and they are written back as three entries on one line, which the joined form could not do:
        // it produced a single `!!block` whose three lines read back in as ONE note.
        String ddot2 = new DDotOutput(cj1).toDDot();
        assertTrue(ddot2.contains(",, first note ;; second note ;; third note"), () -> ddot2);
        assertEquals(CjDocuments.toJsonString(cj1), CjDocuments.toJsonString(parse(ddot2)), () -> ddot2);
    }

    /**
     * A note whose own text holds a {@code ;;} cannot go inline any more than a typed value can: inline the
     * separator would cut it into two notes. The {@code ,,} block, where {@code ;;} is ordinary content, is
     * the form for it (corpus 28-semicolon-in-meta-block).
     */
    @Test
    void aNoteContainingSemicolonsUsesTheBlockForm() throws IOException {
        ICjDocument cj1 = parse("""
                Dirk ..works at.. SAP ,,
                one ;; two
                ,,
                """);
        String ddot2 = new DDotOutput(cj1).toDDot();
        assertTrue(ddot2.contains("\n,,\none ;; two\n,,\n"),
                () -> "a `;;`-bearing note must be written in a `,,` block:\n" + ddot2);
        assertEquals(CjDocuments.toJsonString(cj1), CjDocuments.toJsonString(parse(ddot2)), () -> ddot2);
    }

    /**
     * A typed value containing {@code ;;} must NOT go inline — the separator that joins pairs would cut it
     * in two and the reader would drop the line entirely. Inside a {@code ,,} block it is ordinary content
     * (corpus 28-semicolon-in-meta-block).
     */
    @Test
    void aValueContainingSemicolonsUsesTheBlockForm() throws IOException {
        ICjDocument cj1 = parse("""
                Dirk Hagemann ..works at.. SAP ,,
                ..note.. first ;; second
                ,,
                """);
        String ddot2 = new DDotOutput(cj1).toDDot();
        assertTrue(ddot2.contains("\n,,\n..note.. first ;; second\n,,\n"),
                () -> "a `;;`-bearing value must be written in a `,,` block:\n" + ddot2);
        assertEquals(CjDocuments.toJsonString(cj1), CjDocuments.toJsonString(parse(ddot2)), () -> ddot2);
    }

    /**
     * A multi-line SUBJECT is only expressible as a {@code !!block} filling the subject slot, with the
     * triple following as a continuation line (corpus 29-block-subject). Written raw it would decay into
     * stray prose plus a triple with the wrong subject.
     */
    @Test
    void aMultiLineSubjectRoundTripsThroughASubjectBlock() throws IOException {
        ICjDocument cj1 = parse("""
                !!block
                Alice
                Anderson

                ..knows.. Bob
                """);
        String ddot2 = new DDotOutput(cj1).toDDot();
        assertTrue(ddot2.startsWith("!!block\nAlice\nAnderson\n"),
                () -> "the subject must be re-emitted as a subject block:\n" + ddot2);
        assertEquals(CjDocuments.toJsonString(cj1), CjDocuments.toJsonString(parse(ddot2)), () -> ddot2);
    }

    /**
     * A metadata value spanning lines is only expressible as a {@code !!block} meta object
     * (corpus 30-block-meta-object); as plain block lines every line but the first would decay to meta text.
     */
    @Test
    void aMultiLineMetaValueRoundTripsThroughABlock() throws IOException {
        ICjDocument cj1 = parse("""
                Dirk ..works at.. SAP ,, ..note.. !!block
                line one
                line two

                """);
        String ddot2 = new DDotOutput(cj1).toDDot();
        assertTrue(ddot2.contains(",, ..note.. " + DDotOutput.OBJECT_BLOCK + "\nline one\nline two\n"),
                () -> "a multi-line meta value must open a block:\n" + ddot2);
        assertEquals(CjDocuments.toJsonString(cj1), CjDocuments.toJsonString(parse(ddot2)), () -> ddot2);
    }
}

package com.graphinout.reader.ddot;

import com.graphinout.base.cj.document.ICjDocument;
import com.graphinout.base.cj.document.ICjEdge;
import com.graphinout.base.input.SingleInputSource;
import com.graphinout.foundation.pure.json.document.IJsonArray;
import com.graphinout.foundation.pure.json.document.IJsonObject;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Per-link metadata (the {@code ,,} syntax, see https://ddot.it) must survive ddot &rarr; CJ as edge data:
 * structured {@code ..key.. value} pairs under {@code ddot-it:props}, free text under {@code ddot-it:text}.
 *
 * <p>These read the metadata cases straight from the canonical ddot.it corpus. They used to read a
 * <em>mirror</em> of it in graph-test-data, resolved off the classpath, and skipped themselves with a bare
 * {@code assumeTrue(res != null)} when that checkout was absent — so on any machine without it the
 * assertions vanished behind nothing louder than a "skipped" tick. Going to the source removes both the
 * mirror as a dependency and the silent skip: {@link DdotCorpus} skips with a banner and fails outright
 * when the corpus is present but incomplete.
 */
class DDotLinkMetadataTest {

    private static ICjEdge firstEdge(String caseName) throws IOException {
        ICjDocument doc = DDotReader.parseDDotToCjDocument(
                SingleInputSource.of(caseName + "/input.ddot", DdotCorpus.input(caseName)));
        assertNotNull(doc);
        return doc.graphs().findFirst().orElseThrow().edges().findFirst().orElseThrow();
    }

    /** Structured properties live under {@code ddot-it:props}. */
    private static IJsonObject props(ICjEdge edge) {
        assertNotNull(edge.data().jsonValue(), "edge should carry metadata as data");
        return edge.data().jsonValue().asObject().get(DDotOutput.LINK_PROPS_KEY).asObject();
    }

    /** Free-text notes live under {@code ddot-it:text}. */
    private static String text(ICjEdge edge) {
        assertNotNull(edge.data().jsonValue(), "edge should carry metadata as data");
        return edge.data().jsonValue().asObject().get(DDotOutput.LINK_TEXT_KEY).asString();
    }

    @Test
    void inlineStructuredMetaUnderProps() throws IOException {
        // John Doe ..leads.. Project Eagle ,, ..since.. 2025
        ICjEdge edge = firstEdge("03-inline-meta");
        assertEquals("leads", edge.edgeType().type());
        assertEquals("2025", props(edge).get("since").asString());
    }

    @Test
    void multilineStructuredMetaUnderProps() throws IOException {
        // Dirk Hagemann ..works at.. SAP ,,  /  ..year.. 2010  /  ..fictive.. yes  /  ,,
        ICjEdge edge = firstEdge("04-multiline-meta");
        assertEquals("works at", edge.edgeType().type());
        IJsonObject props = props(edge);
        assertEquals("2010", props.get("year").asString());
        assertEquals("yes", props.get("fictive").asString());
    }

    @Test
    void inlineFreeTextMetaUnderText() throws IOException {
        // John Doe ..leads.. Project Eagle ,, a random note
        assertEquals("a random note", text(firstEdge("13-inline-meta-text")));
    }

    @Test
    void multilineFreeTextMetaUnderText() throws IOException {
        // Dirk Hagemann ..works at.. SAP ,,  /  a random note  /  ,,
        assertEquals("a random note", text(firstEdge("14-multiline-meta-text")));
    }

    /**
     * A whole meta text block is ONE entry, joined by newlines — not one entry per line
     * (corpus 27-multiline-meta-text-lines).
     */
    @Test
    void multilineFreeTextIsOneEntry() throws IOException {
        assertEquals("a random note\nspanning two lines", text(firstEdge("27-multiline-meta-text-lines")));
    }

    /** Further inline pairs are separated by {@code ;;} (corpus 22-inline-meta-separator). */
    @Test
    void semicolonSeparatedPairsBothSurvive() throws IOException {
        IJsonObject props = props(firstEdge("22-inline-meta-separator"));
        assertEquals("2025", props.get("since").asString());
        assertEquals("2027", props.get("until").asString());
    }

    private static ICjEdge firstEdgeOf(String ddot) throws IOException {
        ICjDocument doc = DDotReader.parseDDotToCjDocument(SingleInputSource.of("in.ddot", ddot));
        return doc.graphs().findFirst().orElseThrow().edges().findFirst().orElseThrow();
    }

    /**
     * Nothing makes a link's relation unique: {@code ,, ..author.. Ann ;; ..author.. Bob} is TWO pairs. A JSON
     * object cannot hold {@code author} twice, so both values land in an array under the one key — dropping
     * either of them (which a {@code Map<String,String>} anywhere in the chain does) loses data the source
     * spelled out.
     */
    @Test
    void aRepeatedRelationKeepsEveryValue() throws IOException {
        IJsonObject props = props(firstEdgeOf("Ann ..wrote.. Book ,, ..author.. Ann ;; ..author.. Bob\n"));

        IJsonArray authors = props.get("author").asArray();
        assertEquals(2, authors.size(), "both ..author.. pairs must survive");
        assertEquals("Ann", authors.get(0).asString());
        assertEquals("Bob", authors.get(1).asString());
    }

    /** …and the write-back spells them out again as two {@code ;;}-separated pairs, so they survive a round trip. */
    @Test
    void aRepeatedRelationRoundTrips() throws IOException {
        String ddot = "Ann ..wrote.. Book ,, ..author.. Ann ;; ..author.. Bob\n";
        ICjDocument cj1 = DDotReader.parseDDotToCjDocument(SingleInputSource.of("in.ddot", ddot));

        String written = new DDotOutput(cj1).toDDot();
        assertTrue(written.contains(",, ..author.. Ann ;; ..author.. Bob"),
                "both values must be written back, `;;`-separated, but got:\n" + written);

        IJsonArray authors = props(firstEdgeOf(written)).get("author").asArray();
        assertEquals(2, authors.size());
        assertEquals("Bob", authors.get(1).asString());
    }

    /** A relation written once stays a plain string — the array form is only for the repeated case. */
    @Test
    void aSingleValuedRelationStaysAScalar() throws IOException {
        IJsonObject props = props(firstEdgeOf("Ann ..wrote.. Book ,, ..author.. Ann\n"));
        assertEquals("Ann", props.get("author").asString());
    }
}

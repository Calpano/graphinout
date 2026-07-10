package com.graphinout.reader.grale;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import com.graphinout.base.cj.document.ICjDocument;
import com.graphinout.base.cj.document.ICjEdge;
import com.graphinout.base.cj.document.ICjLabel;
import com.graphinout.base.cj.document.ICjLabelEntry;
import com.graphinout.base.cj.document.ICjNode;
import com.graphinout.base.input.SingleInputSource;
import com.graphinout.base.output.InMemoryOutputSink;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GraleNodeSizeTest {

    private final ObjectMapper mapper = new ObjectMapper();

    // -------------------------------------------------------------- wrapping

    @Test
    void wrapsOnBrTags() {
        assertEquals(List.of("Line one", "Line two"),
                RobotoLabelMetrics.wrap("Line one<br>Line two", 50));
        assertEquals(List.of("a", "b"), RobotoLabelMetrics.wrap("a<br/>b", 50));
        assertEquals(List.of("a", "b"), RobotoLabelMetrics.wrap("a<BR />b", 50));
    }

    @Test
    void keepsExplicitBlankLineBetweenBrs() {
        assertEquals(List.of("a", "", "b"), RobotoLabelMetrics.wrap("a<br><br>b", 50));
    }

    @Test
    void wordWrapsAtMaxLineLength() {
        String label = "the quick brown fox jumps over the lazy dog and then keeps on running";
        List<String> lines = RobotoLabelMetrics.wrap(label, 50);
        assertTrue(lines.size() >= 2, "should wrap into multiple lines");
        for (String line : lines) {
            assertTrue(line.length() <= 50, "line exceeds max length: '" + line + "'");
        }
    }

    @Test
    void longSingleWordIsNotHardSplit() {
        String word = "x".repeat(80);
        assertEquals(List.of(word), RobotoLabelMetrics.wrap(word, 50));
    }

    // -------------------------------------------------------------- estimation

    @Test
    void widerTextGivesWiderBox() {
        assertTrue(RobotoLabelMetrics.estimate("WWWWWWWW").width()
                > RobotoLabelMetrics.estimate("iiii").width());
    }

    @Test
    void moreLinesGiveTallerBox() {
        int oneLine = RobotoLabelMetrics.estimate("Process").height();
        int twoLines = RobotoLabelMetrics.estimate("Process<br>step").height();
        assertTrue(twoLines > oneLine, "two-line label should be taller (" + twoLines + " > " + oneLine + ")");
    }

    @Test
    void emptyLabelStillHasPositiveBox() {
        RobotoLabelMetrics.Box box = RobotoLabelMetrics.estimate("");
        assertTrue(box.width() > 0 && box.height() > 0);
    }

    @Test
    void tinyLabelIsClampedToMinimumBox() {
        // a 1-char label would be narrower than the floor; it must be clamped up
        RobotoLabelMetrics.Box box = RobotoLabelMetrics.estimate("i");
        int minWidth = (int) Math.round(RobotoLabelMetrics.MIN_WIDTH_EM * RobotoLabelMetrics.DEFAULT_FONT_SIZE);
        int minHeight = (int) Math.round(RobotoLabelMetrics.MIN_HEIGHT_EM * RobotoLabelMetrics.DEFAULT_FONT_SIZE);
        assertTrue(box.width() >= minWidth, "width floored to " + minWidth + ": " + box.width());
        assertTrue(box.height() >= minHeight, "height floored to " + minHeight + ": " + box.height());
    }

    @Test
    void paddingScalesWithFontSize() {
        // ½-em padding on each side means the box grows with the font even for the same text
        RobotoLabelMetrics.Box small = RobotoLabelMetrics.estimate("Process step", 16.0, 50);
        RobotoLabelMetrics.Box big = RobotoLabelMetrics.estimate("Process step", 32.0, 50);
        assertTrue(big.width() > small.width() && big.height() > small.height());
    }

    // -------------------------------------------------------------- writer wiring

    private JsonNode firstNodeValue(String graleInput) throws Exception {
        GraleReader reader = new GraleReader();
        try (SingleInputSource in = SingleInputSource.of("in.grale.json", graleInput)) {
            ICjDocument doc = reader.readToCjDocument(in);
            InMemoryOutputSink sink = new InMemoryOutputSink();
            reader.writeCjDocument(doc, sink);
            JsonNode out = mapper.readTree(sink.getBufferAsUtf8String());
            return out.get("nodes").get(0).get("value");
        }
    }

    /** Read grale into CJ and return the first node of the first graph. */
    private ICjNode firstCjNode(String graleInput) throws Exception {
        GraleReader reader = new GraleReader();
        try (SingleInputSource in = SingleInputSource.of("in.grale.json", graleInput)) {
            ICjDocument doc = reader.readToCjDocument(in);
            return doc.graphs().findFirst().orElseThrow()
                    .nodes().findFirst().orElseThrow();
        }
    }

    /** Read grale into CJ and return the first edge of the first graph. */
    private ICjEdge firstCjEdge(String graleInput) throws Exception {
        GraleReader reader = new GraleReader();
        try (SingleInputSource in = SingleInputSource.of("in.grale.json", graleInput)) {
            ICjDocument doc = reader.readToCjDocument(in);
            return doc.graphs().findFirst().orElseThrow()
                    .edges().findFirst().orElseThrow();
        }
    }

    private static String labelOf(ICjLabel label) {
        return label == null ? null : label.entries()
                .map(ICjLabelEntry::value).filter(s -> s != null && !s.isEmpty()).findFirst().orElse(null);
    }

    @Test
    void graleToCjPutsLabelInCjLabelAndSizeInDataNotMeta() throws Exception {
        String input = "{ \"options\": {\"directed\": true, \"multigraph\": false, \"compound\": false},"
                + " \"nodes\": [ { \"v\": \"a\", \"value\": { \"width\": 80, \"height\": 40,"
                + " \"meta\": { \"label\": \"Start\", \"fill\": \"#dcfce7\" } } } ], \"edges\": [] }";
        ICjNode node = firstCjNode(input);

        // label is carried by the CJ node's native label, not by data
        assertEquals("Start", labelOf(node.label()), "label should be the CJ node label");

        JsonNode data = mapper.readTree(node.data().jsonValue().toJsonString());
        assertEquals(80, data.path("size").path("width").asInt(), "width folded into data.size");
        assertEquals(40, data.path("size").path("height").asInt(), "height folded into data.size");
        assertFalse(data.has("width"), "width must not stay at data top level");
        assertFalse(data.path("meta").has("label"), "label must not be duplicated in data.meta");
        assertEquals("#dcfce7", data.path("meta").path("fill").asText(), "other meta fields are kept");
    }

    @Test
    void graleToCjPutsEdgeLabelInCjLabelNotData() throws Exception {
        String input = "{ \"options\": {\"directed\": true, \"multigraph\": false, \"compound\": false},"
                + " \"nodes\": [ { \"v\": \"a\" }, { \"v\": \"b\" } ],"
                + " \"edges\": [ { \"v\": \"a\", \"w\": \"b\", \"value\": { \"weight\": 2,"
                + " \"meta\": { \"label\": \"next\", \"color\": \"#ea580c\" } } } ] }";
        ICjEdge edge = firstCjEdge(input);

        // label is carried by the CJ edge's native label, not by data
        assertEquals("next", labelOf(edge.label()), "label should be the CJ edge label");

        JsonNode data = mapper.readTree(edge.data().jsonValue().toJsonString());
        assertEquals(2, data.path("weight").asInt(), "non-label edge data is kept");
        assertFalse(data.path("meta").has("label"), "label must not be duplicated in data.meta");
        assertEquals("#ea580c", data.path("meta").path("color").asText(), "other meta fields are kept");
    }

    @Test
    void estimatesSizeWhenLabelPresentButSizeMissing() throws Exception {
        String input = "{ \"options\": {\"directed\": true, \"multigraph\": false, \"compound\": false},"
                + " \"nodes\": [ { \"v\": \"a\", \"value\": { \"meta\": { \"label\": \"Process step\" } } } ],"
                + " \"edges\": [] }";
        JsonNode value = firstNodeValue(input);
        assertTrue(value.has("width"), "width should be estimated");
        assertTrue(value.has("height"), "height should be estimated");
        assertTrue(value.get("width").asInt() > 0 && value.get("height").asInt() > 0);
    }

    @Test
    void brLabelProducesTallerBoxThanSingleLine() throws Exception {
        String single = "{ \"options\": {\"directed\": true, \"multigraph\": false, \"compound\": false},"
                + " \"nodes\": [ { \"v\": \"a\", \"value\": { \"meta\": { \"label\": \"one two\" } } } ], \"edges\": [] }";
        String multi = "{ \"options\": {\"directed\": true, \"multigraph\": false, \"compound\": false},"
                + " \"nodes\": [ { \"v\": \"a\", \"value\": { \"meta\": { \"label\": \"one<br>two\" } } } ], \"edges\": [] }";
        assertTrue(firstNodeValue(multi).get("height").asInt() > firstNodeValue(single).get("height").asInt());
    }

    @Test
    void doesNotOverrideAuthorProvidedSize() throws Exception {
        String input = "{ \"options\": {\"directed\": true, \"multigraph\": false, \"compound\": false},"
                + " \"nodes\": [ { \"v\": \"a\", \"value\": { \"width\": 123, \"height\": 45,"
                + " \"meta\": { \"label\": \"Process step\" } } } ], \"edges\": [] }";
        JsonNode value = firstNodeValue(input);
        assertEquals(123, value.get("width").asInt());
        assertEquals(45, value.get("height").asInt());
    }

    @Test
    void leavesUnlabeledNodeWithoutSize() throws Exception {
        String input = "{ \"options\": {\"directed\": true, \"multigraph\": false, \"compound\": false},"
                + " \"nodes\": [ { \"v\": \"a\" } ], \"edges\": [] }";
        JsonNode node = mapper.readTree(writeBack(input)).get("nodes").get(0);
        // no label, no size in -> no value/size synthesised
        assertFalse(node.has("value") && node.get("value").has("width"),
                "unlabeled node should not gain an estimated size");
    }

    private String writeBack(String graleInput) throws Exception {
        GraleReader reader = new GraleReader();
        try (SingleInputSource in = SingleInputSource.of("in.grale.json", graleInput)) {
            ICjDocument doc = reader.readToCjDocument(in);
            InMemoryOutputSink sink = new InMemoryOutputSink();
            reader.writeCjDocument(doc, sink);
            return sink.getBufferAsUtf8String();
        }
    }
}

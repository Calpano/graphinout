package com.graphinout.reader.grale;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.graphinout.base.cj.document.ICjDocument;
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

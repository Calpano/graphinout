package com.graphinout.reader.graphml;

import com.graphinout.base.cj.analyze.CjAnalysis;
import com.graphinout.base.cj.analyze.CjAnalyzer;
import com.graphinout.base.cj.document.ICjDocument;
import com.graphinout.base.input.SingleInputSource;
import com.graphinout.foundation.pure.input.ContentError;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.google.common.truth.Truth.assertThat;

/** GraphML with HTML named entities (e.g. {@code &Eacute;}) is auto-corrected by the streaming decoder, with a warning. */
class GraphmlHtmlEntityTest {

    private static final String GRAPHML_WITH_HTML_ENTITY = """
            <?xml version='1.0' encoding='utf-8'?>
            <graphml xmlns="http://graphml.graphdrawing.org/xmlns">
              <key id="d0" attr.name="country" attr.type="string" for="node"/>
              <graph edgedefault="undirected">
                <node id="n1"><data key="d0">M&Eacute;XICO</data></node>
                <node id="n2"><data key="d0">caf&eacute;</data></node>
                <edge source="n1" target="n2"/>
              </graph>
            </graphml>
            """;

    @Test
    void parsesHtmlEntityGraphmlAndWarns() throws IOException {
        List<ContentError> errors = new ArrayList<>();
        GraphmlReader reader = new GraphmlReader();
        reader.setContentErrorHandler(errors::add);

        ICjDocument doc = reader.readToCjDocument(SingleInputSource.of("entities.graphml", GRAPHML_WITH_HTML_ENTITY));
        CjAnalysis a = CjAnalyzer.analyze(doc);

        // the undeclared &Eacute; / &eacute; no longer abort the parse
        assertThat(a.nodeCount()).isEqualTo(2);
        assertThat(a.edgeCount()).isEqualTo(1);

        // ...and the auto-correction is surfaced as a single Warn-level error naming the entities
        List<ContentError> warnings = errors.stream()
                .filter(e -> e.getLevel() == ContentError.ErrorLevel.Warn)
                .filter(e -> e.getMessage().contains("Auto-corrected"))
                .toList();
        assertThat(warnings).hasSize(1);
        assertThat(warnings.get(0).getMessage()).contains("&Eacute;");
        assertThat(warnings.get(0).getMessage()).contains("&eacute;");
    }
}

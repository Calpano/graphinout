package com.graphinout.engine;

import com.graphinout.base.cj.document.ICjDocument;
import com.graphinout.base.cj.document.ICjEdge;
import com.graphinout.base.gio.GioFileFormat;
import com.graphinout.base.gio.GioInputAnalysis;
import com.graphinout.base.gio.GioInputAnalyzer;
import com.graphinout.base.gio.GioService;
import com.graphinout.base.input.SingleInputSource;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Map;

import static com.google.common.truth.Truth.assertThat;

/** PG-JSON is reachable through the engine: registered, readable by id, and detected. */
class PgJsonEngineTest {

    private static final String PG_JSON = """
            {"nodes":[{"id":"101","labels":["person"],"properties":{"name":["Alice"]}},
                      {"id":"102","labels":["person"],"properties":{"name":["Bob"]}}],
             "edges":[{"from":"101","to":"102","labels":["likes"],"properties":{"since":[2015]}}]}""";

    private final GioEngineCore engine = new GioEngineCore();

    @Test
    void registeredForReadAndWrite() {
        Map<String, GioService.FormatSupport> byId = new java.util.HashMap<>();
        engine.formats().forEach((format, support) -> byId.put(format.id(), support));
        assertThat(byId).containsEntry("pg-json", GioService.FormatSupport.ReadAndWrite);
    }

    @Test
    void readByFormatId() throws IOException {
        ICjDocument doc = engine.getReader("pg-json").readToCjDocument(SingleInputSource.of("graph.pg.json", PG_JSON));
        ICjEdge edge = doc.graphs().findFirst().orElseThrow().edges().findFirst().orElseThrow();
        assertThat(edge.source().node()).isEqualTo("101");
        assertThat(edge.target().node()).isEqualTo("102");
    }

    @Test
    void detectedByFileName() throws IOException {
        GioInputAnalysis analysis = new GioInputAnalyzer(engine.readers()).analyze(SingleInputSource.of("graph.pg.json", PG_JSON));
        assertThat(analysis.best().map(c -> c.format().id())).hasValue("pg-json");
    }

    @Test
    void detectedByContentAlone() throws IOException {
        GioInputAnalysis analysis = new GioInputAnalyzer(engine.readers()).analyze(SingleInputSource.of("unknown.json", PG_JSON));
        assertThat(analysis.best().map(c -> c.format().id())).hasValue("pg-json");
    }

    @Test
    void formatMatchesItsExtensions() {
        GioFileFormat format = engine.getReader("pg-json").fileFormat();
        assertThat(format.matches("x/graph.pg.json")).isTrue();
        assertThat(format.matches("graph.pgjson")).isTrue();
    }
}

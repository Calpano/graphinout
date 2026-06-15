package com.graphinout.reader.gexf;

import com.graphinout.base.cj.stream.CjStream2CjWriter;
import com.graphinout.base.cj.writer.Cj2JsonWriter;
import com.graphinout.base.input.InputSource;
import com.graphinout.base.input.SingleInputSource;
import com.graphinout.foundation.pure.input.ContentError;
import com.graphinout.foundation.pure.json.writer.impl.Json2StringWriter;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static com.google.common.truth.Truth.assertThat;

class GexfReaderTest {

    @Test
    void read() throws Exception {
        String content = IOUtils.resourceToString("/xml/gexf/sample.gexf", StandardCharsets.UTF_8);
        InputSource inputSource = SingleInputSource.of("xml/gexf/sample.gexf", content);

        GexfReader reader = new GexfReader();
        Json2StringWriter json2StringWriter = new Json2StringWriter();
        Cj2JsonWriter cj2JsonWriter = new Cj2JsonWriter(json2StringWriter);
        CjStream2CjWriter stream = new CjStream2CjWriter(cj2JsonWriter, true);
        reader.read(inputSource, stream);
        String json = json2StringWriter.jsonString();
        // two labelled nodes and one directed edge (GEXF defaultedgetype="directed" -> endpoint directions in/out)
        assertThat(json).isEqualTo("{\"$schema\":\"https://j-s-o-n.org/schema/cj-7.0.0.json\",\"connectedJson\":{\"versionDate\":\"2026-01-15\",\"versionNumber\":\"7.0.0\"},\"graphs\":[{\"nodes\":[{\"id\":\"0\",\"label\":{\"entries\":[{\"value\":\"Hello\"}]}},{\"id\":\"1\",\"label\":{\"entries\":[{\"value\":\"World\"}]}}],\"edges\":[{\"endpoints\":[{\"node\":\"0\",\"direction\":\"in\"},{\"node\":\"1\",\"direction\":\"out\"}]}]}]}");
    }

    @Test
    void readMalformed_reportsContentError() throws Exception {
        // an <edge> without source/target must be reported as a content error (issue #30 checklist)
        String malformed = """
                <?xml version="1.0" encoding="UTF-8"?>
                <gexf xmlns="http://www.gexf.net/1.2draft" version="1.2">
                    <graph defaultedgetype="directed">
                        <nodes>
                            <node id="0" label="Hello" />
                        </nodes>
                        <edges>
                            <edge target="0" />
                        </edges>
                    </graph>
                </gexf>
                """;
        InputSource inputSource = SingleInputSource.of("malformed.gexf", malformed);
        GexfReader reader = new GexfReader();
        List<ContentError> errors = new ArrayList<>();
        reader.setContentErrorHandler(errors::add);

        Json2StringWriter json2StringWriter = new Json2StringWriter();
        Cj2JsonWriter cj2JsonWriter = new Cj2JsonWriter(json2StringWriter);
        CjStream2CjWriter stream = new CjStream2CjWriter(cj2JsonWriter, true);
        reader.read(inputSource, stream);

        assertThat(errors).isNotEmpty();
        assertThat(errors.stream().anyMatch(e -> e.getLevel() == ContentError.ErrorLevel.Error)).isTrue();
    }
}

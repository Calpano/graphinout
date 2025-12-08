package com.graphinout.reader.gexf;

import com.graphinout.base.cj.stream.CjStream2CjWriter;
import com.graphinout.base.cj.writer.Cj2JsonWriter;
import com.graphinout.base.input.InputSource;
import com.graphinout.base.input.SingleInputSource;
import com.graphinout.foundation.pure.json.writer.impl.Json2StringWriter;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static com.google.common.truth.Truth.assertThat;

@Disabled("not ready yet")
class GexfReaderTest {

    @Test
    void read() throws Exception {
        String content = IOUtils.resourceToString("/xml/gexf/sample.gexf", StandardCharsets.UTF_8);
        InputSource inputSource = SingleInputSource.of("xml/gexf/sample.gexf", content);

        GexfReader reader = new GexfReader();
        Json2StringWriter json2StringWriter = new Json2StringWriter();
        Cj2JsonWriter cj2JsonWriter = new Cj2JsonWriter(json2StringWriter);
        CjStream2CjWriter stream = new CjStream2CjWriter(cj2JsonWriter);
        reader.read(inputSource, stream);
        String json = json2StringWriter.jsonString();
        assertThat(json).isEqualTo("{\"graphs\":[{\"nodes\":[{\"id\":\"0\",\"label\":[{\"value\":\"Hello\"}]},{\"id\":\"1\",\"label\":[{\"value\":\"World\"}]}],\"edges\":[{\"endpoints\":[{\"node\":\"0\"},{\"node\":\"1\"}]}]}]}");
    }
}

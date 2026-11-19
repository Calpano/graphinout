package com.graphinout.reader.graphml;

import com.graphinout.base.input.SingleInputSource;
import com.graphinout.base.xml.util.XmlTool;
import com.graphinout.reader.graphml.cj.Graphml2CjDocument;
import com.graphinout.reader.graphml.validation.GraphmlValidator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

/**
 * GraphML input is untrusted: no code path may resolve an external entity or load an external DTD (XXE).
 * <p>
 * The entity points to a local file holding an XML element. Had the entity been resolved, its element would show up
 * in the result (or make {@code <desc>} schema-invalid); pointing it to a missing file would throw
 * {@link java.io.FileNotFoundException}.
 */
class GraphmlXxeTest {

    static final String MARKER = "secretMarker42";

    @TempDir
    Path dir;

    String graphmlWithExternalEntity(String fileName) throws IOException {
        Path secret = dir.resolve("secret.txt");
        Files.writeString(secret, "<" + MARKER + "/>");
        String url = dir.resolve(fileName).toUri().toString();
        return """
                <?xml version="1.0"?>
                <!DOCTYPE graphml [ <!ENTITY x SYSTEM "%s"> ]>
                <graphml xmlns="http://graphml.graphdrawing.org/xmlns">
                  <graph id="G" edgedefault="directed"><desc>&x;</desc><node id="a"/></graph>
                </graphml>""".formatted(url);
    }

    @Test
    @DisplayName("GraphmlValidator does not resolve external entities")
    void validator() throws IOException {
        // resolved, the entity puts an element into <desc>, which the schema forbids
        assertThat(GraphmlValidator.isValidGraphml(SingleInputSource.of("x.graphml", graphmlWithExternalEntity("secret.txt")))).isTrue();
        assertDoesNotThrow(() -> GraphmlValidator.isValidGraphml(SingleInputSource.of("x.graphml", graphmlWithExternalEntity("missing.txt"))));
    }

    @Test
    @DisplayName("GraphmlReader.isValid (format detection) does not resolve external entities")
    void readerIsValid() throws IOException {
        String missing = graphmlWithExternalEntity("missing.txt");
        assertDoesNotThrow(() -> new GraphmlReader().isValid(SingleInputSource.of("x.graphml", missing)));
    }

    @Test
    @DisplayName("Reading GraphML does not resolve external entities")
    void read() throws Exception {
        Graphml2CjDocument graphml2Cj = new Graphml2CjDocument();
        XmlTool.parseAndWriteXml("x.graphml", graphmlWithExternalEntity("secret.txt"), new Xml2GraphmlWriter(graphml2Cj));
        assertThat(graphml2Cj.resultDoc().toCjJsonString()).doesNotContain(MARKER);
    }

}

package com.graphinout.reader.graphml;

import com.graphinout.base.cj.document.ICjDocument;
import com.graphinout.base.cj.document.ICjNode;
import com.graphinout.base.input.SingleInputSourceOfString;
import com.graphinout.reader.cj.ConnectedJsonReader;
import com.graphinout.reader.graphml.cj.CjDocument2Graphml;
import com.graphinout.foundation.pure.xml.writer.Xml2StringWriter;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;

import static com.google.common.truth.Truth.assertThat;

/**
 * Text in GraphML element content, checked in each direction on its own with the JDK's XML parser. A round trip alone
 * doesn't find a missing escape in the writer if the reader misses the matching decode.
 */
class GraphmlTextEscapingTest {

    private static final String TEXT = "A & <B> \"q\" 'a' &amp; ]]>";

    /** value as JSON string literal */
    private static final String TEXT_JSON = "\"A & <B> \\\"q\\\" 'a' &amp; ]]>\"";

    private static final String TEXT_XML = "A &amp; &lt;B> \"q\" 'a' &amp;amp; ]]&gt;";

    private static String cj(String nodeProperties) {
        return "{\"graphs\":[{\"nodes\":[{\"id\":\"a\"," + nodeProperties + "}]}]}";
    }

    private static ICjDocument readCj(String cj) throws Exception {
        return new ConnectedJsonReader().readToCjDocument(new SingleInputSourceOfString("in.cj.json", cj));
    }

    private static ICjNode readGraphmlNode(String graphml) throws Exception {
        return new GraphmlReader().readToCjDocument(new SingleInputSourceOfString("in.graphml", graphml))
                .graphs().findFirst().orElseThrow().nodes().findFirst().orElseThrow();
    }

    private static String graphml(String key, String nodeContent) {
        return "<graphml xmlns=\"http://graphml.graphdrawing.org/xmlns\">" + key
                + "<graph edgedefault=\"directed\"><node id=\"a\">" + nodeContent + "</node></graph></graphml>";
    }

    private static String writeGraphml(ICjDocument cjDoc) throws Exception {
        Xml2StringWriter xml = new Xml2StringWriter();
        new CjDocument2Graphml(new Graphml2XmlWriter(xml)).writeDocumentToGraphml(cjDoc);
        return xml.resultString();
    }

    /** text content of the first such element within the node */
    private static String nodeText(String graphml, String tagName) throws Exception {
        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new InputSource(new StringReader(graphml)));
        NodeList found = ((Element) doc.getElementsByTagName("node").item(0)).getElementsByTagName(tagName);
        assertThat(found.getLength()).isEqualTo(1);
        return found.item(0).getTextContent();
    }

    private static String label(ICjNode node) {
        return node.label().entries().findFirst().orElseThrow().value();
    }

    @Test
    void labelIsWrittenEncoded() throws Exception {
        String graphml = writeGraphml(readCj(cj("\"label\":{\"entries\":[{\"value\":" + TEXT_JSON + "}]}")));
        assertThat(nodeText(graphml, "data")).isEqualTo(TEXT);
        assertThat(label(readGraphmlNode(graphml))).isEqualTo(TEXT);
    }

    @Test
    void multiLanguageLabelIsWrittenEncoded() throws Exception {
        String graphml = writeGraphml(readCj(cj("\"label\":{\"entries\":[{\"language\":\"de\",\"value\":" + TEXT_JSON + "},{\"language\":\"en\",\"value\":\"x\"}]}")));
        assertThat(nodeText(graphml, "data")).contains("\"language\"");
        ICjNode node = readGraphmlNode(graphml);
        assertThat(node.label().entries().count()).isEqualTo(2);
        assertThat(label(node)).isEqualTo(TEXT);
    }

    @Test
    void stringDataIsWrittenEncoded() throws Exception {
        String graphml = writeGraphml(readCj(cj("\"data\":{\"foo\":" + TEXT_JSON + "}")));
        assertThat(nodeText(graphml, "data")).isEqualTo(TEXT);
    }

    @Test
    void jsonDataIsWrittenEncoded() throws Exception {
        // nested, so it can't be one GraphML <data> per property
        String graphml = writeGraphml(readCj(cj("\"data\":{\"foo\":{\"bar\":" + TEXT_JSON + "}}")));
        assertThat(nodeText(graphml, "data")).isEqualTo("{\"foo\":{\"bar\":" + TEXT_JSON + "}}");
        assertThat(readGraphmlNode(graphml).jsonValue().asObject().get("foo").asObject().get("bar").asString()).isEqualTo(TEXT);
    }

    @Test
    void descriptionIsWrittenEncoded() throws Exception {
        String graphml = writeGraphml(readCj(cj("\"data\":{\"graphml:description\":" + TEXT_JSON + "}")));
        assertThat(nodeText(graphml, "desc")).isEqualTo(TEXT);
    }

    @Test
    void labelIsReadDecoded() throws Exception {
        String key = "<key id=\"cj_label\" for=\"all\" attr.name=\"cj_label\" attr.type=\"string\"/>";
        assertThat(label(readGraphmlNode(graphml(key, "<data key=\"cj_label\">" + TEXT_XML + "</data>")))).isEqualTo(TEXT);
        assertThat(label(readGraphmlNode(graphml(key, "<data key=\"cj_label\"><![CDATA[A & <B>]]></data>")))).isEqualTo("A & <B>");
    }

    @Test
    void nodeDefaultIsNotEncodedTwice() throws Exception {
        String key = "<key id=\"k\" for=\"node\" attr.name=\"foo\" attr.type=\"string\"><default>A &amp; B</default></key>";
        String in = graphml(key, "");
        assertThat(readGraphmlNode(in).jsonValue().asObject().get("foo").asString()).isEqualTo("A & B");
        String out = writeGraphml(new GraphmlReader().readToCjDocument(new SingleInputSourceOfString("in.graphml", in)));
        assertThat(out).doesNotContain("&amp;amp;");
        assertThat(readGraphmlNode(out).jsonValue().asObject().get("foo").asString()).isEqualTo("A & B");
    }

    @Test
    void keyDescriptionIsNotEncodedTwice() throws Exception {
        String key = "<key id=\"k\" for=\"node\" attr.name=\"foo\" attr.type=\"string\"><desc>A &amp; B</desc></key>";
        String out = writeGraphml(new GraphmlReader().readToCjDocument(new SingleInputSourceOfString("in.graphml", graphml(key, "<data key=\"k\">v</data>"))));
        assertThat(out).contains("<desc>A &amp; B</desc>");
    }

}

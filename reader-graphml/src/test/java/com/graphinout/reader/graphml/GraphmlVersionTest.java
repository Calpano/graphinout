package com.graphinout.reader.graphml;

import com.graphinout.base.cj.data.CjDataProperty;
import com.graphinout.base.cj.document.impl.CjDocumentElement;
import com.graphinout.base.input.SingleInputSource;
import com.graphinout.base.xml.util.XmlTool;
import com.graphinout.foundation.pure.xml.writer.Xml2StringWriter;
import com.graphinout.reader.graphml.cj.CjDocument2Graphml;
import com.graphinout.reader.graphml.cj.Graphml2CjDocument;
import com.graphinout.reader.graphml.validation.GraphmlValidator;
import com.graphinout.testdata.TestFileUtil;
import io.github.classgraph.Resource;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static com.google.common.truth.Truth.assertThat;

/**
 * GraphML 1.0 and 1.1 (issue #92): we read both and always write 1.1.
 * <p>
 * The 1.1 schema differs from 1.0 only in {@code for="graphml"} and {@code dynamic} on {@code <key>}, and
 * {@code time} on {@code <data>} (plus {@code @time} in the data-key uniqueness constraints).
 * <p>
 * These tests check the raw writer output. The corpus round-trip test ({@link Graphml2CjAndBackTest}) compares through
 * {@link GraphmlNormalizer}, which upgrades 1.0 to 1.1 on both sides, so it cannot tell which version is written.
 */
class GraphmlVersionTest {

    static final String SCHEMA_LOCATION_1_0 = "http://graphml.graphdrawing.org/xmlns/1.0/graphml.xsd";
    static final String SCHEMA_LOCATION_1_1 = "http://graphml.graphdrawing.org/xmlns/1.1/graphml.xsd";

    /** GraphML 1.0 input from the corpus. */
    static final String FIXTURE_1_0 = "xml/graphml/synthetic/graphml-key-data.graphml.xml";
    /** GraphML 1.1 input from the corpus, using for="graphml", dynamic and time. */
    static final String FIXTURE_1_1 = "xml/graphml/synthetic/graphml-1.1.graphml.xml";

    /** GraphML 1.1 with time-stamped values (#158). */
    static final String TIME_VALUES_1_1 = """
            <?xml version="1.0" encoding="UTF-8"?>
            <graphml xmlns="http://graphml.graphdrawing.org/xmlns" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
              xsi:schemaLocation="http://graphml.graphdrawing.org/xmlns http://graphml.graphdrawing.org/xmlns/1.1/graphml.xsd">
              <key id="k" for="node" attr.name="w" attr.type="int" dynamic="true"/>
              <graph id="G" edgedefault="directed">
                <node id="a"><data key="k" time="1">1</data><data key="k" time="2">5</data></node>
              </graph>
            </graphml>""";

    record Roundtrip(CjDocumentElement cj, String graphml) {}

    static String fixture(String path) throws IOException {
        Resource resource = TestFileUtil.resource(path);
        assertThat(resource).isNotNull();
        return resource.getContentAsString();
    }

    static boolean isValid(String graphml) throws IOException {
        return GraphmlValidator.isValidGraphml(SingleInputSource.of("test.graphml", graphml));
    }

    /** GraphML -> CJ -> GraphML, returning the CJ and the raw (not normalized) GraphML output. */
    static Roundtrip roundtrip(String graphml) throws Exception {
        Graphml2CjDocument graphml2Cj = new Graphml2CjDocument();
        XmlTool.parseAndWriteXml("test.graphml", graphml, new Xml2GraphmlWriter(graphml2Cj));
        CjDocumentElement cj = graphml2Cj.resultDoc();
        Xml2StringWriter xml = new Xml2StringWriter();
        new CjDocument2Graphml(new Graphml2XmlWriter(xml)).writeDocumentToGraphml(cj);
        return new Roundtrip(cj, xml.resultString());
    }

    @Test
    void fixturesDeclareTheirVersion() throws IOException {
        assertThat(fixture(FIXTURE_1_0)).contains(SCHEMA_LOCATION_1_0);
        assertThat(fixture(FIXTURE_1_1)).contains(SCHEMA_LOCATION_1_1);
    }

    @Test
    void bothVersionsValidate() throws IOException {
        assertThat(isValid(fixture(FIXTURE_1_0))).isTrue();
        assertThat(isValid(fixture(FIXTURE_1_1))).isTrue();
        assertThat(isValid(TIME_VALUES_1_1)).isTrue();
    }

    @Test
    void graphml10IsWrittenAs11() throws Exception {
        String out = roundtrip(fixture(FIXTURE_1_0)).graphml();
        assertThat(out).contains(SCHEMA_LOCATION_1_1);
        assertThat(out).doesNotContain(SCHEMA_LOCATION_1_0);
        assertThat(isValid(out)).isTrue();
    }

    @Test
    void graphml11IsWrittenAs11() throws Exception {
        String out = roundtrip(fixture(FIXTURE_1_1)).graphml();
        assertThat(out).contains(SCHEMA_LOCATION_1_1);
        assertThat(isValid(out)).isTrue();
    }

    @Test
    void missingSchemaLocationIsWrittenAs11() throws Exception {
        String out = roundtrip("""
                <graphml xmlns="http://graphml.graphdrawing.org/xmlns">
                  <graph id="G" edgedefault="undirected"><node id="a"/></graph>
                </graphml>""").graphml();
        assertThat(out).contains(SCHEMA_LOCATION_1_1);
    }

    @Test
    void keyForGraphmlBecomesDocumentData() throws Exception {
        Roundtrip rt = roundtrip(fixture(FIXTURE_1_1));
        assertThat(rt.cj().toCjJsonString()).contains("\"author\":\"graphinout\"");
        assertThat(rt.graphml()).contains("for=\"graphml\"");
        assertThat(rt.graphml()).containsMatch("<data key=\"author\">graphinout</data>");
    }

    @Test
    void fixtureTimeValuesSurviveRoundtrip() throws Exception {
        String out = roundtrip(fixture(FIXTURE_1_1)).graphml();
        assertThat(out).containsMatch(data("weight", "1", "1"));
        assertThat(out).containsMatch(data("weight", "2", "3"));
    }

    @Test
    void dynamicOnKeySurvivesRoundtrip() throws Exception {
        Roundtrip rt = roundtrip(fixture(FIXTURE_1_1));
        assertThat(rt.graphml()).containsMatch("<key [^>]*dynamic=\"true\"[^>]*id=\"weight\"");
        assertThat(rt.graphml()).containsMatch("<key [^>]*dynamic=\"false\"[^>]*id=\"label\"");
    }

    /** Matches a {@code <data>} with the given key, time and value, whatever the attribute order. */
    static String data(String key, String time, String value) {
        return "<data (?=[^>]*key=\"" + key + "\")(?=[^>]*time=\"" + time + "\")[^>]*>" + value + "</data>";
    }

    @Test
    void timeValuesAreATimeSeriesInCj() throws Exception {
        String cj = roundtrip(TIME_VALUES_1_1).cj().toCjJsonString();
        assertThat(cj).contains("\"w\":[{\"time\":1,\"value\":1},{\"time\":2,\"value\":5}]");
    }

    @Test
    void timeValuesSurviveRoundtrip() throws Exception {
        String out = roundtrip(TIME_VALUES_1_1).graphml();
        assertThat(out).containsMatch(data("k", "1", "1"));
        assertThat(out).containsMatch(data("k", "2", "5"));
        // under the original typed key, not as a JSON string
        assertThat(out).containsMatch("<key (?=[^>]*id=\"k\")(?=[^>]*attr.type=\"int\")(?=[^>]*dynamic=\"true\")");
        assertThat(out).doesNotContain("<data key=\"cj_jsonData\"");
        assertThat(isValid(out)).isTrue();
    }

    @Test
    void timeValuesRoundtripTwiceUnchanged() throws Exception {
        Roundtrip first = roundtrip(TIME_VALUES_1_1);
        Roundtrip second = roundtrip(first.graphml());
        // the GraphML is stable; the CJ is not compared, since the 2nd round adds the parse.* attributes the writer
        // puts on <graph> as graphml:xmlAttributes
        assertThat(second.graphml()).isEqualTo(first.graphml());
        assertThat(second.cj().toCjJsonString()).contains("\"w\":[{\"time\":1,\"value\":1},{\"time\":2,\"value\":5}]");
    }

    @Test
    void dataWithoutTimeJoinsTheTimeSeriesWithDefaultTime() throws Exception {
        Roundtrip rt = roundtrip("""
                <graphml xmlns="http://graphml.graphdrawing.org/xmlns">
                  <key id="k" for="node" attr.name="w" attr.type="double" dynamic="true"/>
                  <graph id="G" edgedefault="directed">
                    <node id="a"><data key="k">0.5</data><data key="k" time="7">1.5</data></node>
                  </graph>
                </graphml>""");
        // no time = the schema default 0, so the entry has no time either
        assertThat(rt.cj().toCjJsonString()).contains("\"w\":[{\"value\":0.5},{\"time\":7,\"value\":1.5}]");
        assertThat(rt.graphml()).containsMatch("<data key=\"k\">0.5</data>");
        assertThat(rt.graphml()).containsMatch(data("k", "7", "1.5"));
    }

    @Test
    void timeValuesOnTheDocument() throws Exception {
        String out = roundtrip("""
                <graphml xmlns="http://graphml.graphdrawing.org/xmlns">
                  <key id="v" for="graphml" attr.name="version" attr.type="string" dynamic="true"/>
                  <data key="v" time="1">draft</data><data key="v" time="2">final</data>
                  <graph id="G" edgedefault="directed"/>
                </graphml>""").graphml();
        assertThat(out).containsMatch(data("v", "1", "draft"));
        assertThat(out).containsMatch(data("v", "2", "final"));
    }

    @Test
    void timeSeriesFromCjGetsADynamicTypedKey() throws Exception {
        // a CJ document with a time series, but no stored GraphML key
        CjDocumentElement cj = roundtrip(TIME_VALUES_1_1).cj();
        cj.dataMutable(d -> d.remove(CjDataProperty.Keys.cjPropertyKey));
        assertThat(cj.toCjJsonString()).doesNotContain("graphml:keys");
        Xml2StringWriter xml = new Xml2StringWriter();
        new CjDocument2Graphml(new Graphml2XmlWriter(xml)).writeDocumentToGraphml(cj);
        String out = xml.resultString();
        assertThat(out).containsMatch("<key (?=[^>]*attr.name=\"w\")(?=[^>]*attr.type=\"int\")(?=[^>]*dynamic=\"true\")");
        assertThat(isValid(out)).isTrue();
    }

    @Test
    void keyKeepsIdAndDynamicWhenItsTypeMustChange() throws Exception {
        // the key says int, but the value is no int: the writer must widen the key to string (#157)
        String out = roundtrip("""
                <graphml xmlns="http://graphml.graphdrawing.org/xmlns">
                  <key id="k" for="node" attr.name="w" attr.type="int" dynamic="true"><desc>weight</desc></key>
                  <graph id="G" edgedefault="directed"><node id="a"><data key="k">heavy</data></node></graph>
                </graphml>""").graphml();
        assertThat(out).containsMatch("<key (?=[^>]*id=\"k\")(?=[^>]*attr.type=\"string\")(?=[^>]*dynamic=\"true\")");
        assertThat(out).contains("<desc>weight</desc>");
        assertThat(out).containsMatch("<data key=\"k\">heavy</data>");
    }
}

package com.graphinout.reader.gxl;

import com.graphinout.base.cj.document.CjDirection;
import com.graphinout.base.cj.document.ICjDocument;
import com.graphinout.base.cj.document.ICjEdge;
import com.graphinout.base.cj.document.ICjEndpoint;
import com.graphinout.base.cj.document.ICjGraph;
import com.graphinout.base.cj.document.ICjNode;
import com.graphinout.base.input.SingleInputSource;
import com.graphinout.foundation.pure.input.ContentError;
import com.graphinout.foundation.pure.json.document.IJsonValue;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.google.common.truth.Truth.assertThat;

class GxlReaderTest {

    /** The "simple example" from the GXL site, as in graph-test-data/xml/gxl/simpleExample. */
    static final String SIMPLE_EXAMPLE = """
            <?xml version="1.0"?>
            <!DOCTYPE gxl SYSTEM "http://www.gupro.de/GXL/gxl-1.0.1.dtd">
            <gxl xmlns:xlink="http://www.w3.org/1999/xlink">
              <graph id="simpleExample" edgemode="directed">
                <type xlink:href="schema.gxl#simpleExampleSchema"/>
                <node id="p"><type xlink:href="schema.gxl#Proc"/><attr name="file"><string>main.c</string></attr></node>
                <node id="q"><type xlink:href="schema.gxl#Proc"/><attr name="file"><string>test.c</string></attr></node>
                <node id="v"><type xlink:href="schema.gxl#Var"/><attr name="line"><int>225</int></attr></node>
                <edge id="r1" from="p" to="v"><type xlink:href="schema.gxl#ref"/><attr name="line"><int>127</int></attr></edge>
                <edge id="c" from="p" to="q"><type xlink:href="schema.gxl#call"/></edge>
              </graph>
            </gxl>
            """;

    final List<ContentError> errors = new ArrayList<>();

    ICjDocument read(String gxl) throws IOException {
        GxlReader reader = new GxlReader();
        reader.setContentErrorHandler(errors::add);
        ICjDocument doc = reader.readToCjDocument(SingleInputSource.of("test.gxl", gxl));
        assertThat(doc).isNotNull();
        return doc;
    }

    static @Nullable IJsonValue prop(com.graphinout.base.cj.document.ICjHasData element, String key) {
        return element.data().getProperty(key);
    }

    static ICjNode node(ICjGraph g, String id) {
        return g.nodes().filter(n -> id.equals(n.id())).findFirst().orElseThrow();
    }

    static ICjEdge edge(ICjGraph g, String id) {
        return g.edges().filter(e -> id.equals(e.id())).findFirst().orElseThrow();
    }

    List<ContentError> errorsAtLevel(ContentError.ErrorLevel level) {
        return errors.stream().filter(e -> e.getLevel() == level).toList();
    }

    @Test
    void simpleExample() throws IOException {
        ICjDocument doc = read(SIMPLE_EXAMPLE);
        assertThat(errors).isEmpty();
        ICjGraph g = doc.graphs().findFirst().orElseThrow();
        assertThat(g.id()).isEqualTo("simpleExample");
        assertThat(prop(g, GXL.KEY_TYPE).asString()).isEqualTo("schema.gxl#simpleExampleSchema");
        assertThat(g.nodes().count()).isEqualTo(3);

        ICjNode p = node(g, "p");
        assertThat(p.types().map(t -> t.type()).toList()).containsExactly("schema.gxl#Proc");
        assertThat(prop(p, "file").asString()).isEqualTo("main.c");
        assertThat(prop(node(g, "v"), "line").asNumber().longValue()).isEqualTo(225L);

        ICjEdge r1 = edge(g, "r1");
        assertThat(r1.type()).isEqualTo("schema.gxl#ref");
        List<ICjEndpoint> eps = r1.endpoints().toList();
        assertThat(eps.get(0).node()).isEqualTo("p");
        assertThat(eps.get(0).direction()).isEqualTo(CjDirection.IN);
        assertThat(eps.get(1).node()).isEqualTo("v");
        assertThat(eps.get(1).direction()).isEqualTo(CjDirection.OUT);
        assertThat(prop(r1, "line").asNumber().longValue()).isEqualTo(127L);
    }

    @Test
    void undirectedGraphWithoutEdgeIds() throws IOException {
        ICjDocument doc = read("""
                <gxl><graph id="1.ct" edgeids="false" edgemode="undirected">
                  <node id="_1"><attr name="chem"><int>8</int>  </attr></node>
                  <node id="_2"><attr name="chem"><int>6</int></attr></node>
                  <edge from="_1" to="_2"><attr name="valence"><int>1</int> </attr></edge>
                </graph></gxl>
                """);
        assertThat(errors).isEmpty();
        ICjEdge e = doc.graphs().findFirst().orElseThrow().edges().findFirst().orElseThrow();
        assertThat(e.id()).isNull();
        assertThat(e.endpoints().allMatch(ICjEndpoint::isUndirected)).isTrue();
        assertThat(prop(e, "valence").asNumber().longValue()).isEqualTo(1L);
    }

    @Test
    void relWithRolesAndDirections() throws IOException {
        ICjDocument doc = read("""
                <gxl><graph id="g" hypergraph="true">
                  <node id="a"/><node id="b"/><node id="c"/>
                  <rel id="r" isdirected="true">
                    <relend target="a" role="caller" direction="out" startorder="1"/>
                    <relend target="b" role="callee" direction="in"/>
                    <relend target="c" direction="none"><attr name="w"><float>0.5</float></attr></relend>
                  </rel>
                </graph></gxl>
                """);
        assertThat(errors).isEmpty();
        ICjEdge r = doc.graphs().findFirst().orElseThrow().edges().findFirst().orElseThrow();
        assertThat(prop(r, GXL.KEY_REL).asBoolean()).isTrue();
        List<ICjEndpoint> eps = r.endpoints().toList();
        assertThat(eps).hasSize(3);
        assertThat(eps.get(0).direction()).isEqualTo(CjDirection.IN);
        assertThat(eps.get(0).type()).isEqualTo("caller");
        assertThat(prop(eps.get(0), GXL.KEY_STARTORDER).asString()).isEqualTo("1");
        assertThat(eps.get(1).direction()).isEqualTo(CjDirection.OUT);
        assertThat(eps.get(1).type()).isEqualTo("callee");
        assertThat(eps.get(2).direction()).isEqualTo(CjDirection.UNDIR);
        assertThat(prop(eps.get(2), "w").asNumber().doubleValue()).isEqualTo(0.5);
    }

    @Test
    void allValueKinds() throws IOException {
        ICjDocument doc = read("""
                <gxl xmlns:xlink="http://www.w3.org/1999/xlink"><graph id="g">
                  <node id="n">
                    <attr name="label"><string>Node N</string></attr>
                    <attr name="b"><bool>true</bool></attr>
                    <attr name="i"><int>-3</int></attr>
                    <attr name="f"><float>2.0</float></attr>
                    <attr name="s"><string> spaced </string></attr>
                    <attr name="e"><enum>RED</enum></attr>
                    <attr name="l"><locator xlink:href="http://example.org/x"/></attr>
                    <attr name="q"><seq><int>1</int><int>2</int></seq></attr>
                    <attr name="t"><tup><string>x</string><bool>false</bool></tup></attr>
                    <attr name="nested"><set><bag><int>1</int></bag></set></attr>
                  </node>
                </graph></gxl>
                """);
        assertThat(errors).isEmpty();
        ICjNode n = node(doc.graphs().findFirst().orElseThrow(), "n");
        assertThat(n.labelEntries().getFirst().value()).isEqualTo("Node N");
        assertThat(prop(n, "label")).isNull();
        assertThat(prop(n, "b").asBoolean()).isTrue();
        assertThat(prop(n, "i").asNumber().longValue()).isEqualTo(-3L);
        assertThat(prop(n, "f").asNumber().doubleValue()).isEqualTo(2.0);
        assertThat(prop(n, "s").asString()).isEqualTo(" spaced ");
        assertThat(prop(n, "e").asString()).isEqualTo("RED");
        assertThat(prop(n, "l").asString()).isEqualTo("http://example.org/x");
        assertThat(prop(n, "q").asArray().size()).isEqualTo(2);
        assertThat(prop(n, "nested").asArray().get_(0).asArray().size()).isEqualTo(1);
        IJsonValue kinds = prop(n, GXL.KEY_KINDS);
        assertThat(kinds.asObject().getString("e")).isEqualTo("enum");
        assertThat(kinds.asObject().getString("l")).isEqualTo("locator");
        assertThat(kinds.asObject().getString("t")).isEqualTo("tup");
        assertThat(kinds.asObject().getString("nested")).isEqualTo("set");
        assertThat(kinds.asObject().get("q")).isNull();
    }

    @Test
    void nestedGraphsInNodeAndEdge() throws IOException {
        ICjDocument doc = read("""
                <gxl><graph id="outer">
                  <node id="a"><graph id="inA"><node id="a1"/></graph></node>
                  <node id="b"/>
                  <edge id="e" from="a" to="b"><graph id="inE"><node id="e1"/></graph></edge>
                </graph></gxl>
                """);
        assertThat(errors).isEmpty();
        ICjGraph g = doc.graphs().findFirst().orElseThrow();
        assertThat(node(g, "a").graphs().findFirst().orElseThrow().id()).isEqualTo("inA");
        assertThat(edge(g, "e").graphs().findFirst().orElseThrow().id()).isEqualTo("inE");
    }

    @Test
    void defaultDirectedWithUndirectedException() throws IOException {
        ICjDocument doc = read("""
                <gxl><graph id="g" edgemode="defaultdirected">
                  <node id="a"/><node id="b"/>
                  <edge id="d" from="a" to="b"/>
                  <edge id="u" from="a" to="b" isdirected="false"/>
                </graph></gxl>
                """);
        assertThat(errors).isEmpty();
        ICjGraph g = doc.graphs().findFirst().orElseThrow();
        assertThat(edge(g, "d").endpoints().allMatch(ICjEndpoint::isDirected)).isTrue();
        assertThat(edge(g, "u").endpoints().allMatch(ICjEndpoint::isUndirected)).isTrue();
        assertThat(prop(g, GXL.KEY_EDGEMODE).asString()).isEqualTo("defaultdirected");
    }

    @Test
    void edgeWithoutTo_isError() throws IOException {
        ICjDocument doc = read("<gxl><graph id='g'><node id='a'/><edge from='a'/></graph></gxl>");
        assertThat(errorsAtLevel(ContentError.ErrorLevel.Error)).hasSize(1);
        assertThat(doc.graphs().findFirst().orElseThrow().edges().count()).isEqualTo(0);
    }

    @Test
    void notGxl_isError() throws IOException {
        read("<graphml><graph id='g'/></graphml>");
        assertThat(errorsAtLevel(ContentError.ErrorLevel.Error)).isNotEmpty();
    }

    @Test
    void malformedXml_isErrorWithLocation() throws IOException {
        GxlReader reader = new GxlReader();
        reader.setContentErrorHandler(errors::add);
        reader.readToCjDocument(SingleInputSource.of("bad.gxl", "<gxl>\n<graph id='g'>\n</gxl>"));
        assertThat(errors).hasSize(1);
        assertThat(errors.getFirst().getLevel()).isEqualTo(ContentError.ErrorLevel.Error);
        assertThat(errors.getFirst().location.line()).isEqualTo(3);
    }

    /** Seen in the wild (RBGL/snacoreex.gxl): blank lines before the XML declaration. */
    @Test
    void whitespaceBeforeXmlDeclaration_isSkippedWithWarning() throws IOException {
        ICjDocument doc = read("\n  <?xml version=\"1.0\" encoding=\"iso-8859-1\"?><gxl><graph id='g'><node id='ä'/></graph></gxl>");
        assertThat(errorsAtLevel(ContentError.ErrorLevel.Error)).isEmpty();
        assertThat(errorsAtLevel(ContentError.ErrorLevel.Warn)).hasSize(1);
        assertThat(doc.graphs().findFirst().orElseThrow().nodes().count()).isEqualTo(1);
    }

    /** Seen in the wild (Kite_graphs_xml, RBGL/demo.gxl): source/target instead of from/to, type as attribute. */
    @Test
    void nonStandardEdgeAttributes_areAcceptedWithOneWarningEach() throws IOException {
        ICjDocument doc = read("""
                <gxl><graph id="g"><node id="a"/><node id="b"/>
                  <edge source="a" target="b" type="Call"/>
                  <edge source="b" target="a" type="Call"/>
                </graph></gxl>
                """);
        assertThat(errorsAtLevel(ContentError.ErrorLevel.Error)).isEmpty();
        assertThat(errorsAtLevel(ContentError.ErrorLevel.Warn)).hasSize(3); // source, target, type: once each
        ICjEdge e = doc.graphs().findFirst().orElseThrow().edges().findFirst().orElseThrow();
        assertThat(e.endpoints().map(ICjEndpoint::node).toList()).containsExactly("a", "b").inOrder();
        assertThat(e.type()).isEqualTo("Call");
    }

    /** A remote DTD (here: an unreachable one) must not be fetched; fetching it would fail the parse. */
    @Test
    void remoteDtdIsNotFetched() throws IOException {
        read("""
                <?xml version="1.0"?>
                <!DOCTYPE gxl SYSTEM "http://127.0.0.1:1/never.dtd">
                <gxl><graph id="g"><node id="a"/></graph></gxl>
                """);
        assertThat(errors).isEmpty();
    }

    /** XXE: an external entity must not be expanded. */
    @Test
    void externalEntityIsNotExpanded() throws IOException {
        ICjDocument doc = read("""
                <?xml version="1.0"?>
                <!DOCTYPE gxl [ <!ENTITY xxe SYSTEM "file:///etc/hosts"> ]>
                <gxl><graph id="g"><node id="a"><attr name="s"><string>[&xxe;]</string></attr></node></graph></gxl>
                """);
        ICjNode a = node(doc.graphs().findFirst().orElseThrow(), "a");
        assertThat(prop(a, "s").asString()).isEqualTo("[]");
    }

}

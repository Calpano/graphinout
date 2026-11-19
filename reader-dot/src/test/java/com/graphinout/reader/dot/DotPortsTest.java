package com.graphinout.reader.dot;

import com.graphinout.base.cj.document.CjDirection;
import com.graphinout.base.cj.document.ICjDocument;
import com.graphinout.base.cj.document.ICjEdge;
import com.graphinout.base.cj.document.ICjEndpoint;
import com.graphinout.base.cj.document.ICjGraph;
import com.graphinout.base.cj.document.ICjHasData;
import com.graphinout.base.cj.document.ICjNode;
import com.graphinout.base.cj.document.ICjPort;
import com.graphinout.base.cj.document.impl.CjDocumentElement;
import com.graphinout.base.text.TextReader;
import com.graphinout.base.text.TextWriterOnStringBuilder;
import com.graphinout.foundation.pure.input.ContentError;
import com.graphinout.foundation.pure.json.document.IJsonObject;
import com.graphinout.foundation.pure.json.document.IJsonValue;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static com.google.common.truth.Truth.assertThat;

/**
 * DOT ports and compass points follow Graphviz: a port must be declared by the node (record field or HTML
 * {@code PORT}), else it may be a compass point, else Graphviz ignores it — and we warn.
 */
class DotPortsTest {

    private final List<ContentError> errors = new ArrayList<>();

    private ICjDocument read(String dot) {
        DotLines2CjDocument reader = new DotLines2CjDocument(errors::add);
        TextReader.read(dot, reader);
        return reader.resultDocument();
    }

    private static String toDot(ICjDocument doc) {
        TextWriterOnStringBuilder out = new TextWriterOnStringBuilder();
        CjDocument2Dot.toDotSyntax(doc, out);
        return out.toString();
    }

    private List<String> warnings() {
        return errors.stream().filter(e -> e.level == ContentError.ErrorLevel.Warn).map(e -> e.message).toList();
    }

    private static Stream<ICjGraph> allGraphs(ICjGraph g) {
        return Stream.concat(Stream.of(g), g.graphs().flatMap(DotPortsTest::allGraphs));
    }

    /** The node {@code id}; if a subgraph repeats it, the instance that carries the ports. */
    private static ICjNode node(ICjDocument doc, String id) {
        List<ICjNode> nodes = doc.graphs().flatMap(DotPortsTest::allGraphs).flatMap(ICjGraph::nodes)
                .filter(n -> id.equals(n.id())).toList();
        return nodes.stream().filter(ICjNode::hasPorts).findFirst().orElse(nodes.get(0));
    }

    private static List<ICjEdge> edges(ICjDocument doc) {
        return doc.graphs().flatMap(DotPortsTest::allGraphs).flatMap(ICjGraph::edges).toList();
    }

    /** Endpoint 0 is the tail (direction in), 1 the head (direction out); CJ does not keep endpoint order. */
    private static ICjEndpoint endpoint(ICjEdge e, int i) {
        List<ICjEndpoint> eps = e.endpoints().toList();
        CjDirection dir = i == 0 ? CjDirection.IN : CjDirection.OUT;
        return eps.stream().filter(ep -> ep.direction() == dir).findFirst().orElse(eps.get(i));
    }

    private static List<String> portIds(ICjNode n) {
        return n.ports().map(ICjPort::id).toList();
    }

    private static ICjPort port(ICjNode n, String id) {
        return n.ports().filter(p -> id.equals(p.id())).findFirst().orElseThrow();
    }

    private static @Nullable String data(ICjHasData hasData, String key) {
        IJsonValue v = hasData.jsonValue();
        IJsonObject o = v == null ? null : v.asObjectOrNull();
        IJsonValue x = o == null ? null : o.get(key);
        return x == null ? null : x.asString();
    }

    @Nested
    class DeclaredPorts {

        @Test
        void recordFieldsAreNodePorts() {
            ICjDocument doc = read("""
                    digraph {
                      node [shape=record];
                      R [label="<f0> a|{<f1> b|< f2 >c}|\\<x\\>"];
                      R:f0 -> R:f2;
                    }
                    """);
            // trimmed, nested record braces flattened, escaped \\<x\\> is text
            assertThat(portIds(node(doc, "R"))).containsExactly("f0", "f1", "f2").inOrder();
            ICjEdge e = edges(doc).get(0);
            assertThat(endpoint(e, 0).port()).isEqualTo("f0");
            assertThat(endpoint(e, 1).port()).isEqualTo("f2");
            assertThat(warnings()).isEmpty();
        }

        @Test
        void mrecordShapeOnTheNodeItself() {
            ICjDocument doc = read("""
                    digraph { M [shape=Mrecord, label="<in> x|<out> y"]; M:out -> M:in; }
                    """);
            assertThat(portIds(node(doc, "M"))).containsExactly("in", "out");
            assertThat(warnings()).isEmpty();
        }

        @Test
        void htmlPortsAreNodePorts() {
            ICjDocument doc = read("""
                    digraph {
                      H [shape=plain, label=<<table><tr><td port="a">x</td><td PORT='b'>y</td></tr></table>>];
                      H:a -> H:b;
                    }
                    """);
            assertThat(portIds(node(doc, "H"))).containsExactly("a", "b");
            assertThat(warnings()).isEmpty();
        }

        @Test
        void portDeclaredAfterUseResolves() {
            ICjDocument doc = read("""
                    digraph { A -> R:p; R [shape=record, label="<p> x"]; }
                    """);
            assertThat(endpoint(edges(doc).get(0), 1).port()).isEqualTo("p");
            assertThat(warnings()).isEmpty();
        }

        @Test
        void recordLabelWithoutRecordShapeDeclaresNothing() {
            ICjDocument doc = read("""
                    digraph { M [label="<p> a"]; M:p -> X; }
                    """);
            assertThat(portIds(node(doc, "M"))).isEmpty();
            assertThat(endpoint(edges(doc).get(0), 0).port()).isNull();
            assertThat(warnings()).containsExactly(
                    "Port 'p' of node 'M' unrecognized and ignored: the node declares no ports, and 'p' is no compass point");
        }

        @Test
        void quotedRecordLabelIsNotHtml() {
            ICjDocument doc = read("""
                    digraph { R [shape=record, label="<a> x|<b> y", tooltip="<b>"]; }
                    """);
            assertThat(portIds(node(doc, "R"))).containsExactly("a", "b");
            // a quoted value starting with '<' is a plain string, not an HTML label object
            assertThat(data(node(doc, "R"), "tooltip")).isEqualTo("<b>");
        }

        @Test
        void nodeDefaultsAreScopedToTheirSubgraph() {
            ICjDocument doc = read("""
                    digraph {
                      subgraph s { node [shape=record]; R [label="<p> x"]; }
                      Q [label="<p> x"];
                      R:p -> Q:p;
                    }
                    """);
            assertThat(portIds(node(doc, "R"))).containsExactly("p");
            assertThat(portIds(node(doc, "Q"))).isEmpty();
            assertThat(warnings()).hasSize(1);
            assertThat(warnings().get(0)).startsWith("Port 'p' of node 'Q' unrecognized");
        }
    }

    @Nested
    class CompassPoints {

        @Test
        void compassPointIsABuiltInPort() {
            ICjDocument doc = read("""
                    digraph { A:n -> B:sw; A:_ -> B:c; }
                    """);
            ICjNode a = node(doc, "A");
            assertThat(portIds(a)).containsExactly("n", "_");
            assertThat(data(port(a, "n"), DotPorts.DATA_COMPASS)).isEqualTo("n");
            assertThat(portIds(node(doc, "B"))).containsExactly("sw", "c");
            assertThat(endpoint(edges(doc).get(0), 0).port()).isEqualTo("n");
            assertThat(warnings()).isEmpty();
        }

        @Test
        void compassOnPortIsNestedPort() {
            ICjDocument doc = read("""
                    digraph { R [shape=record, label="<f0> a|<f1> b"]; R:f0:e -> R:f1:w; R:f0:e -> R:f1; }
                    """);
            ICjPort f0 = port(node(doc, "R"), "f0");
            assertThat(f0.ports().map(ICjPort::id).toList()).containsExactly("f0:e"); // added once
            assertThat(data(f0.ports().findFirst().orElseThrow(), DotPorts.DATA_COMPASS)).isEqualTo("e");
            assertThat(endpoint(edges(doc).get(0), 0).port()).isEqualTo("f0:e");
            assertThat(endpoint(edges(doc).get(0), 1).port()).isEqualTo("f1:w");
            assertThat(warnings()).isEmpty();
        }

        @Test
        void declaredPortWinsOverCompassPoint() {
            ICjDocument doc = read("""
                    digraph { R [shape=record, label="<n> a"]; R:n -> X; }
                    """);
            ICjPort n = port(node(doc, "R"), "n");
            assertThat(data(n, DotPorts.DATA_COMPASS)).isNull();
            assertThat(portIds(node(doc, "R"))).containsExactly("n");
        }

        @Test
        void compassPointsAreLowercaseOnly() {
            ICjDocument doc = read("""
                    digraph { A:N -> B; }
                    """);
            assertThat(portIds(node(doc, "A"))).isEmpty();
            assertThat(warnings()).containsExactly(
                    "Port 'N' of node 'A' unrecognized and ignored: the node declares no ports, and 'N' is no compass point");
        }

        @Test
        void invalidCompassOnDeclaredPortIsIgnored() {
            ICjDocument doc = read("""
                    digraph { R [shape=record, label="<f0> a"]; R:f0:zz -> X; }
                    """);
            ICjEndpoint ep = endpoint(edges(doc).get(0), 0);
            assertThat(ep.port()).isNull();
            assertThat(warnings()).containsExactly("Port 'f0:zz' of node 'R' unrecognized and ignored: "
                    + "'zz' is no compass point (n, ne, e, se, s, sw, w, nw, c, _)");
        }

        @Test
        void compassOnCompassIsIgnored() {
            read("""
                    digraph { A:n:s -> B; }
                    """);
            assertThat(warnings()).containsExactly("Port 'n:s' of node 'A' unrecognized and ignored: the node declares no ports");
        }
    }

    @Nested
    class IgnoredPorts {

        @Test
        void undeclaredPortWarnsWithLocationAndIsKeptInEndpointData() {
            ICjDocument doc = read("""
                    digraph P {
                      A:out -> B:in;
                      C:"p ort" -> D:p:ne;
                    }
                    """);
            assertThat(warnings()).containsExactly( //
                    "Port 'out' of node 'A' unrecognized and ignored: the node declares no ports, and 'out' is no compass point", //
                    "Port 'in' of node 'B' unrecognized and ignored: the node declares no ports, and 'in' is no compass point", //
                    "Port 'p ort' of node 'C' unrecognized and ignored: the node declares no ports, and 'p ort' is no compass point", //
                    "Port 'p:ne' of node 'D' unrecognized and ignored: the node declares no ports, and 'p' is no compass point");
            ContentError first = errors.get(0);
            assertThat(first.location.line()).isEqualTo(2);
            assertThat(first.location.col()).isEqualTo(3);
            assertThat(errors.get(3).location.line()).isEqualTo(3);

            ICjEndpoint d = endpoint(edges(doc).get(1), 1);
            assertThat(d.port()).isNull();
            assertThat(data(d, DotPorts.DATA_PORT)).isEqualTo("p");
            assertThat(data(d, DotPorts.DATA_COMPASS)).isEqualTo("ne");
            assertThat(portIds(node(doc, "A"))).isEmpty();
        }
    }

    @Nested
    class HeadAndTailPort {

        @Test
        void attributesSetEndpointPorts() {
            ICjDocument doc = read("""
                    digraph { R [shape=record, label="<f0> a"]; A -> R [tailport=s, headport="f0:e"]; }
                    """);
            ICjEdge e = edges(doc).get(0);
            assertThat(endpoint(e, 0).port()).isEqualTo("s");
            assertThat(endpoint(e, 1).port()).isEqualTo("f0:e");
            // represented on the endpoints, not duplicated as edge data
            assertThat(data(e, "tailport")).isNull();
            assertThat(data(e, "headport")).isNull();
            assertThat(warnings()).isEmpty();
        }

        @Test
        void attributeOverridesPortOnNodeId() {
            ICjDocument doc = read("""
                    digraph { A:n -> B [tailport=s]; }
                    """);
            assertThat(endpoint(edges(doc).get(0), 0).port()).isEqualTo("s");
            assertThat(portIds(node(doc, "A"))).containsExactly("s");
        }

        @Test
        void edgeDefaultsApply() {
            ICjDocument doc = read("""
                    digraph { edge [headport=w]; A -> B; subgraph { edge [headport=e]; C -> D; } E -> F [headport=n]; }
                    """);
            List<ICjEdge> edges = edges(doc);
            assertThat(edges.stream().map(e -> endpoint(e, 1).port()).toList()).containsExactly("w", "n", "e");
        }

        @Test
        void undeclaredAttributePortWarns() {
            read("""
                    digraph { A -> B [headport=xx]; }
                    """);
            assertThat(warnings()).containsExactly(
                    "Port 'xx' of node 'B' unrecognized and ignored: the node declares no ports, and 'xx' is no compass point");
        }

        @Test
        void undirectedGraph() {
            ICjDocument doc = read("""
                    graph { A:n -- B [headport=s]; }
                    """);
            ICjEdge e = edges(doc).get(0);
            assertThat(endpoint(e, 0).port()).isEqualTo("n");
            assertThat(endpoint(e, 1).port()).isEqualTo("s");
        }
    }

    @Nested
    class Writer {

        @Test
        void roundtripKeepsPortsCompassPointsAndIgnoredPorts() {
            String out = toDot(read("""
                    digraph {
                      R [shape=record, label="<f0> a|<f1> b"];
                      R:f0:e -> R:f1;
                      A:n -> R:f1:sw;
                      A:out -> B:p:ne;
                      C -> D [tailport=s];
                    }
                    """));
            assertThat(out).contains("R:f0:e -> R:f1;");
            assertThat(out).contains("A:n -> R:f1:sw;");
            assertThat(out).contains("A:out -> B:p:ne;");
            assertThat(out).contains("C:s -> D;");
            assertThat(out).doesNotContain("tailport");
            assertThat(out).doesNotContain("dot.");
        }

        @Test
        void htmlLabelIsWrittenAsHtml() {
            String out = toDot(read("""
                    digraph { H [shape=plain, label=<<table><tr><td port="a">x</td></tr></table>>]; H:a -> X; }
                    """));
            assertThat(out).contains("label=<<table><tr><td port=\"a\">x</td></tr></table>>");
            assertThat(out).contains("H:a -> X;");
        }

        @Test
        void defaultRecordShapeIsKeptWithThePorts() {
            ICjDocument doc = read("""
                    digraph { node [shape=Mrecord]; M [label="<p> a"]; M:p -> X; }
                    """);
            assertThat(data(node(doc, "M"), "shape")).isEqualTo("Mrecord");
            // X has no ports, so the default shape is not copied to it
            assertThat(data(node(doc, "X"), "shape")).isNull();
            assertThat(toDot(doc)).contains("M [shape=\"Mrecord\", label=\"<p> a\"];");
        }

        @Test
        void nodeRepeatedInSubgraphGetsNoPorts() {
            String out = toDot(read("""
                    digraph { R [shape=record, label="<p> a"]; { rank=same; R; } R:p -> X; }
                    """));
            assertThat(out).doesNotContain("<p> p");
        }

        @Test
        void portsFromOtherFormatsBecomeARecordLabel() {
            CjDocumentElement doc = new CjDocumentElement();
            doc.addGraph(g -> {
                g.addNode(n -> {
                    n.id("X");
                    n.setLabel(l -> l.addEntry(le -> le.value("a|b")));
                    n.dataMutable(d -> d.add("shape", "box"));
                    n.addPort(p -> p.id("p 1"));
                    n.addPort(p -> p.id("<q>"));
                });
                g.addEdge(e -> {
                    e.addEndpoint(ep -> ep.node("X").port("p 1").direction(CjDirection.IN));
                    e.addEndpoint(ep -> ep.node("X").port("<q>").direction(CjDirection.OUT));
                });
            });
            String out = toDot(doc);
            assertThat(out).contains("X [shape=record, label=\"a\\|b|<p 1> p 1|<\\<q\\>> \\<q\\>\"];");
            assertThat(out).contains("X:\"p 1\" -> X:\"<q>\";");
            // and Graphviz semantics agree: both ports are declared
            ICjDocument back = read(out);
            assertThat(warnings()).isEmpty();
            assertThat(portIds(node(back, "X"))).containsExactly("p 1", "<q>");
        }

        @Test
        void reReadingTheOutputGivesTheSamePorts() {
            ICjDocument first = read("""
                    digraph { R [shape=record, label="<f0> a"]; R:f0:e -> A:n; }
                    """);
            errors.clear();
            ICjDocument second = read(toDot(first));
            assertThat(warnings()).isEmpty();
            assertThat(endpoint(edges(second).get(0), 0).port()).isEqualTo("f0:e");
            assertThat(endpoint(edges(second).get(0), 1).port()).isEqualTo("n");
        }
    }

    @Nested
    class Parsing {

        @Test
        void recordPorts() {
            assertThat(DotPorts.recordPorts("<f0> a|{<f1> b|< f2 >c}|\\<x\\>|<>|<a\\>b>"))
                    .containsExactly("f0", "f1", "f2", "a>b").inOrder();
        }

        @Test
        void htmlPorts() {
            assertThat(DotPorts.htmlPorts("<table port=\"t\"><tr><td PORT='a'>x</td><td Port = \"b\">y</td><td port=\"\">z</td></tr></table>"))
                    .containsExactly("t", "a", "b").inOrder();
        }
    }
}

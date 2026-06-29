package com.graphinout.base.cj.analyze;

import com.graphinout.base.cj.document.CjDocument2CjStream;
import com.graphinout.base.cj.document.CjDocuments;
import com.graphinout.base.cj.document.ICjDocument;
import com.graphinout.base.cj.document.ICjElementType;
import com.graphinout.base.cj.document.ICjEndpoint;
import com.graphinout.base.cj.document.ICjNode;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.google.common.truth.Truth.assertThat;

/** {@link CjMetaGraph} infers a type schema: node/edge types as counted nodes, {@code uses} links and {@code has subtype} generalisation. */
class CjMetaGraphTest {

    /**
     * Person nodes are only linked by {@code knows}; Employee nodes by {@code knows} AND {@code worksAt}. So Person's
     * used-edge set ({knows}) is a strict subset of Employee's ({knows, worksAt}) → Person is a super-type of Employee,
     * the shared {@code knows} use moves up to Person, and Employee keeps only {@code worksAt}.
     */
    private static final String SOURCE = """
            {"graphs":[{"nodes":[
                {"id":"p1","types":["Person"]},
                {"id":"p2","types":["Person"]},
                {"id":"e1","types":["Employee"]},
                {"id":"e2","types":["Employee"]}
              ],"edges":[
                {"type":"knows","endpoints":[{"direction":"out","node":"p1"},{"direction":"in","node":"p2"}]},
                {"type":"knows","endpoints":[{"direction":"out","node":"e1"},{"direction":"in","node":"e2"}]},
                {"type":"worksAt","endpoints":[{"direction":"out","node":"e1"},{"direction":"in","node":"e2"}]}
              ]}]}
            """;

    private record MetaEdge(String type, String from, String to) {
    }

    @Test
    void infersTypesCountsUsesAndSubtypes() throws IOException {
        ICjDocument source = CjDocuments.parseCjJsonString("src", SOURCE);
        ICjDocument meta = CjMetaGraph.metaGraph(source);

        Map<String, ICjNode> byId = CjFeature.allNodes(meta)
                .collect(Collectors.toMap(ICjNode::id, Function.identity()));
        assertThat(byId.keySet()).containsExactly("Person", "Employee", "knows", "worksAt");

        // node types are 'Node' for source node types, 'Edge' for source edge types
        assertThat(metaType(byId.get("Person"))).isEqualTo(CjMetaGraph.META_TYPE_NODE);
        assertThat(metaType(byId.get("Employee"))).isEqualTo(CjMetaGraph.META_TYPE_NODE);
        assertThat(metaType(byId.get("knows"))).isEqualTo(CjMetaGraph.META_TYPE_EDGE);
        assertThat(metaType(byId.get("worksAt"))).isEqualTo(CjMetaGraph.META_TYPE_EDGE);

        // instance counts
        assertThat(count(byId.get("Person"))).isEqualTo(2);
        assertThat(count(byId.get("Employee"))).isEqualTo(2);
        assertThat(count(byId.get("knows"))).isEqualTo(2);
        assertThat(count(byId.get("worksAt"))).isEqualTo(1);

        // 'uses' moved up to the super-type; 'has subtype' from super to sub
        List<MetaEdge> edges = CjFeature.allEdges(meta).map(e -> {
            String from = null;
            String to = null;
            for (ICjEndpoint ep : e.endpoints().toList()) {
                if (ep.isSource()) {
                    from = ep.node();
                } else if (ep.isTarget()) {
                    to = ep.node();
                }
            }
            return new MetaEdge(e.type(), from, to);
        }).toList();

        assertThat(edges).containsExactly(
                new MetaEdge(CjMetaGraph.EDGE_USES, "Person", "knows"),
                new MetaEdge(CjMetaGraph.EDGE_USES, "Employee", "worksAt"),
                new MetaEdge(CjMetaGraph.EDGE_HAS_SUBTYPE, "Person", "Employee"));
    }

    @Test
    void streamingCollectorMatchesDocumentPath() throws IOException {
        ICjDocument source = CjDocuments.parseCjJsonString("src", SOURCE);

        String viaDocument = CjDocuments.toJsonString(CjMetaGraph.metaGraph(source));

        CjMetaGraphCollector collector = new CjMetaGraphCollector();
        CjDocument2CjStream.toCjStream(source, collector); // stream the input instead of holding the document
        String viaStream = CjDocuments.toJsonString(collector.build());

        assertThat(viaStream).isEqualTo(viaDocument);
    }

    private static String metaType(ICjNode n) {
        return n.types().map(ICjElementType::type).findFirst().orElse(null);
    }

    private static long count(ICjNode n) {
        return n.data().getProperty(CjMetaGraph.COUNT).asNumber().longValue();
    }
}

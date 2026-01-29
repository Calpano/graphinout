package com.graphinout.reader.cj;

import com.graphinout.base.cj.CjDocsTestData;
import com.graphinout.base.cj.document.CjDocuments;
import com.graphinout.base.cj.document.ICjDocument;
import com.graphinout.base.cj.document.ICjGraph;
import com.graphinout.base.cj.document.ICjNode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.Logger;

import java.util.Objects;

import static com.google.common.truth.Truth.assertThat;
import static org.slf4j.LoggerFactory.getLogger;

public class CjDocTest {

    private static final Logger log = getLogger(CjDocTest.class);

    private static String labelValue(ICjNode node) {
        return Objects.requireNonNull(node.label_().theEntry()).value();
    }

    /**
     * Should run without errors
     *
     * @param doc
     */
    public void cjDoc2Json(ICjDocument doc) {
        String json = CjDocuments.toJsonString(doc);
        log.debug("CJ:\n{}", json);
    }

    @Test
    public void idVsUri() {
        ICjDocument doc = CjDocsTestData.idVsUri();
        assertThat(doc.graphsAll().count()).isEqualTo(3);
        assertThat(doc.nodesAll().count()).isEqualTo(6);
        assertThat(doc.edgesAll().count()).isEqualTo(3);
        {
            // is interpreted with doc-baseUri as "doi:abc#n1"
            ICjNode node = doc.findNodeById("n1");
            assertThat(node).isNotNull();
            assertThat(labelValue(node)).isEqualTo("Node N1 in G2");
        }
        {
            ICjNode node = doc.findNodeById("https://example.com/n1");
            assertThat(node).isNotNull();
            assertThat(labelValue(node)).isEqualTo("Node N1 in G1");
        }
        {
            ICjNode node = doc.findNodeById("https://example.com/n2");
            assertThat(node).isNotNull();
            assertThat(labelValue(node)).isEqualTo("Node N2 in G1");
        }
        {
            ICjNode node = doc.findNodeById("_:n3");
            assertThat(node).isNotNull();
            assertThat(labelValue(node)).isEqualTo("Node N3 in G1");
        }
        {
            ICjNode node = doc.findNodeById("doi:abc#n1");
            assertThat(node).isNotNull();
            assertThat(labelValue(node)).isEqualTo("Node N1 in G2");
        }
        {
            ICjNode node = doc.findNodeById("doi:abc#n2");
            assertThat(node).isNotNull();
            assertThat(labelValue(node)).isEqualTo("Node N2 in G2");
        }
        {
            ICjNode node = doc.findNodeById("_:n4");
            assertThat(node).isNotNull();
            assertThat(labelValue(node)).isEqualTo("Node N4 in G2");
        }
        // TODO assert graph ids as URIs
        // expect NOT "doi:abc-g1-with'
        // expect "https://example.com/g1-with'
        // expect "doi:abc-g2-without'

        ICjGraph graph = doc.findGraphById("https://example.com/g1-with");
    }

    @ParameterizedTest
    @MethodSource("com.graphinout.base.cj.CjDocsTestData#cjTestDocs")
    void testCj(String name, ICjDocument doc) {
        cjDoc2Json(doc);
    }

    @ParameterizedTest
    @MethodSource("com.graphinout.base.cj.CjDocsEdgeCasesTestData#testDocs")
    void testCjEdgeCases(String name, ICjDocument doc) {
        cjDoc2Json(doc);
    }

}

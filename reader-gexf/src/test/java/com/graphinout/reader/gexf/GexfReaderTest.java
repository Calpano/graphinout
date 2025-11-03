package com.graphinout.reader.gexf;

import com.graphinout.base.cj.document.ICjDocument;
import com.graphinout.base.cj.document.ICjGraph;
import com.graphinout.base.cj.document.ICjNode;
import com.graphinout.base.cj.document.ICjEdge;
import com.graphinout.base.cj.stream.CjStream2CjDocument;
import com.graphinout.foundation.input.InputSource;
import com.graphinout.foundation.input.SingleInputSource;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.util.List;

import static com.google.common.truth.Truth.assertThat;

class GexfReaderTest {

    @Test
    void read() throws Exception {
        InputStream inputStream = getClass().getResourceAsStream("/sample.gexf");
        InputSource inputSource = new SingleInputSource("sample.gexf", inputStream);

        GexfReader reader = new GexfReader();
        CjStream2CjDocument stream = new CjStream2CjDocument();
        reader.read(inputSource, stream);

        ICjDocument cjDocument = stream.getDocument();
        ICjGraph graph = cjDocument.getGraphs().get(0);

        List<ICjNode> nodes = graph.nodes();
        assertThat(nodes).hasSize(2);
        assertThat(nodes.get(0).id()).isEqualTo("0");
        assertThat(nodes.get(0).label().get().getEntries().get(0).value()).isEqualTo("Hello");
        assertThat(nodes.get(1).id()).isEqualTo("1");
        assertThat(nodes.get(1).label().get().getEntries().get(0).value()).isEqualTo("World");

        List<ICjEdge> edges = graph.edges();
        assertThat(edges).hasSize(1);
        assertThat(edges.get(0).getEndpoints().get(0).node()).isEqualTo("0");
        assertThat(edges.get(0).getEndpoints().get(1).node()).isEqualTo("1");
    }
}

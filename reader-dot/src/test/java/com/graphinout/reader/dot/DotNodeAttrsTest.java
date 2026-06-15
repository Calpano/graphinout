package com.graphinout.reader.dot;

import com.graphinout.base.cj.document.ICjDocument;
import com.graphinout.base.text.TextReader;
import com.graphinout.base.text.TextWriterOnStringBuilder;
import com.graphinout.foundation.pure.input.ContentError;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static com.google.common.truth.Truth.assertThat;

/** Regression test for issue #133: node attributes (e.g. {@code a [label="Foo"]}) were dropped on DOT->CJ->DOT. */
class DotNodeAttrsTest {

    private static String dotToDot(String dotIn) {
        List<ContentError> errors = new ArrayList<>();
        DotLines2CjDocument reader = new DotLines2CjDocument(errors::add);
        TextReader.read(dotIn, reader);
        ICjDocument cjDoc = reader.resultDocument();
        TextWriterOnStringBuilder out = new TextWriterOnStringBuilder();
        CjDocument2Dot.toDotSyntax(cjDoc, out);
        return out.toString();
    }

    @Test
    void nodeLabelSurvivesRoundtrip() {
        String in = """
                graph fromWikipedia {
                  graph [size="1,1"];
                  a [label="Foo"];
                  b;
                  a -- b [color="blue"];
                }
                """;
        String out = dotToDot(in);
        assertThat(out).contains("label=\"Foo\"");
        assertThat(out).contains("color=\"blue\"");
    }

    @Test
    void multipleNodeAttrsSurviveRoundtrip() {
        String in = """
                digraph G {
                  a [color="red", shape="box"];
                }
                """;
        String out = dotToDot(in);
        assertThat(out).contains("color=\"red\"");
        assertThat(out).contains("shape=\"box\"");
    }
}

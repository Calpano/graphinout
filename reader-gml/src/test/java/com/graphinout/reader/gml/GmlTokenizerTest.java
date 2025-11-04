package com.graphinout.reader.gml;

import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import java.io.IOException;
import java.io.StringReader;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;

class GmlTokenizerTest {

    @Test
    void testParse() throws IOException {
        String gml = """
                Creator "yFiles"
                Version 2.2
                graph
                [ hierarchic  1
                  directed  1
                  node
                  [ id  0
                    graphics
                    [ x 200.0
                      y 0.0
                    ]
                    LabelGraphics
                    [ text  "January" ]
                  ]
                ]
                """;

        IGmlHandler handler = mock(IGmlHandler.class);
        GmlTokenizer tokenizer = new GmlTokenizer(new StringReader(gml), handler);
        tokenizer.parse();

        InOrder inOrder = inOrder(handler);

        inOrder.verify(handler).key("Creator");
        inOrder.verify(handler).value("yFiles");
        inOrder.verify(handler).key("Version");
        inOrder.verify(handler).value("2.2");
        inOrder.verify(handler).key("graph");
        inOrder.verify(handler).open();
        inOrder.verify(handler).key("hierarchic");
        inOrder.verify(handler).value("1.0");
        inOrder.verify(handler).key("directed");
        inOrder.verify(handler).value("1.0");
        inOrder.verify(handler).key("node");
        inOrder.verify(handler).open();
        inOrder.verify(handler).key("id");
        inOrder.verify(handler).value("0.0");
        inOrder.verify(handler).key("graphics");
        inOrder.verify(handler).open();
        inOrder.verify(handler).key("x");
        inOrder.verify(handler).value("200.0");
        inOrder.verify(handler).key("y");
        inOrder.verify(handler).value("0.0");
        inOrder.verify(handler).close();
        inOrder.verify(handler).key("LabelGraphics");
        inOrder.verify(handler).open();
        inOrder.verify(handler).key("text");
        inOrder.verify(handler).value("January");
        inOrder.verify(handler, times(3)).close();
        inOrder.verifyNoMoreInteractions();
    }
}

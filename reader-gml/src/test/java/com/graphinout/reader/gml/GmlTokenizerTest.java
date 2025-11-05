package com.graphinout.reader.gml;

import com.graphinout.reader.gml.GmlListHandler.Key;
import com.graphinout.reader.gml.GmlListHandler.Value;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;

class GmlTokenizerTest {

    static final String GML_1 = """
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

    static final String GML_NUMBER = """
                    MyDecimal 2.2
                    MyInteger 1
                    MyString foo
            """;

    @Test
    void testParse_1() throws IOException {
        IGmlHandler handler = mock(IGmlHandler.class);
        GmlTokenizer tokenizer = new GmlTokenizer(new StringReader(GML_1), handler);
        tokenizer.parse();

        InOrder inOrder = inOrder(handler);

        inOrder.verify(handler).key("Creator");
        inOrder.verify(handler).value("yFiles");
        inOrder.verify(handler).key("Version");
        inOrder.verify(handler).value("2.2");
        inOrder.verify(handler).key("graph");
        inOrder.verify(handler).open();
        inOrder.verify(handler).key("hierarchic");
        inOrder.verify(handler).value("1");
        inOrder.verify(handler).key("directed");
        inOrder.verify(handler).value("1");
        inOrder.verify(handler).key("node");
        inOrder.verify(handler).open();
        inOrder.verify(handler).key("id");
        inOrder.verify(handler).value("0");
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

    @Test
    void testParse_number() throws IOException {
        GmlListHandler handler = new GmlListHandler();
        GmlTokenizer tokenizer = new GmlTokenizer(new StringReader(GML_NUMBER), handler);
        tokenizer.parse();
        List<Object> list = handler.list();
        assertThat(list).isNotNull();
        assertThat(list).hasSize(6);
        assertThat(list.get(0)).isEqualTo(new Key("MyDecimal"));
        assertThat(list.get(1)).isEqualTo(new Value("2.2"));
        assertThat(list.get(2)).isEqualTo(new Key("MyInteger"));
        assertThat(list.get(3)).isEqualTo(new Value("1"));
        assertThat(list.get(4)).isEqualTo(new Key("MyString"));
        assertThat(list.get(5)).isEqualTo(new Value("foo"));
    }

}

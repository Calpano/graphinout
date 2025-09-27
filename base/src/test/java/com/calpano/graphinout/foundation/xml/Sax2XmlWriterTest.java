package com.calpano.graphinout.foundation.xml;

import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

import java.util.Collections;

import static com.calpano.graphinout.foundation.xml.XmlWriter.CDATA_END;
import static com.calpano.graphinout.foundation.xml.XmlWriter.CDATA_START;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;

class Sax2XmlWriterTest {

    @Test
    void test() throws Exception {
        String xml = "<aaa> bbb " + CDATA_START + " ccc " + CDATA_END + " </aaa>";
        XmlWriter xmlWriter = mock(XmlWriter.class);
        XmlTool.parseAndWriteXml(xml, xmlWriter);

        InOrder inOrder = inOrder(xmlWriter);
        // mockito: show all interactions in order
        // dump all interaction
        System.out.println(
                Mockito.mockingDetails(xmlWriter).printInvocations());

        inOrder.verify(xmlWriter).documentStart();
        inOrder.verify(xmlWriter).elementStart(eq(""),eq("aaa"),eq("aaa"), eq(Collections.emptyMap()));
        inOrder.verify(xmlWriter).characterDataStart(eq(false));
        inOrder.verify(xmlWriter).characterData(eq(" bbb "), eq(false));
        inOrder.verify(xmlWriter).cdataStart();
        inOrder.verify(xmlWriter).characterData(eq(" ccc "), eq(true));
        inOrder.verify(xmlWriter).cdataEnd();
        inOrder.verify(xmlWriter).characterData(eq(" "), eq(false));
        inOrder.verify(xmlWriter).characterDataEnd(eq(false));
        inOrder.verify(xmlWriter).elementEnd(eq(""),eq("aaa"),eq("aaa"));
        inOrder.verify(xmlWriter).documentEnd();
    }

}

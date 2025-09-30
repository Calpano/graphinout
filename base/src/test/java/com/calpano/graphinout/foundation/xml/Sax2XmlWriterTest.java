package com.calpano.graphinout.foundation.xml;

import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

import java.util.Collections;

import static com.calpano.graphinout.foundation.xml.XmlCharacterWriter.CDATA_END;
import static com.calpano.graphinout.foundation.xml.XmlCharacterWriter.CDATA_START;
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
        System.out.println(Mockito.mockingDetails(xmlWriter).printInvocations());

        inOrder.verify(xmlWriter).documentStart();
        inOrder.verify(xmlWriter).elementStart(eq(""), eq("aaa"), eq("aaa"), eq(Collections.emptyMap()));
        inOrder.verify(xmlWriter).charactersStart();
        inOrder.verify(xmlWriter).characters(eq(" bbb "), eq(CharactersKind.Default));
        inOrder.verify(xmlWriter).characters(eq(" ccc "), eq(CharactersKind.CDATA));
        inOrder.verify(xmlWriter).characters(eq(" "), eq(CharactersKind.Default));
        inOrder.verify(xmlWriter).charactersEnd();
        inOrder.verify(xmlWriter).elementEnd(eq(""), eq("aaa"), eq("aaa"));
        inOrder.verify(xmlWriter).documentEnd();
    }

}

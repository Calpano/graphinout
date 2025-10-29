package com.graphinout.foundation.xml.sax;

import com.graphinout.foundation.xml.CharactersKind;
import com.graphinout.foundation.xml.writer.XmlCharacter2AppendableWriter;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static com.google.common.truth.Truth.assertThat;

class SaxCharBufferTest {

    @Test
    void test() throws IOException {
        StringBuilder sb = new StringBuilder();
        XmlCharacter2AppendableWriter xmlCharWriter = new XmlCharacter2AppendableWriter(sb, true);
        SaxCharBuffer sax = new SaxCharBuffer(xmlCharWriter);
        sax.kindStart(CharactersKind.Default);
        String input = "Hello &amp;quot; World";
        String parserSends = "Hello &quot; World";
        String expected = input;
        sax.characters(parserSends);
        sax.charactersEnd();
        assertThat(sb.toString()).isEqualTo(expected);
    }

}

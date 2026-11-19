package com.graphinout.base.xml.factory;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.xml.sax.InputSource;
import org.xml.sax.helpers.DefaultHandler;

import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;

import static com.google.common.truth.Truth.assertThat;

/** All XML parsing of untrusted input goes through {@link XmlFactory}; it must never resolve anything external (XXE). */
class XmlFactoryTest {

    @TempDir
    Path dir;

    static String parseText(String xml) throws Exception {
        StringBuilder text = new StringBuilder();
        XmlFactory.createSaxParser().parse(new InputSource(new StringReader(xml)), new DefaultHandler() {
            @Override
            public void characters(char[] ch, int start, int length) {
                text.append(ch, start, length);
            }
        });
        return text.toString();
    }

    @Test
    @DisplayName("external general entities are not resolved")
    void externalEntity() throws Exception {
        Files.writeString(dir.resolve("secret.txt"), "SECRET42");
        for (String file : new String[]{"secret.txt", "missing.txt"}) {
            String url = dir.resolve(file).toUri().toString();
            // a missing file would throw FileNotFoundException if it were opened
            String text = parseText("<!DOCTYPE r [ <!ENTITY x SYSTEM \"" + url + "\"> ]><r>&x;</r>");
            assertThat(text).doesNotContain("SECRET42");
        }
    }

    @Test
    @DisplayName("external parameter entities and DTDs are not loaded")
    void externalDtd() throws Exception {
        String missing = dir.resolve("missing.dtd").toUri().toString();
        assertThat(parseText("<!DOCTYPE r SYSTEM \"" + missing + "\"><r>ok</r>")).isEqualTo("ok");
        assertThat(parseText("<!DOCTYPE r [ <!ENTITY % p SYSTEM \"" + missing + "\"> %p; ]><r>ok</r>")).isEqualTo("ok");
    }

    @Test
    @DisplayName("internal entities still work")
    void internalEntity() throws Exception {
        assertThat(parseText("<!DOCTYPE r [ <!ENTITY x \"hello\"> ]><r>&x;</r>")).isEqualTo("hello");
    }

}

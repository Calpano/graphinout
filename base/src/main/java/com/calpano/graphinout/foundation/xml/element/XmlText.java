package com.calpano.graphinout.foundation.xml.element;

import com.calpano.graphinout.foundation.xml.CharactersKind;
import com.calpano.graphinout.foundation.xml.XmlWriter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class XmlText extends XmlNode {

    static class Section {

        final String text;
        final CharactersKind charactersKind;

        public Section(String text, CharactersKind charactersKind) {
            this.text = text;
            this.charactersKind = charactersKind;
        }

    }

    private final List<Section> sections = new ArrayList<>();

    public XmlText() {
    }

    public XmlText addSection(String text, CharactersKind charactersKind) {
        sections.add(new Section(text, charactersKind));
        return this;
    }

    @Override
    void fire(XmlWriter writer) throws IOException {
        writer.charactersStart();
        for (Section section : sections) {
            writer.characters(section.text, section.charactersKind);
        }
        writer.charactersEnd();
    }

}

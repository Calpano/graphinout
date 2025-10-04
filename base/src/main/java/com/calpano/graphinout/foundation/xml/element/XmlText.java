package com.calpano.graphinout.foundation.xml.element;

import com.calpano.graphinout.foundation.xml.CharactersKind;
import com.calpano.graphinout.foundation.xml.XmlWriter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class XmlText extends XmlNode {

    public static class Section {

        private final CharactersKind charactersKind;
        private String text;

        public Section(String text, CharactersKind charactersKind) {
            this.text = text;
            this.charactersKind = charactersKind;
        }

        public CharactersKind charactersKind() {
            return charactersKind;
        }

        public boolean isEmpty() {
            return text.isEmpty();
        }

        public void removeIgnorableWhitespace() {
            this.text = switch (charactersKind) {
                case IgnorableWhitespace, Default -> text.trim();
                case CDATA, PreserveWhitespace -> text;
            };
        }

        public String text() {
            return text;
        }

        @Override
        public String toString() {
            return "<" + charactersKind.name() + ":'" + text + ">";
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
    public Stream<XmlNode> directChildren() {
        return Stream.empty();
    }

    @Override
    public void fire(XmlWriter writer) throws IOException {
        writer.charactersStart();
        for (Section section : sections) {
            writer.characters(section.text, section.charactersKind);
        }
        writer.charactersEnd();
    }

    public boolean isEmpty() {
        return sections.isEmpty();
    }

    public void removeIgnorableWhitespace() {
        sections.forEach(Section::removeIgnorableWhitespace);
        sections.removeIf(Section::isEmpty);
    }

    public List<Section> sections() {
        return sections;
    }

    @Override
    public String toString() {
        if (sections.isEmpty()) {
            return "XmlText{--}";
        }
        if (sections.size() == 1) {
            return "XmlText{" + sections.getFirst().toString() + "}";
        }

        return "XmlText{" + "sections(" + sections.size() +
                ")=" + sections.stream().map(Section::toString).reduce("", (a, b) -> a + b) + '}';
    }

}

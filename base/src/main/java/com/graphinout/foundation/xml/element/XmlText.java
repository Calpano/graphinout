package com.graphinout.foundation.xml.element;

import com.graphinout.foundation.xml.CharactersKind;
import com.graphinout.foundation.xml.XML;
import com.graphinout.foundation.xml.XmlWriter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class XmlText implements IXmlNode {

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

        public boolean isEmpty(XML.XmlSpace xmlSpace) {
            return switch (charactersKind) {
                case Default ->
                    switch (xmlSpace) {
                        case preserve -> text.isEmpty();
                        // remove whitespace if we may
                        case default_ -> XML.isWhitespace(text);
                    };
                case CDATA, PreserveWhitespace ->
                     text.isEmpty();
                case IgnorableWhitespace ->
                    XML.isWhitespace(text);
            };
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

    public static IXmlNode of(String text, CharactersKind charactersKind) {
        XmlText xmlText = new XmlText();
        xmlText.addSection(text, charactersKind);
        return xmlText;
    }

    @SuppressWarnings("UnusedReturnValue")
    public XmlText addSection(String text, CharactersKind charactersKind) {
        sections.add(new Section(text, charactersKind));
        return this;
    }

    @Override
    public Stream<IXmlNode> directChildren() {
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

    public boolean hasEmptyContent(XML.XmlSpace xmlSpace) {
        return sections.stream().allMatch(section -> section.isEmpty(xmlSpace));
    }

    public void removeIgnorableWhitespace() {
        sections.forEach(Section::removeIgnorableWhitespace);
        sections.removeIf(section -> section.isEmpty(XML.XmlSpace.default_));
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

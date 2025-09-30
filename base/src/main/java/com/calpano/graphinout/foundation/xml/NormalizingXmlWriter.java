package com.calpano.graphinout.foundation.xml;

import com.calpano.graphinout.foundation.util.MapMap;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.TreeMap;
import java.util.function.BiPredicate;
import java.util.function.Function;

@SuppressWarnings("UnusedReturnValue")
public class NormalizingXmlWriter<T extends NormalizingXmlWriter<T>> implements XmlWriter {


    public interface Action {

        enum Kind {Attribute, ElementSkip}

        Kind kind();

    }

    @FunctionalInterface
    public interface AttributeAction extends Action, Function<Map<String, String>, Map<String, String>> {

        default Map<String, String> apply(Map<String, String> attributes) {
            return attributes(attributes);
        }

        /**
         * Action can add, remove or modify attributes. The result is always lexicographically sorted.
         *
         * @param attributes immutable input
         */
        Map<String, String> attributes(Map<String, String> attributes);

        default Kind kind() {return Kind.Attribute;}

    }

    @FunctionalInterface
    public interface ElementSkipAction extends Action, BiPredicate<String, Map<String, String>> {

        default Kind kind() {return Kind.ElementSkip;}

        /**
         * Action can skip an element
         *
         * @param attributes immutable input
         * @return null to remove the element
         */
        boolean skip(String elementName, Map<String, String> attributes);

        default boolean test(String name, Map<String, String> attributes) {
            return skip(name, attributes);
        }

    }

    /** currently open mixed-content-model elements */
    private final Stack<String> stackOfContentElements = new Stack<>();
    private final MapMap<String, Action.Kind, Action> elementActions = MapMap.create();
    private final XmlWriter xmlWriter;
    private String skipElementsUntil = null;
    /** XML element names with mixed content model (#PCDATA). null = treat ALL elements as mixed content. */
    private Set<String> contentElements = null;

    /**
     * @param downstreamXmlWriter to forward to
     */
    public NormalizingXmlWriter(XmlWriter downstreamXmlWriter) {this.xmlWriter = downstreamXmlWriter;}

    public T addAction(String name, Action action) {
        Action prev = elementActions.put(name, action.kind(), action);
        assert prev == null : "action already added for name=" + name + " action=" + action;
        return this_();
    }

    public void characters(String characters, CharactersKind kind) throws IOException {
        if (skipElementsUntil != null) {
            // we are in skipMode
            return;
        }
        if(characters.isEmpty())
            return;
        if (isInContentElement() || kind== CharactersKind.CDATA) {
            // forward only when inside a (maybe nested) content element OR when in CDATA
            xmlWriter.characters(characters, kind);
        } else {
            //  forward only non-whitespace content
            if (!characters.trim().isEmpty()) {
                xmlWriter.characters(characters, kind);
            }
        }
    }

    public void charactersEnd() {
    }

    public void charactersStart() {
    }


    @Override
    public void documentEnd() throws IOException {
        xmlWriter.documentEnd();
    }

    @Override
    public void documentStart() throws IOException {
        xmlWriter.documentStart();
    }

    @Override
    public void elementEnd(String uri, String localName, String qName) throws IOException {
        // deactivate skipMode?
        if (localName.equals(skipElementsUntil)) {
            skipElementsUntil = null;
            // but skip this elementEnd
            return;
        }
        if (skipElementsUntil != null) {
            // we are in skipMode
            return;
        }

        if (isMixedContentElement(localName)) {
            String started = stackOfContentElements.pop();
            assert localName.equals(started);
        }
        xmlWriter.elementEnd(uri, localName, qName);
    }

    @Override
    public void elementStart(String uri, String localName, String qName, Map<String, String> attributes) throws IOException {
        if (skipElementsUntil != null) {
            // skip element
            return;
        }

        if (isMixedContentElement(localName)) {
            stackOfContentElements.push(localName);
        }

        @Nullable Action elementAction = elementActions.get(localName, Action.Kind.ElementSkip);
        if (elementAction != null) {
            assert elementAction instanceof ElementSkipAction : "action is not an instance of " + ElementSkipAction.class.getSimpleName();
            boolean skip = ((ElementSkipAction) elementAction).test(localName, attributes);
            if (skip) {
                // skip it
                skipElementsUntil = localName;
                return;
            }
        }

        // work on attributes
        Action attAction = elementActions.get(localName, Action.Kind.Attribute);
        // using TreeMap to sort attributes alphabetically
        Map<String, String> sortedAtts = new TreeMap<>();
        if (attAction instanceof AttributeAction attributeAction) {
            Map<String, String> changedAtts = attributeAction.apply(attributes);
            sortedAtts.putAll(changedAtts);
        } else {
            sortedAtts.putAll(attributes);
        }

        xmlWriter.elementStart(uri, localName, qName, sortedAtts);
    }

    /** @return inside a (maybe nested) content element */
    public boolean isInContentElement() {
        if (contentElements == null) return true;
        return !stackOfContentElements.isEmpty();
    }

    public boolean isMixedContentElement(String name) {
        return contentElements == null || contentElements.contains(name);
    }

    @Override
    public void lineBreak() throws IOException {
        xmlWriter.lineBreak();
    }

    @Override
    public void raw(String rawXml) throws IOException {
        xmlWriter.raw(rawXml);
    }

    /**
     * @param contentElements use <code>null</code> to treat any XML element as content element
     */
    public T withContentElements(@Nullable Set<String> contentElements) {
        if (contentElements == null) {
            this.contentElements = null;
        } else {
            this.contentElements = new HashSet<>(contentElements);
        }
        return this_();
    }

    private T this_() {
        //noinspection unchecked
        return (T) this;
    }


}

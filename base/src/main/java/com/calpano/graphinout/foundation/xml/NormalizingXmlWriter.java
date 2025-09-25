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
public class NormalizingXmlWriter<T extends NormalizingXmlWriter<T>> extends DelegatingXmlWriter {


    public interface Action {

        enum Kind {Attribute, ElementSkip}

        Kind kind();

    }

    @FunctionalInterface
    public interface AttributeAction extends Action, Function<Map<String, String>, Map<String, String>> {

        default Kind kind() { return Kind.Attribute;}

        default Map<String, String> apply(Map<String, String> attributes) {
            return attributes(attributes);
        }

        /**
         * Action can add, remove or modify attributes. The result is always lexicographically sorted.
         *
         * @param attributes immutable input
         */
        Map<String, String> attributes(Map<String, String> attributes);

    }

    @FunctionalInterface
    public interface ElementSkipAction extends Action, BiPredicate<String, Map<String, String>> {

        default Kind kind() { return Kind.ElementSkip;}

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
    private final StringBuilder charBuffer = new StringBuilder();
    private final MapMap<String, Action.Kind, Action> elementActions = MapMap.create();
    private String skipElementsUntil = null;
    /** XML element names with mixed content model (#PCDATA). null = treat ALL elements as mixed content. */
    private Set<String> contentElements = null;

    /**
     * @param downstreamXmlWriter to forward to
     */
    public NormalizingXmlWriter(XmlWriter downstreamXmlWriter) {super(downstreamXmlWriter);}

    public T addAction(String name, Action action) {
        Action prev = elementActions.put(name, action.kind(), action);
        assert prev == null : "action already added for name=" + name + " action=" + action;
        return this_();
    }

    @Override
    public void characterData(String characterData, boolean isInCdata) throws IOException {
        if (skipElementsUntil != null) {
            // we are in skipMode
            return;
        }
        charBuffer.append(characterData);
    }

    @Override
    public void characterDataEnd(boolean isInCdata) throws IOException {
        if (skipElementsUntil != null) {
            // we are in skipMode
            return;
        }
        // emit
        String c = charBuffer.toString();
        if (isInContentElement() || isInCdata) {
            // forward only when inside a (maybe nested) content element OR when in CDATA
            super.characterData(c, isInCdata);
        } else {
            //  forward only non-whitespace content
            if (!c.trim().isEmpty()) {
                super.characterData(c, false);
            }
        }
        charBuffer.setLength(0);
    }

    @Override
    public void characterDataStart(boolean isInCdata) {
        if (skipElementsUntil != null) {
            // we are in skipMode
            return;
        }
        //   _log.info("characterDataStart: {}", isInCdata);
    }

    @Override
    public void elementEnd(String name) throws IOException {
        // deactivate skipMode?
        if (name.equals(skipElementsUntil)) {
            skipElementsUntil = null;
            // but skip this elementEnd
            return;
        }
        if (skipElementsUntil != null) {
            // we are in skipMode
            return;
        }

        if (isMixedContentElement(name)) {
            String started = stackOfContentElements.pop();
            assert name.equals(started);
        }
        super.elementEnd(name);
    }

    @Override
    public void elementStart(String name, Map<String, String> attributes) throws IOException {
        if (skipElementsUntil != null) {
            // skip element
            return;
        }

        if (isMixedContentElement(name)) {
            stackOfContentElements.push(name);
        }

        @Nullable Action elementAction = elementActions.get(name, Action.Kind.ElementSkip);
        if (elementAction != null) {
            assert elementAction instanceof ElementSkipAction : "action is not an instance of " + ElementSkipAction.class.getSimpleName();
            boolean skip = ((ElementSkipAction) elementAction).test(name, attributes);
            if (skip) {
                // skip it
                skipElementsUntil = name;
                return;
            }
        }

        // work on attributes
        Action attAction = elementActions.get(name, Action.Kind.Attribute);
        // using TreeMap to sort attributes alphabetically
        Map<String, String> sortedAtts = new TreeMap<>();
        if (attAction instanceof AttributeAction attributeAction) {
            Map<String, String> changedAtts = attributeAction.apply(attributes);
            sortedAtts.putAll(changedAtts);
        } else {
            sortedAtts.putAll(attributes);
        }

        super.elementStart(name, sortedAtts);
    }

    /** @return inside a (maybe nested) content element */
    public boolean isInContentElement() {
        if (contentElements == null) return true;
        return !stackOfContentElements.isEmpty();
    }

    public boolean isMixedContentElement(String name) {
        return contentElements == null || contentElements.contains(name);
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

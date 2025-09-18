package com.calpano.graphinout.foundation.xml;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.TreeMap;
import java.util.function.Function;

@SuppressWarnings("UnusedReturnValue")
public class NormalizingXmlWriter<T extends NormalizingXmlWriter<T>> extends DelegatingXmlWriter {

    public interface Action {}

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

    }

    /** currently open mixed-content-model elements */
    private final Stack<String> stackOfContentElements = new Stack<>();
    private final StringBuilder charBuffer = new StringBuilder();
    private final Map<String, Action> elementActions = new HashMap<>();
    /** XML element names with mixed content model (#PCDATA). null = treat ALL elements as mixed content. */
    private Set<String> contentElements = null;

    /**
     * @param downstreamXmlWriter to forward to
     */
    public NormalizingXmlWriter(XmlWriter downstreamXmlWriter) {super(downstreamXmlWriter);}

    @Override
    public void characterData(String characterData, boolean isInCdata) throws IOException {
        charBuffer.append(characterData);
    }

    @Override
    public void characterDataEnd(boolean isInCdata) throws IOException {
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
    public void characterDataStart(boolean isInCdata)
    {
        //   _log.info("characterDataStart: {}", isInCdata);
    }

    @Override
    public void elementEnd(String name) throws IOException {
        if (isMixedContentElement(name)) {
            String started = stackOfContentElements.pop();
            assert name.equals(started);
        }
        super.elementEnd(name);
    }

    @Override
    public void elementStart(String name, Map<String, String> attributes) throws IOException {
        if (isMixedContentElement(name)) {
            stackOfContentElements.push(name);
        }
        // using TreeMap to sort attributes alphabetically
        Map<String, String> sortedAtts = new TreeMap<>();

        @Nullable Action action = elementActions.get(name);
        if (action instanceof AttributeAction attributeAction) {
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

    public T addAction(String name, Action action) {
        Action prev = elementActions.put(name, action);
        assert prev == null;
        return this_();
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

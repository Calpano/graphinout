package com.calpano.graphinout.reader.cj;

import com.calpano.graphinout.base.cj.CjEventStream;
import com.calpano.graphinout.base.cj.CjProperties;
import com.calpano.graphinout.foundation.json.JsonEventStream;
import com.calpano.graphinout.foundation.json.JsonException;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Stack;

/**
 * Function: Recognize some JSON events as connected JSON. Emit other JSON as meta-data.
 * <p>
 * Strategy: Merge aliase, e.g., normalize both "node" and "nodes" to "nodes". Do not buffer.
 */
public class Json2Cj implements JsonEventStream {

    public enum Container {
        JsonObject, JsonArray, Document, Graph, Node, Port, Edge, Label, LabelEntry, Endpoint
    }


    static class ParseStack {

        public record Step(Object containerOrPropertyKey, boolean isCj, @Nullable Container expect) {

            @Override
            public @NonNull String toString() {
                return "Step " + containerOrPropertyKey + " cj=" + isCj + " expect=" + expect;
            }

        }

        private final Stack<Step> stack = new Stack<>();

        public void clear() {
            stack.clear();
        }

        public void containerEnd() {
            stack.pop();
            onAfterValueEnd();
        }

        public @Nullable Container currentContainer() {
            // peek from the top until we find a container
            for (int i = stack.size() - 1; i >= 0; i--) {
                Step step = stack.get(i);
                if (step.containerOrPropertyKey instanceof Container) {
                    return (Container) step.containerOrPropertyKey;
                }
            }
            return null;
        }

        public @Nullable String currentPropertyKey() {
            if (stack.isEmpty()) {
                return null;
            }
            Object o = stack.peek().containerOrPropertyKey;
            if (o instanceof String) {
                return (String) o;
            }
            return null;
        }

        public @Nullable Container expect() {
            return stack.isEmpty() ? null : stack.peek().expect;
        }

        public void onAfterValueEnd() {
            if (!stack.isEmpty() && stack.peek().containerOrPropertyKey instanceof String) {
                stack.pop();
            }
        }

        public void push(Object containerOrPropertyKey, boolean isCj, @Nullable Container expect) {
            stack.push(new Step(containerOrPropertyKey, isCj, expect));
        }

    }

    /** The sink for the converted events. */
    final CjEventStream cjSink;

    /** The current context from root: stack of Container and String (property keys). */
    private final ParseStack parseStack = new ParseStack();

    public Json2Cj(CjEventStream cjSink) {
        this.cjSink = cjSink;
    }

    @Override
    public void arrayEnd() throws JsonException {
        if (parseStack.expect() == null) {
            cjSink.arrayEnd();
        }
        parseStack.containerEnd();
    }

    @Override
    public void arrayStart() throws JsonException {
        Container expect = parseStack.expect();
        if (expect == null) {
            cjSink.arrayStart();
            parseStack.push(Container.JsonArray, false, null);
        } else {
            parseStack.push(Container.JsonArray, true, expect);
        }
    }

    @Override
    public void documentEnd() {
        cjSink.documentEnd();
        parseStack.containerEnd();
    }

    @Override
    public void documentStart() {
        reset();
        cjSink.documentStart();
    }

    @Override
    public void objectEnd() throws JsonException {
        Container container = parseStack.currentContainer();
        assert container != null;
        switch (container) {
            case Container.Node -> cjSink.nodeEnd();
            case Container.Edge -> cjSink.edgeEnd();
            case Container.Endpoint -> cjSink.endpointEnd();
            case Container.Port -> cjSink.portEnd();
            case Container.Graph -> cjSink.graphEnd();
            case Container.Label -> cjSink.labelEnd();
            case Container.LabelEntry -> cjSink.labelEntryEnd();
            case Container.Document -> {
                // do nothing
            }
            default -> // JSON
                    cjSink.objectEnd();
        }
        parseStack.containerEnd();
    }

    @Override
    public void objectStart() throws JsonException {
        // known JSON interpreted as CJ
        Container expect = parseStack.expect();
        if (expect == null) {
            // JSON
            parseStack.push(Container.JsonObject, false, null);
        } else {
            parseStack.push(expect, true, null);
        }
        switch (expect) {
            case Document -> {
                // do nothing
            }
            case Graph -> cjSink.graphStart();
            case Node -> cjSink.nodeStart();
            case Port -> cjSink.portStart();
            case Edge -> cjSink.edgeStart();
            case LabelEntry -> cjSink.labelEntryStart();
            case Endpoint -> cjSink.endpointStart();
            case Label -> cjSink.labelStart();
            case JsonArray, JsonObject -> {
                // we expected this, e.g. the root object
            }
            case null ->
                // JSON
                    cjSink.objectStart();
        }
    }

    @Override
    public void onBigDecimal(BigDecimal bigDecimal) {
        if (!processAsConnectedJson(bigDecimal)) {
            cjSink.onBigDecimal(bigDecimal);
        }
        onAfterValueEnd();
    }

    @Override
    public void onBigInteger(BigInteger bigInteger) {
        if (!processAsConnectedJson(bigInteger)) {
            cjSink.onBigInteger(bigInteger);
        }
        onAfterValueEnd();
    }

    @Override
    public void onBoolean(boolean b) throws JsonException {
        cjSink.onBoolean(b);
        onAfterValueEnd();
    }

    @Override
    public void onDouble(double d) throws JsonException {
        if (!processAsConnectedJson(d)) {
            cjSink.onDouble(d);
        }
        onAfterValueEnd();
    }

    @Override
    public void onFloat(float f) throws JsonException {
        if (!processAsConnectedJson(f)) {
            cjSink.onFloat(f);
        }
        onAfterValueEnd();
    }

    @Override
    public void onInteger(int i) throws JsonException {
        if (!processAsConnectedJson(i)) {
            cjSink.onInteger(i);
        }
        onAfterValueEnd();
    }

    @Override
    public void onKey(String key) throws JsonException {
        String normKey = normalizeProperty(key);
        // interpret key FIXME take current context into account
        switch (normKey) {
            case CjProperties.NODES -> parseStack.push(key, true, Container.Node);
            case CjProperties.EDGES -> parseStack.push(key, true, Container.Edge);
            case CjProperties.ENDPOINTS -> parseStack.push(key, true, Container.Endpoint);
            case CjProperties.PORTS -> parseStack.push(key, true, Container.Port);
            case CjProperties.GRAPHS -> parseStack.push(key, true, Container.Graph);
            case CjProperties.LABEL -> parseStack.push(key, true, Container.Label);
            // properties
            case CjProperties.ID -> parseStack.push(key, true, null);
            case CjProperties.LANGUAGE -> parseStack.push(key, true, null);
            case CjProperties.VALUE -> parseStack.push(key, true, null);
            default -> {
            }
        }

        // JSON
        parseStack.push(key, false, null);
        cjSink.onKey(key);
    }

    @Override
    public void onLong(long l) throws JsonException {
        if (!processAsConnectedJson(l)) {
            cjSink.onLong(l);
        }
        onAfterValueEnd();
    }

    @Override
    public void onNull() throws JsonException {
        cjSink.onNull();
        onAfterValueEnd();
    }

    @Override
    public void onString(String s) throws JsonException {
        if (!processAsConnectedJson(s)) {
            cjSink.onString(s);
        }
        onAfterValueEnd();
    }

    public void reset() {
        parseStack.clear();
        parseStack.push(Container.Document, true, Container.JsonObject);
    }

    /** Normalize property names using aliases */
    private String normalizeProperty(String property) {
        return CjProperties.normalizeProperty(property);
    }

    /** after container or primitive ends */
    private void onAfterValueEnd() {
        parseStack.onAfterValueEnd();
    }

    private boolean processAsConnectedJson(Object primitive) {
        @Nullable String currentPropertyKey = parseStack.currentPropertyKey();
        switch (currentPropertyKey) {
            case CjProperties.ID -> {
                cjSink.id("" + primitive);
                return true;
            }
            case CjProperties.LANGUAGE -> {
                cjSink.language("" + primitive);
                return true;
            }
            case CjProperties.VALUE -> {
                cjSink.value("" + primitive);
                return true;
            }
            case null, default -> {
                return false;
            }
        }
    }


}

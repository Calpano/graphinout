package com.calpano.graphinout.reader.cj;

import com.calpano.graphinout.base.cj.CjDirection;
import com.calpano.graphinout.base.cj.CjProperties;
import com.calpano.graphinout.base.cj.CjType;
import com.calpano.graphinout.base.cj.CjWriter;
import com.calpano.graphinout.foundation.json.JsonException;
import com.calpano.graphinout.foundation.json.JsonType;
import com.calpano.graphinout.foundation.json.JsonWriter;
import edu.umd.cs.findbugs.annotations.Nullable;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

/**
 * Function: Recognize some JSON events as connected JSON. Emit other JSON as meta-data.
 * <p>
 * Strategy: Merge aliase, e.g., normalize both "node" and "nodes" to "nodes". Do not buffer.
 */
public class Json2CjWriter implements JsonWriter {

    static class ParseStack {

        /** Stack of CjType or JsonType */
        private final Stack<Object> stack = new Stack<>();
        private final Set<CjType> expectedCjTypes = new HashSet<>();

        public void clear() {
            stack.clear();
        }

        public void containerEnd() {
            stack.pop();
        }

        public void expect(Set<CjType> expected) {
            expectedCjTypes.clear();
            expectedCjTypes.addAll(expected);
        }

        public CjType expectedCjType(JsonType jsonType) {
            return Util.findExactlyOne(expectedCjTypes(), jsonType);
        }

        public Set<CjType> expectedCjTypes() {
            return expectedCjTypes;
        }

        public boolean isInJson() {
            if (stack.isEmpty()) return false;
            return stack.peek() instanceof JsonType;
        }

        public @Nullable CjType peekCjType() {
            if (stack.isEmpty()) return null;
            Object cjOrJsonType = stack.peek();
            if (cjOrJsonType instanceof CjType) {
                return (CjType) cjOrJsonType;
            }
            return null;
        }

        public void popJsonPropertyMaybe() {
            if (!stack.isEmpty() && stack.peek() == JsonType.Property) {
                stack.pop();
            }
        }

        public void push(CjType cjType) {
            stack.push(cjType);
        }

        public void push(JsonType jsonType) {
            stack.push(jsonType);
        }

    }

    /** The sink for the converted events. */
    final CjWriter cjWriter;

    /** The current context from root: stack of Container and String (property keys). */
    private final ParseStack parseStack = new ParseStack();
    private final StringBuilder stringBuffer = new StringBuilder();

    public Json2CjWriter(CjWriter cjWriter) {
        this.cjWriter = cjWriter;
    }

    @Override
    public void arrayEnd() throws JsonException {
        if (parseStack.isInJson()) {
            cjWriter.arrayEnd();
        } else {
            cjWriter.listEnd(parseStack.peekCjType());
        }
        parseStack.containerEnd();
    }

    @Override
    public void arrayStart() throws JsonException {
        if (parseStack.isInJson()) {
            parseStack.push(JsonType.Array);
            cjWriter.arrayStart();
        } else {
            Set<CjType> expected = parseStack.expectedCjTypes();
            if (expected.isEmpty()) {
                throw new IllegalArgumentException("Expected CJ type on stack, but found none.");
            }
            CjType cjType = Util.findExactlyOne(expected, JsonType.Array);
            parseStack.push(cjType);
            cjWriter.listStart(cjType);
        }
    }

    @Override
    public void documentEnd() {
        cjWriter.documentEnd();
    }

    @Override
    public void documentStart() {
        reset();
        cjWriter.documentStart();
    }

    @Override
    public void objectEnd() throws JsonException {
        if (parseStack.isInJson()) {
            parseStack.containerEnd();
            cjWriter.objectEnd();
        } else {
            CjType cjType = parseStack.peekCjType();
            if (cjType == null) {
                throw new IllegalArgumentException("Expected CJ type on stack, but found none.");
            }
            switch (cjType) {
                case Node -> cjWriter.nodeEnd();
                case Edge -> cjWriter.edgeEnd();
                case Endpoint -> cjWriter.endpointEnd();
                case Port -> cjWriter.portEnd();
                case Graph -> cjWriter.graphEnd();
                case ArrayOfLabelEntries -> cjWriter.labelEnd();
                case LabelEntry -> cjWriter.labelEntryEnd();
                case RootObject -> {
                    // do nothing
                }
            }
            parseStack.containerEnd();
        }
    }

    @Override
    public void objectStart() throws JsonException {
        if (parseStack.isInJson()) {
            parseStack.push(JsonType.Object);
            cjWriter.objectStart();
        } else {
            Set<CjType> expected = parseStack.expectedCjTypes();
            if (expected.isEmpty()) {
                throw new IllegalArgumentException("Expected CJ type on stack, but found none.");
            }
            CjType cjType = Util.findExactlyOne(expected, JsonType.Object);
            parseStack.push(cjType);
            switch (cjType) {
                case Graph -> cjWriter.graphStart();
                case Node -> cjWriter.nodeStart();
                case Port -> cjWriter.portStart();
                case Edge -> cjWriter.edgeStart();
                case Endpoint -> cjWriter.endpointStart();
                case ArrayOfLabelEntries -> cjWriter.labelStart();
                case LabelEntry -> cjWriter.labelEntryStart();
                case RootObject -> {
                    // do nothing
                }
            }
        }
    }

    @Override
    public void onBigDecimal(BigDecimal bigDecimal) {
        if (parseStack.isInJson()) {
            cjWriter.onBigDecimal(bigDecimal);
            parseStack.popJsonPropertyMaybe();
        } else {
            onCjNumber(bigDecimal.toString());
        }
    }

    @Override
    public void onBigInteger(BigInteger bigInteger) {
        if (parseStack.isInJson()) {
            cjWriter.onBigInteger(bigInteger);
            parseStack.popJsonPropertyMaybe();
        } else {
            onCjNumber(bigInteger.toString());
        }
    }

    @SuppressWarnings("SwitchStatementWithTooFewBranches")
    @Override
    public void onBoolean(boolean b) throws JsonException {
        if (parseStack.isInJson()) {
            cjWriter.onBoolean(b);
            parseStack.popJsonPropertyMaybe();
        } else {
            throw new IllegalStateException("Standard CJ has no booleans.");
        }
    }

    @Override
    public void onDouble(double d) throws JsonException {
        if (parseStack.isInJson()) {
            cjWriter.onDouble(d);
            parseStack.popJsonPropertyMaybe();
        } else {
            onCjNumber("" + d);
        }
    }

    @Override
    public void onFloat(float f) throws JsonException {
        if (parseStack.isInJson()) {
            cjWriter.onFloat(f);
            parseStack.popJsonPropertyMaybe();
        } else {
            onCjNumber("" + f);
        }
    }

    @Override
    public void onInteger(int i) throws JsonException {
        if (parseStack.isInJson()) {
            cjWriter.onInteger(i);
            parseStack.popJsonPropertyMaybe();
        } else {
            onCjNumber("" + i);
        }
    }

    @Override
    public void onKey(String key) throws JsonException {
        if (parseStack.isInJson()) {
            cjWriter.onKey(key);
        } else {
            CjType cjType = parseStack.peekCjType();
            String normKey = normalizeProperty(key);
            assert cjType != null;
            CjType.CjProperty prop = cjType.properties.get(normKey);
            if (prop == null) {
                // JSON property extensible -> an unknown property is generic JSON
                parseStack.stack.push(JsonType.Property);
            } else {
                parseStack.expect(prop.expected());
            }
        }
    }

    @Override
    public void onLong(long l) throws JsonException {
        if (parseStack.isInJson()) {
            cjWriter.onLong(l);
            parseStack.popJsonPropertyMaybe();
        } else {
            onCjNumber("" + l);
        }
    }

    @Override
    public void onNull() throws JsonException {
        if (parseStack.isInJson()) {
            cjWriter.onNull();
            parseStack.popJsonPropertyMaybe();
        } else {
            throw new IllegalStateException("No null values in CJ");
        }
    }


    public void reset() {
        parseStack.clear();
        parseStack.expectedCjTypes.clear();
        parseStack.expectedCjTypes.add(CjType.RootObject);
    }

    @Override
    public void stringCharacters(String s) throws JsonException {
        if (parseStack.isInJson()) {
            cjWriter.stringCharacters(s);
        } else {
            stringBuffer.append(s);
        }
    }

    @Override
    public void stringEnd() throws JsonException {
        if (parseStack.isInJson()) {
            parseStack.stack.pop();
            cjWriter.stringEnd();
        } else {
            CjType cjType = parseStack.expectedCjType(JsonType.String);
            switch (cjType) {
                case Id -> cjWriter.id(stringBuffer.toString());
                case LabelStringNoLanguage -> {
                    cjWriter.labelStart();
                    cjWriter.labelEntryStart();
                    cjWriter.value(stringBuffer.toString());
                    cjWriter.labelEntryEnd();
                    cjWriter.labelEnd();
                    parseStack.expectedCjTypes.clear();
                }
                case Language -> cjWriter.language(stringBuffer.toString());
                case Value -> cjWriter.value(stringBuffer.toString());
                case BaseUri -> cjWriter.baseUri(stringBuffer.toString());
                case Direction -> cjWriter.direction(CjDirection.valueOf(stringBuffer.toString()));
                default ->
                        throw new IllegalStateException("Unreasonable expectations. You expect " + cjType + " but why?");
            }
            stringBuffer.setLength(0);
        }

    }

    @Override
    public void stringStart() throws JsonException {
        if (parseStack.isInJson()) {
            parseStack.push(JsonType.String);
            cjWriter.stringStart();
        } else {
            parseStack.expectedCjType(JsonType.String);
            // reset string buffer
            stringBuffer.setLength(0);
        }
    }

    @Override
    public void whitespaceCharacters(String s) throws JsonException {
        // ignored in CJ for now
    }

    /** Normalize property names using aliases */
    private String normalizeProperty(String property) {
        return CjProperties.normalizeProperty(property);
    }

    @SuppressWarnings("SwitchStatementWithTooFewBranches")
    private void onCjNumber(String number) {
        CjType cjType = parseStack.expectedCjType(JsonType.Number);
        switch (cjType) {
            case CjType.Id -> cjWriter.id(number);
            default -> throw new IllegalStateException("Unreasonable expectations. You expect " + cjType + " but why?");
        }
        parseStack.stack.pop();
    }


}

package com.calpano.graphinout.base.cj;

import com.calpano.graphinout.foundation.json.JsonException;
import com.calpano.graphinout.foundation.json.JsonType;
import com.calpano.graphinout.foundation.json.stream.JsonWriter;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.slf4j.Logger;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import static org.slf4j.LoggerFactory.getLogger;

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
            expectedCjTypes.clear();
        }

        public void expect(Set<CjType> expected) {
            expectedCjTypes.clear();
            expectedCjTypes.addAll(expected);
        }

        public CjType expectedCjType(JsonType jsonType) {
            return CjType.findExactlyOne(expectedCjTypes(), jsonType);
        }

        public Set<CjType> expectedCjTypes() {
            // auto-add: we additionally always expect itemTypes, when in an array
            CjType cjType = peekCjType();
            if (cjType != null && cjType.itemTypes != null) {
                // what member type do we expect?
                expectedCjTypes.addAll(List.of(cjType.itemTypes));
            }

            return expectedCjTypes;
        }

        public boolean isInJson() {
            if (stack.isEmpty()) return false;
            return stack.peek() instanceof JsonType || stack.peek() == CjType.Data;
        }

        public @Nullable CjType peekCjType() {
            if (stack.isEmpty()) return null;
            Object cjOrJsonType = stack.peek();
            if (cjOrJsonType instanceof CjType) {
                return (CjType) cjOrJsonType;
            }
            return null;
        }

        public void popData() {
            if (!stack.isEmpty() && stack.peek() == CjType.Data) {
                stack.pop();
            }
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

    private static final Logger log = getLogger(Json2CjWriter.class);
    /** The sink for the converted events. */
    final CjWriter cjWriter;
    /** The current context from root: stack of Container and String (property keys). */
    private final ParseStack parseStack = new ParseStack();

    public Json2CjWriter(CjWriter cjWriter) {
        this.cjWriter = cjWriter;
    }

    public static Json2CjWriter createWritingTo(CjWriter cjWriter) {
        return new Json2CjWriter(cjWriter);
    }

    @Override
    public void arrayEnd() throws JsonException {
        if (parseStack.isInJson()) {
            cjWriter.arrayEnd();
            maybeEndData();
        } else {
            CjType cjType = parseStack.peekCjType();
            if(cjType == CjType.ArrayOfLabelEntries) {
                cjWriter.labelEnd();
            }
            cjWriter.listEnd(cjType);
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
            CjType cjType = CjType.findExactlyOne(expected, JsonType.Array);
            parseStack.push(cjType);
            if(cjType == CjType.ArrayOfLabelEntries) {
               cjWriter.labelStart();
            }
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
            // might also pop a Data object itself
            parseStack.containerEnd();
            cjWriter.objectEnd();
            maybeEndData();
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
                case Meta -> cjWriter.metaEnd();
                case ArrayOfLabelEntries -> cjWriter.labelEnd();
                case LabelEntry -> cjWriter.labelEntryEnd();
                case RootObject, ConnectedJson -> {
                    // do nothing
                }
                default -> throw new IllegalStateException("Unexpected CJ type " + cjType + " on stack.");
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
            CjType cjType = CjType.findExactlyOne(expected, JsonType.Object);
            parseStack.push(cjType);
            switch (cjType) {
                case Graph -> cjWriter.graphStart();
                case Node -> cjWriter.nodeStart();
                case Port -> cjWriter.portStart();
                case Edge -> cjWriter.edgeStart();
                case Endpoint -> cjWriter.endpointStart();
                case ArrayOfLabelEntries -> cjWriter.labelStart();
                case LabelEntry -> cjWriter.labelEntryStart();
                case Meta -> cjWriter.metaStart();
                case RootObject, ConnectedJson, Data -> {
                    // do nothing
                }
                default -> throw new IllegalStateException("Unexpected CJ type " + cjType + " on stack.");
            }
        }
    }

    @Override
    public void onBigDecimal(BigDecimal bigDecimal) {
        if (parseStack.isInJson()) {
            cjWriter.onBigDecimal(bigDecimal);
            parseStack.popJsonPropertyMaybe();
            maybeEndData();
        } else {
            onCjNumber(bigDecimal);
        }
    }

    @Override
    public void onBigInteger(BigInteger bigInteger) {
        if (parseStack.isInJson()) {
            cjWriter.onBigInteger(bigInteger);
            parseStack.popJsonPropertyMaybe();
            maybeEndData();
        } else {
            onCjNumber(bigInteger);
        }
    }

    @Override
    public void onBoolean(boolean b) throws JsonException {
        if (parseStack.isInJson()) {
            cjWriter.onBoolean(b);
            parseStack.popJsonPropertyMaybe();
            maybeEndData();
        } else {
            // are we in Canonical?
            if (parseStack.peekCjType() == CjType.Meta) {
                cjWriter.meta__canonical(b);
            } else {
                throw new IllegalStateException("Unexpected boolean value '" + b + "' in CJ.");
            }
        }
    }

    @Override
    public void onDouble(double d) throws JsonException {
        if (parseStack.isInJson()) {
            cjWriter.onDouble(d);
            parseStack.popJsonPropertyMaybe();
            maybeEndData();
        } else {
            onCjNumber(d);
        }
    }

    @Override
    public void onFloat(float f) throws JsonException {
        if (parseStack.isInJson()) {
            cjWriter.onFloat(f);
            parseStack.popJsonPropertyMaybe();
            maybeEndData();
        } else {
            onCjNumber(f);
        }
    }

    @Override
    public void onInteger(int i) throws JsonException {
        if (parseStack.isInJson()) {
            cjWriter.onInteger(i);
            parseStack.popJsonPropertyMaybe();
            maybeEndData();
        } else {
            onCjNumber(i);
        }
    }

    @Override
    public void onKey(String key) throws JsonException {
        if (parseStack.isInJson()) {
            cjWriter.onKey(key);
        } else {
            CjType cjType = parseStack.peekCjType();
            assert cjType != null;
            CjType.CjProperty prop = cjType.properties.get(key);
            if (prop == null) {
                log.warn("Unknown property {}.'{}' in CJ.", cjType, key);
                parseStack.stack.push(JsonType.Property);
            } else {
                if (key.equals("data")) {
                    cjWriter.jsonDataStart();
                    parseStack.push(CjType.Data);
                }
                parseStack.expect(prop.expected());
            }
        }
    }

    @Override
    public void onLong(long l) throws JsonException {
        if (parseStack.isInJson()) {
            cjWriter.onLong(l);
            parseStack.popJsonPropertyMaybe();
            maybeEndData();
        } else {
            onCjNumber(l);
        }
    }

    @Override
    public void onNull() throws JsonException {
        if (parseStack.isInJson()) {
            cjWriter.onNull();
            parseStack.popJsonPropertyMaybe();
            maybeEndData();
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
    public void onString(String s) throws JsonException {
        if (parseStack.isInJson()) {
            cjWriter.onString(s);
            parseStack.popJsonPropertyMaybe();
            maybeEndData();
        } else {
            // FIXME handle strings, if we expect them
            if( parseStack.expectedCjTypes.size() == 1) {
                // we know how to interpret it
                // FIXME interpret
                CjType expect = parseStack.expectedCjTypes.iterator().next();
                switch (expect) {
                    // document level
                    case JsonSchemaLocation, JsonSchemaId -> {
                        // TODO ...
                    }
                    case BaseUri -> cjWriter.baseUri(s);
                    case ConnectedJson__VersionDate -> {}
                    case ConnectedJson__VersionId -> {}
                    case Meta__Canonical -> cjWriter.meta__canonical(Boolean.parseBoolean(s));
                    case Meta__EdgeCountInGraph -> cjWriter.meta__edgeCountInGraph(Long.parseLong(s));
                    case Meta__EdgeCountTotal -> cjWriter.meta__edgeCountTotal(Long.parseLong(s));
                    case Meta__NodeCountInGraph -> cjWriter.meta__nodeCountInGraph(Long.parseLong(s));
                    case Meta__NodeCountTotal -> cjWriter.meta__nodeCountTotal(Long.parseLong(s));
                    // all
                    case Id -> cjWriter.id(s);
                    // label
                    case Language -> cjWriter.language(s);
                    case Value -> cjWriter.value(s);
                    // node, port
                    case NodeId -> cjWriter.nodeId(s);
                    case PortId -> cjWriter.portId(s);
                    // edge, endpoint
                    case Direction -> cjWriter.direction(CjDirection.of(s));
                    case EdgeTypeNodeId -> cjWriter.edgeType(CjEdgeType.of(CjEdgeTypeSource.Node, s));
                    case EdgeTypeString -> cjWriter.edgeType(CjEdgeType.of(CjEdgeTypeSource.String, s));
                    case EdgeTypeUri -> cjWriter.edgeType(CjEdgeType.of(CjEdgeTypeSource.URI, s));
                    default -> throw new UnsupportedOperationException("TODO implement string interpretation for " + expect + " in CJ.");
                }
            } else {
                throw new IllegalStateException("Ambiguous string value '" + s + "' in CJ. Expecting " + parseStack.expectedCjTypes() );
            }
        }
    }

    private void maybeEndData() {
        if (parseStack.peekCjType() == CjType.Data) {
            parseStack.popData();
            cjWriter.jsonDataEnd();
        }
    }

    private void onCjNumber(Number number) {
        CjType cjType = parseStack.expectedCjType(JsonType.Number);
        switch (cjType) {
            case CjType.Id -> cjWriter.id(number.toString());
            case CjType.Meta__NodeCountTotal -> cjWriter.meta__nodeCountTotal(number.longValue());
            case CjType.Meta__EdgeCountTotal -> cjWriter.meta__edgeCountTotal(number.longValue());
            case CjType.Meta__NodeCountInGraph -> cjWriter.meta__nodeCountInGraph(number.longValue());
            case CjType.Meta__EdgeCountInGraph -> cjWriter.meta__edgeCountInGraph(number.longValue());
            default -> throw new IllegalStateException("Unreasonable expectations. You expect " + cjType + " but why?");
        }
        //parseStack.stack.pop();
    }


}

package com.graphinout.reader.gml;

import com.graphinout.foundation.pure.input.ContentError;
import com.graphinout.foundation.pure.input.Location;
import org.jspecify.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.function.Consumer;
import java.util.function.IntSupplier;

public class Gml2GmlDataHandler implements IGmlHandler {

    /** placeholder key used to keep the tree balanced when a '[' appears without a preceding key */
    private static final String MISSING_KEY = "";

    private final GmlData doc = new GmlData();
    private final Deque<GmlData> stack = new ArrayDeque<>();
    private final @Nullable Consumer<ContentError> errorHandler;
    private @Nullable IntSupplier lineSupplier;
    private @Nullable String lastKey = null;

    public Gml2GmlDataHandler() {
        this(null);
    }

    public Gml2GmlDataHandler(@Nullable Consumer<ContentError> errorHandler) {
        this.errorHandler = errorHandler;
        stack.push(doc);
    }

    /** Supplies the current input line for error locations; typically {@code tokenizer::currentLine}. */
    public void setLineSupplier(@Nullable IntSupplier lineSupplier) {
        this.lineSupplier = lineSupplier;
    }

    @Override
    public void close() {
        // dangling key: a key directly followed by ']' has neither a value nor a nested list
        if (lastKey != null) {
            report(ContentError.ErrorLevel.Warn, "GML key '" + lastKey + "' has no value");
            lastKey = null;
        }
        // unbalanced ']' : never pop the root document off the stack
        if (stack.size() <= 1) {
            report(ContentError.ErrorLevel.Error, "Unbalanced ']' (no matching '[')");
            return;
        }
        stack.pop();
    }

    @Override
    public void key(String key) {
        lastKey = key;
    }

    @Override
    public void open() {
        GmlData peek = stack.peek();
        assert peek != null : "stack always retains the root document";
        String key = lastKey;
        if (key == null) {
            report(ContentError.ErrorLevel.Error, "GML '[' without a preceding key");
            key = MISSING_KEY;
        }
        GmlData gmlData = new GmlData();
        peek.add(key, gmlData);
        stack.push(gmlData);
        lastKey = null;
    }

    /** @return number of '[' that were opened but not yet closed (0 for a well-balanced document) */
    public int openDepth() {
        return stack.size() - 1;
    }

    public GmlData result() {
        return doc;
    }

    @Override
    public void value(String value) {
        GmlData peek = stack.peek();
        assert peek != null : "stack always retains the root document";
        if (lastKey == null) {
            report(ContentError.ErrorLevel.Warn, "GML value '" + value + "' without a preceding key");
            return;
        }
        peek.add(lastKey, value);
        lastKey = null;
    }

    private void report(ContentError.ErrorLevel level, String message) {
        if (errorHandler == null) {
            return;
        }
        int line = lineSupplier == null ? 0 : lineSupplier.getAsInt();
        errorHandler.accept(ContentError.of(level, message, Location.of(line, 1)));
    }

}

package com.graphinout.reader.gml;

import java.util.ArrayDeque;
import java.util.Deque;

public class Gml2GmlDataHandler implements IGmlHandler {

    private GmlData doc = new GmlData();
    private Deque<GmlData> stack = new ArrayDeque<>();
    private String lastKey = null;
    public Gml2GmlDataHandler() {
        stack.push(doc);
    }

    @Override
    public void close() {
        stack.pop();
    }

    @Override
    public void key(String key) {
        lastKey = key;
    }

    @Override
    public void open() {
        GmlData peek = stack.peek();
        assert peek != null;
        assert lastKey != null;

        GmlData gmlData = new GmlData();
        peek.add(lastKey, gmlData);
        stack.push(gmlData);
        lastKey = null;
    }

    public GmlData result() {
        return doc;
    }

    @Override
    public void value(String value) {
        GmlData peek = stack.peek();
        assert peek != null;
        assert lastKey != null;

        peek.add(lastKey, value);
        lastKey = null;
    }

}

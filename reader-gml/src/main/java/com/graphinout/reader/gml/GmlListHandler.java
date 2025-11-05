package com.graphinout.reader.gml;

import java.util.ArrayList;
import java.util.List;

/**
 * An {@link IGmlHandler} for testing. It records all calls into a list.
 */
public class GmlListHandler implements IGmlHandler {

    enum Bracket {Open, Close}

    record Key(String key) {

        @Override
        public String toString() {
            return "Key(" + key + ')';
        }

    }

    record Value(String value) {

        @Override
        public String toString() {
            return "Value(" + value + ')';
        }

    }

    private final List<Object> list = new ArrayList<>();

    @Override
    public void close() {
        list.add(Bracket.Close);
    }

    @Override
    public void key(String key) {
        list.add(new Key(key));
    }

    public List<Object> list() {
        return list;
    }

    @Override
    public void open() {
        list.add(Bracket.Open);
    }

    @Override
    public void value(String value) {
        list.add(new Value(value));
    }

}

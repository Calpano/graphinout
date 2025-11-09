package com.graphinout.reader.gml;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
        public boolean equals(Object o) {
            if (o instanceof Value(String other)) {
                @Nullable Number a = asNumber(value);
                @Nullable Number b = asNumber(other);
                if (a != null && b != null) {
                    return a.equals(b);
                }
                return Objects.equals(value, other);
            }
            return false;


        }

        @Override
        public int hashCode() {
            return Objects.hashCode(value());
        }

        @Override
        public String toString() {
            return "Value(" + value + ')';
        }

        private @Nullable Number asNumber(String s) {
            try {
                return Double.parseDouble(s);
            } catch (NumberFormatException e) {
                return null;
            }
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

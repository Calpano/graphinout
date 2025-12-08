package com.graphinout.foundation.pure.value;

/**
 * A String by reference, to be used with Java 8 lambdas
 */
public class StringRef extends ObjectRef<String> {

    public StringRef(final String value) {
        super(value);
    }

    public StringRef() {
        super(null);
    }

    public static StringRef empty() {
        return new StringRef();
    }

}

package com.graphinout.foundation.pure.json.query;

import com.graphinout.foundation.pure.json.document.IJsonValue;

/**
 * Addresses a set of (existing or non-existing) entries in an {@link IJsonValue}. NOTE: In examples we use '+' to
 * denote "*" so we can write this in a JavaDoc.
 * <h2>Path Examples</h2>
 * '/foo/bar/3/baz', '/foo/bar/+/baz', '/foo/+' '/foo/+/bar', '/foo/+/bar/+'. Numeric segments are treated as array
 * indices. '+' (i.e. '*') matches any object key or array index.
 *
 */
public class JsonPath {

    private final String path;

    /**
     * * @param path must start with '/'.
     */
    public JsonPath(String path) {
        this.path = path;
    }

    public String path() {
        return path;
    }

}

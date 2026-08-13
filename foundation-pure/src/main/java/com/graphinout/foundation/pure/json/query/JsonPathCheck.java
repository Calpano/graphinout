package com.graphinout.foundation.pure.json.query;

public class JsonPathCheck {

    private final JsonPath path;
    private final IJsonExpect expect;

    public JsonPathCheck(JsonPath path, IJsonExpect expect) {
        this.path = path;
        this.expect = expect;
    }

    public static JsonPathCheck of(String path, IJsonExpect expect) {
        return new JsonPathCheck(new JsonPath(path), expect);
    }

    public IJsonExpect expect() {
        return expect;
    }

    public JsonPath path() {
        return path;
    }

}

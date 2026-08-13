package com.graphinout.foundation.pure.json.query;

/**
 * A query consisting of one or more path tests that must <em>all</em> be satisfied.
 */
public class JsonQuery {

    final JsonPathCheck[] pathTests;

    public JsonQuery(JsonPathCheck... pathTests) {
        this.pathTests = pathTests;
    }

    /** Convenience factory: single path + expectation. */
    public static JsonQuery of(String path, IJsonExpect expect) {
        return new JsonQuery(new JsonPathCheck(new JsonPath(path), expect));
    }

    /** Convenience factory: require a property to be present. */
    public static JsonQuery requirePresent(String path) {
        return of(path, IJsonExpect.expectPresent());
    }

    /** Convenience factory: require a property to equal a specific string value. */
    public static JsonQuery requireString(String path, String expectedValue) {
        return of(path, IJsonExpect.expectStringValue(expectedValue));
    }

    public JsonPathCheck[] pathTests() {
        return pathTests;
    }

}

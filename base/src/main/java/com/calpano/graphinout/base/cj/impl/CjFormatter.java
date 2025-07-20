package com.calpano.graphinout.base.cj.impl;

public class CjFormatter {

    static final String HEADER = """
        "connectedJson":{"versionNumber":"5.0.0","versionDate":"2025-07-14"},""";

    static final String SCHEMA = """
            "$schema":"https://calpano.github.io/connected-json/_attachments/cj-schema.json","$id":"https://j-s-o-n.org/schema/connected-json/5.0.0",""";

    /**
     * @param cjJson
     * @return
     */
    public static String stripCjHeader(String cjJson) {
        String s = cjJson.replace(HEADER, "");
        s = s.replace(SCHEMA,"");
        return s;
    }

}

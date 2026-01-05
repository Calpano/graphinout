package com.graphinout.reader.ocif;

import com.graphinout.reader.ocif.document.IOcifDocument;

public class OcifDoc2Json {

    public static Object toJaJson(IOcifDocument ocifDocument) {
        return IOcifDocument.toJsonValue(ocifDocument).toJaJsonValue();
    }

    public static String toJsonString(IOcifDocument ocifDocument) {
        return IOcifDocument.toJsonValue(ocifDocument).toJsonString();
    }

}

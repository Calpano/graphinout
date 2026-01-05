package com.graphinout.reader.ocif.document;

import com.graphinout.foundation.pure.json.document.IJsonObject;

public interface IOcifSchemaMutable extends IOcifSchema {

    IOcifSchema location(String location);

    IOcifSchema name(String name);

    IOcifSchema schema(IJsonObject schema);

    IOcifSchema uri(String uri);

}

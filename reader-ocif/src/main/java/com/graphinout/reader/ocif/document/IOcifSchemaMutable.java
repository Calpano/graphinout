package com.graphinout.reader.ocif.document;

import com.graphinout.foundation.pure.json.document.IJsonObject;

public interface IOcifSchemaMutable extends IOcifSchema {

    IOcifSchema setLocation(String location);

    IOcifSchema setName(String name);

    IOcifSchema setSchema(IJsonObject schema);

    IOcifSchema setUri(String uri);

}

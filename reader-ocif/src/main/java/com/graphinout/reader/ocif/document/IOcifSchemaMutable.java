package com.graphinout.reader.ocif.document;

import com.graphinout.foundation.pure.json.document.IJsonObject;
import org.jspecify.annotations.Nullable;

public interface IOcifSchemaMutable extends IOcifSchema {

    IOcifSchema location(@Nullable String location);

    IOcifSchema name(@Nullable String name);

    IOcifSchema schema(@Nullable IJsonObject schema);

    IOcifSchema uri(String uri);

}

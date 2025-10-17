package com.graphinout.base.cj.element;

import com.graphinout.base.cj.stream.impl.Cj2JsonWriter;
import com.graphinout.foundation.json.stream.impl.Json2StringWriter;

import static com.graphinout.foundation.util.Nullables.ifPresentAccept;

public class CjDocuments {

    /**
     * Analyze a CJ doc and compute a {@link CjDataSchema} to represent all data.
     */
    public static CjDataSchema calcEffectiveSchemaForData(ICjDocument document) {
        CjDataSchema schema = new CjDataSchema();
        document.allElements().forEach(elem -> {
            if (elem instanceof ICjHasData hasData) {
                ifPresentAccept(hasData.data(), data -> schema.index(elem.cjType(), data));
            }
        });
        return schema;
    }

    public static String toJsonString(ICjDocument cjDoc) {
        Json2StringWriter jw = new Json2StringWriter();
        Cj2JsonWriter w = new Cj2JsonWriter(jw);
        cjDoc.fire(w);
        return jw.jsonString();
    }


}

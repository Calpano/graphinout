package com.calpano.graphinout.base.cj.element;

import static com.calpano.graphinout.foundation.util.Nullables.ifPresentAccept;

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

}

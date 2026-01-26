package com.graphinout.base.cj.document;

import com.graphinout.base.cj.writer.Cj2JsonWriter;
import com.graphinout.base.cj.writer.CjWriter2CjDocumentWriter;
import com.graphinout.base.cj.writer.Json2CjWriter;
import com.graphinout.base.input.SingleInputSource;
import com.graphinout.base.json.JsonReaderImpl;
import com.graphinout.foundation.pure.json.writer.impl.Json2StringWriter;
import org.jspecify.annotations.NonNull;

import java.io.IOException;

public class CjDocuments {

    /**
     * Analyze a CJ doc and compute a {@link CjDataSchema} to represent all data.
     */
    public static CjDataSchema calcEffectiveSchemaForData(ICjDocument document) {
        CjDataSchema schema = new CjDataSchema();
        document.allElements().forEach(elem -> {
            if (elem instanceof ICjHasData hasData) {
                if (hasData.data().isNotEmpty()) {
                    schema.index(elem.cjType(), hasData.data());
                }
            }
        });
        return schema;
    }

    public static @NonNull ICjDocumentMutable parseCjJsonString(String contentName, String content) throws IOException {
        SingleInputSource singleInputSource = SingleInputSource.of(contentName, content);
        CjWriter2CjDocumentWriter cj2elements = new CjWriter2CjDocumentWriter();
        Json2CjWriter json2Cj = new Json2CjWriter(cj2elements);
        JsonReaderImpl jsonReader = new JsonReaderImpl();
        jsonReader.read(singleInputSource, json2Cj);
        return cj2elements.resultDoc();
    }

    public static String toJsonString(ICjDocument cjDoc) {
        Json2StringWriter jw = new Json2StringWriter();
        Cj2JsonWriter w = new Cj2JsonWriter(jw);
        cjDoc.fire(w, true);
        return jw.jsonString();
    }


}

package com.graphinout.base.cj.element;

import com.graphinout.base.cj.stream.impl.CjStream2CjDocumentWriter;
import com.graphinout.base.cj.stream.impl.Cj2JsonWriter;
import com.graphinout.base.cj.stream.impl.Json2CjWriter;
import com.graphinout.foundation.input.SingleInputSource;
import com.graphinout.foundation.json.stream.impl.Json2StringWriter;
import com.graphinout.foundation.json.stream.impl.JsonReaderImpl;

import java.io.IOException;

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

    public static ICjDocument parseCjJsonString(String contentName, String content) throws IOException {
        SingleInputSource singleInputSource = SingleInputSource.of(contentName, content);
        CjStream2CjDocumentWriter cj2elements = new CjStream2CjDocumentWriter();
        Json2CjWriter json2Cj = new Json2CjWriter(cj2elements);
        JsonReaderImpl jsonReader = new JsonReaderImpl();
        jsonReader.read(singleInputSource, json2Cj);
        return cj2elements.resultDoc();
    }

    public static String toJsonString(ICjDocument cjDoc) {
        Json2StringWriter jw = new Json2StringWriter();
        Cj2JsonWriter w = new Cj2JsonWriter(jw);
        cjDoc.fire(w);
        return jw.jsonString();
    }


}

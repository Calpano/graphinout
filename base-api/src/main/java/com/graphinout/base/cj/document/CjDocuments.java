package com.graphinout.base.cj.document;

import com.graphinout.base.cj.writer.Cj2JsonWriter;
import com.graphinout.base.cj.writer.CjWriter2CjDocumentWriter;
import com.graphinout.base.cj.writer.Json2CjWriter;
import com.graphinout.base.input.SingleInputSource;
import com.graphinout.base.json.JsonReaderImpl;
import com.graphinout.foundation.pure.json.writer.impl.Json2StringWriter;
import com.graphinout.foundation.pure.input.ContentError;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.util.function.Consumer;

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

    /** Parse CJ; problems such as dropped unknown keys are not reported. See {@link #parseCjJsonString(String, String, Consumer)}. */
    public static @NonNull ICjDocumentMutable parseCjJsonString(String contentName, String content) throws IOException {
        return parseCjJsonString(contentName, content, null);
    }

    /**
     * Parse CJ.
     *
     * @param contentErrorHandler receives content problems, e.g. one warning per unknown (and dropped) key, with its
     *                            path such as {@code graphs[0].nodes[3].foo}
     */
    public static @NonNull ICjDocumentMutable parseCjJsonString(String contentName, String content, @Nullable Consumer<ContentError> contentErrorHandler) throws IOException {
        SingleInputSource singleInputSource = SingleInputSource.of(contentName, content);
        CjWriter2CjDocumentWriter cj2elements = new CjWriter2CjDocumentWriter();
        Json2CjWriter json2Cj = new Json2CjWriter(cj2elements);
        if (contentErrorHandler != null) {
            json2Cj.setContentErrorHandler(contentErrorHandler);
        }
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

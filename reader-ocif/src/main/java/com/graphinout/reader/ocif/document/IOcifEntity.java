package com.graphinout.reader.ocif.document;

import com.graphinout.foundation.pure.annotations.Since;
import com.graphinout.foundation.pure.json.document.IJsonValue;
import com.graphinout.reader.ocif.document.extension.IOcifExtension;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Set;

/** Abstract base type for all extensible entities in the OCIF document (extension data and comments). */
@Since("OCIF 0.6.1")
public interface IOcifEntity extends IDecorateJsonObject {

    @Since("OCIF 0.6.1")
    default @Nullable String comment() {
        IJsonValue comment = map().get("comment");
        return comment == null ? null : comment.asString();
    }

    /** These keys are handled/interpreted. SHOULD include COMMENT and DATA */
    @Override
    Set<String> definedKeys();

    @NonNull List<? extends IOcifExtension> extensions();

}

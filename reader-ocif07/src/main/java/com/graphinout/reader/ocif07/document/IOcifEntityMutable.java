package com.graphinout.reader.ocif07.document;

import com.graphinout.foundation.pure.annotations.Since;
import com.graphinout.reader.ocif07.document.extension.IOcifExtension;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.Set;

import static com.graphinout.reader.ocif07.OCIF.Common.COMMENT;

/** Abstract base type for all extensible entities in the OCIF document (extension data and comments). */
@Since("OCIF 0.6.1")
public interface IOcifEntityMutable extends IDecorateJsonObjectMutable, IOcifEntity {

    @Since("OCIF 0.6.1")
    default IOcifEntityMutable comment(String comment) {
        set(COMMENT, comment);
        return this;
    }

    /** These keys are handled/interpreted. SHOULD include COMMENT and DATA */
    @Override
    Set<String> definedKeys();

    @NonNull List<? extends IOcifExtension> extensions();

}

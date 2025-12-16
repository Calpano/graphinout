package com.graphinout.reader.ocif.document;

import com.graphinout.reader.ocif.document.extension.IOcifExtension;
import org.jspecify.annotations.NonNull;

public interface IOcifRelationMutable extends IOcifRelation {

    IOcifRelationMutable addExtension(@NonNull IOcifExtension ext);

    IOcifRelationMutable setId(String id);

    IOcifRelationMutable setNode(String node);

}

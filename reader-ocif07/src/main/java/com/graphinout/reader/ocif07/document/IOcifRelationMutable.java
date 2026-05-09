package com.graphinout.reader.ocif07.document;

import com.graphinout.reader.ocif07.document.extension.relation.IOcifRelationExtension;
import org.jspecify.annotations.NonNull;

public interface IOcifRelationMutable extends IOcifRelation, IOcifItemMutable {

    IOcifRelationMutable addExtension(@NonNull IOcifRelationExtension ext);

    @Override
    IOcifRelationMutable id(String id);

    IOcifRelationMutable node(String node);

}

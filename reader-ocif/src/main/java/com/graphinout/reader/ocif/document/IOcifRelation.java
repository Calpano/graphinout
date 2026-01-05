package com.graphinout.reader.ocif.document;

import com.graphinout.foundation.pure.json.document.IJsonArrayMutable;
import com.graphinout.foundation.pure.json.document.IJsonObjectMutable;
import com.graphinout.foundation.pure.json.document.IJsonValue;
import com.graphinout.reader.ocif.OCIF;
import com.graphinout.reader.ocif.document.extension.relation.IOcifRelationExtension;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.List;

import static com.graphinout.reader.ocif.Ocifs.factory;

/**
 * OCIF Relation.
 * <p>
 * The spec.md section "Relations" describes the various core and extension relations (edge, parent-child, hyperedge,
 * group).
 */
public interface IOcifRelation extends IOcifEntity, IOcifItem {

    static IJsonValue relationToJson(IOcifRelation relation) {
        IJsonObjectMutable relationJson = factory().createObjectMutable();
        relationJson.setProperty(OCIF.Common.ID, factory().createString(relation.id()));
        if (relation.node() != null) {
            relationJson.setProperty(OCIF.Relation.NODE, factory().createString(relation.node()));
        }

        if (!relation.extensions().isEmpty()) {
            IJsonArrayMutable extensionsArray = factory().createArrayMutable();
            relation.extensions().forEach(extension -> //
                    extensionsArray.add(extension.toJson()));
            relationJson.setProperty(OCIF.Common.DATA, extensionsArray);
        }
        return relationJson;
    }

    /** Typed extensions parsed from the relation's data array. */
    @Override
    @NonNull List<IOcifRelationExtension> extensions();

    /** id (string, required): A unique identifier for the relation. */
    @Override
    String id();

    /** visual node representing relation */
    @Nullable String node();

}

package com.graphinout.reader.ocif.document;

import com.graphinout.reader.ocif.document.extension.IOcifExtension;
import org.jspecify.annotations.Nullable;

import java.util.List;

/**
 * OCIF Relation.
 * <p>
 * The spec.md section "Relations" describes the various core and extension relations (edge, parent-child, hyperedge,
 * group).
 */
public interface IOcifRelation extends IOcifExtensibleEntity {

    /** Typed extensions parsed from the relation's data array. */
    List<IOcifExtension> extensions();

    /** id (string, required): A unique identifier for the relation. */
    String id();

    /** visual node representing relation */
    @Nullable String node();

}

package com.graphinout.reader.ocif.document;

import com.graphinout.reader.ocif.document.extension.IOcifExtension;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public interface IOcifDocument {

    /** Canvas-level extensions, e.g., viewport. */
    List<IOcifExtension> canvasExtensions();

    default Optional<IOcifNode> findNode(String id) {
        if (id == null) return Optional.empty();
        return nodes().stream().filter(n -> id.equals(n.id())).findFirst();
    }

    default Optional<IOcifRelation> findRelation(String id) {
        if (id == null) return Optional.empty();
        return relations().stream().filter(r -> id.equals(r.id())).findFirst();
    }

    default Optional<IOcifResource> findResource(String id) {
        if (id == null) return Optional.empty();
        return resources().stream().filter(r -> id.equals(r.id())).findFirst();
    }

    /** Mutable nodes list */
    List<IOcifNode> nodes();

    /** TYPE_URI of the OCIF schema (root property "ocif"). */
    @NonNull String ocifSchemaURI();

    /** Mutable relations list */
    List<IOcifRelation> relations();

    /** Mutable resources list */
    List<IOcifResource> resources();

    @Nullable IOcifNode rootNode();

    /** Mutable schemas list */
    List<IOcifSchema> schemas();

}

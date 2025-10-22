package com.graphinout.reader.ocif.document;

import java.util.ArrayList;
import java.util.List;

/**
 * OCIF Resource.
 * <p>
 * Spec excerpts (schema.json $defs.resource):
 * <ul>
 *   <li>id (string, required): A unique identifier for the resource.</li>
 *   <li>representations (array, required): A list of {@link OcifRepresentation}.</li>
 * </ul>
 * See spec.md section "Assets > Resources" for semantics and fallback behavior.
 */
public class OcifResource {

    private String id;
    private final List<OcifRepresentation> representations = new ArrayList<>();

    public String getId() { return id; }
    public OcifResource setId(String id) { this.id = id; return this; }

    public List<OcifRepresentation> getRepresentations() { return representations; }

    public OcifResource addRepresentation(OcifRepresentation rep) { this.representations.add(rep); return this; }
}

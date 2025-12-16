package com.graphinout.reader.ocif.document;

import com.graphinout.foundation.pure.json.document.IJsonArray;
import com.graphinout.foundation.pure.json.document.IJsonObject;
import com.graphinout.reader.ocif.document.extension.IOcifExtension;

import java.util.List;

/**
 * OCIF Node.
 * <p>
 * Spec excerpts (schema.json $defs.node):
 * <ul>
 *   <li>id (string, required): A unique identifier for the node.</li>
 *   <li>position (array of number): Coordinate as (x,y) or (x,y,z).</li>
 *   <li>size (array of number): The size of the node per dimension.</li>
 *   <li>resource (string): The resource to display.</li>
 *   <li>resourceFit (enum): Fitting resource in item; one of none, containX, containY, contain, cover, fill, tile.</li>
 *   <li>data (array): Extended node data.</li>
 *   <li>rotation (number): +/- 360 degrees.</li>
 *   <li>relation (string): ID of a relation.</li>
 * </ul>
 * The spec.md section "Nodes" describes the semantics in more detail.
 */
public interface IOcifNode {

    enum ResourceFit {none, containX, containY, contain, cover, fill, tile}

    IJsonArray data();

    /** Typed extensions parsed from the node's data array. */
    List<IOcifExtension> extensions();

    IJsonObject extras();

    String id();

    double[] position();

    String relation();

    String resource();

    ResourceFit resourceFit();

    Double rotation();

    double[] size();

}

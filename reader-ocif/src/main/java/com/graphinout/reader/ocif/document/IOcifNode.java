package com.graphinout.reader.ocif.document;

import com.graphinout.foundation.pure.json.document.IJsonObjectMutable;
import com.graphinout.foundation.pure.json.document.IJsonValue;
import com.graphinout.reader.ocif.OCIF;
import com.graphinout.reader.ocif.document.extension.IOcifExtension;
import com.graphinout.reader.ocif.document.extension.node.IOcifNodeExtension;
import com.graphinout.reader.ocif.document.types.OcifAngle;
import com.graphinout.reader.ocif.document.types.OcifVector23D;
import org.jspecify.annotations.NonNull;

import java.util.List;

import static com.graphinout.foundation.pure.functional.Nullables.ifPresentAccept;
import static com.graphinout.reader.ocif.Ocifs.factory;

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
public interface IOcifNode extends IOcifItem {

    enum ResourceFit {none, containX, containY, contain, cover, fill, tile}

    static IJsonValue nodeToJson(IOcifNode ocifNode) {
        IJsonObjectMutable nodeJson = factory().createObjectMutable();
        nodeJson.setString(OCIF.Common.ID, ocifNode.id());
        ifPresentAccept(ocifNode.position(), p -> nodeJson.setProperty(OCIF.Node.POSITION, p.toJson()));
        ifPresentAccept(ocifNode.size(), s -> nodeJson.setProperty(OCIF.Node.SIZE, s.toJson()));

        ifPresentAccept(ocifNode.resource(), r -> nodeJson.setString(OCIF.Node.RESOURCE, r));

        ifPresentAccept(ocifNode.resourceFit(), r -> nodeJson.setString(OCIF.Node.RESOURCE_FIT, r.name()));
        ifPresentAccept(ocifNode.rotation(), r -> nodeJson.setProperty(OCIF.Node.ROTATION, r.toJson()));
        ifPresentAccept(ocifNode.relation(), r -> nodeJson.setString(OCIF.Node.RELATION, r));
        ifPresentAccept(ocifNode.extensions(), exts -> //
                nodeJson.setArray(OCIF.Common.DATA, array -> exts.forEach(ext -> //
                        array.add(IOcifExtension.extensionToJson(ext)))));
        return nodeJson;
    }

    /** Typed extensions parsed from the node's data array. */
    @NonNull List<IOcifNodeExtension> extensions();

    OcifVector23D position();

    String relation();

    String resource();

    ResourceFit resourceFit();

    OcifAngle rotation();

    OcifVector23D size();

}

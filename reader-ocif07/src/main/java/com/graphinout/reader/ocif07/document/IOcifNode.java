package com.graphinout.reader.ocif07.document;

import com.graphinout.foundation.pure.json.document.IJsonObjectMutable;
import com.graphinout.foundation.pure.json.document.IJsonValue;
import com.graphinout.reader.ocif07.OCIF;
import com.graphinout.reader.ocif07.document.extension.node.IOcifNodeExtension;
import com.graphinout.reader.ocif07.document.types.OcifAngle;
import com.graphinout.reader.ocif07.document.types.OcifVector23D;
import org.jspecify.annotations.NonNull;

import java.util.List;

import static com.graphinout.foundation.pure.functional.Nullables.ifPresentAccept;
import static com.graphinout.reader.ocif07.Ocifs.factory;

/**
 * OCIF Node.
 * <p>
 * Spec excerpts (schema.json $defs.node):
 * <ul>
 *   <li>id (string, required): A unique identifier for the node.</li>
 *   <li>parent (string): ID of a parent node (hierarchical, local coordinate system).</li>
 *   <li>deleteWithParent (boolean): Delete when parent is deleted. Default true.</li>
 *   <li>position (array of number): Coordinate as (x,y) or (x,y,z).</li>
 *   <li>size (array of number): The size of the node per dimension.</li>
 *   <li>rotation (number): +/- 360 degrees.</li>
 *   <li>rotationAxis (number[3]): Rotation axis. Default [0,0,1].</li>
 *   <li>scale (number|number[]): Scale factor(s). Default 1.</li>
 *   <li>resource (string): The resource to display.</li>
 *   <li>resourceFit (enum): Fitting resource in item; one of none, containX, containY, contain, cover, fill, tile.</li>
 *   <li>data (array): Extended node data.</li>
 *   <li>relation (string): ID of a relation.</li>
 * </ul>
 * The spec.md section "Nodes" describes the semantics in more detail.
 */
public interface IOcifNode extends IOcifItem {

    enum ResourceFit {none, containX, containY, contain, cover, fill, tile}

    static IJsonValue nodeToJson(IOcifNode ocifNode) {
        IJsonObjectMutable nodeJson = factory().createObjectMutable();
        nodeJson.setString(OCIF.Common.ID, ocifNode.id());
        ifPresentAccept(ocifNode.parent(), p -> nodeJson.setString(OCIF.Node.PARENT, p));
        if (Boolean.FALSE.equals(ocifNode.deleteWithParent())) {
            nodeJson.setBoolean(OCIF.Node.DELETE_WITH_PARENT, false);
        }
        ifPresentAccept(ocifNode.position(), p -> nodeJson.setProperty(OCIF.Node.POSITION, p.toJson()));
        ifPresentAccept(ocifNode.size(), s -> nodeJson.setProperty(OCIF.Node.SIZE, s.toJson()));
        ifPresentAccept(ocifNode.rotation(), r -> nodeJson.setProperty(OCIF.Node.ROTATION, r.toJson()));
        ifPresentAccept(ocifNode.rotationAxis(), ra -> nodeJson.setProperty(OCIF.Node.ROTATION_AXIS, ra.toJson()));
        ifPresentAccept(ocifNode.scale(), sc -> nodeJson.setProperty(OCIF.Node.SCALE, sc.toJson()));
        ifPresentAccept(ocifNode.resource(), r -> nodeJson.setString(OCIF.Node.RESOURCE, r));
        ifPresentAccept(ocifNode.resourceFit(), r -> nodeJson.setString(OCIF.Node.RESOURCE_FIT, r.name()));
        ifPresentAccept(ocifNode.relation(), r -> nodeJson.setString(OCIF.Node.RELATION, r));
        ifPresentAccept(ocifNode.extensions(), exts -> //
                nodeJson.setArray(OCIF.Common.DATA, array -> exts.forEach(ext -> //
                        array.add(ext.toJson()))));
        return nodeJson;
    }

    /** Typed extensions parsed from the node's data array. */
    @NonNull List<IOcifNodeExtension> extensions();

    Boolean deleteWithParent();

    String parent();

    OcifVector23D position();

    String relation();

    String resource();

    ResourceFit resourceFit();

    OcifAngle rotation();

    OcifVector23D rotationAxis();

    OcifVector23D scale();

    OcifVector23D size();

}

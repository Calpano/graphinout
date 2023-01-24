package com.calpano.graphinout.reader.graphml;

import com.calpano.graphinout.base.*;
import com.calpano.graphinout.base.gio.GioData;
import com.calpano.graphinout.base.gio.GioEndpoint;
import com.calpano.graphinout.base.gio.GioGraph;
import com.calpano.graphinout.base.gio.GioGraphCommonElement;
import com.calpano.graphinout.base.gio.GioNode;
import com.calpano.graphinout.base.gio.GioPort;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author rbaba
 * @version 0.0.1
 * @implNote Edges in the graph are declared by the edge element.
 * Each edge must define its two endpoints with the XML-Attributes source and target.
 * The value of the source, resp. target, must be the identifier of a node in the same document.
 * <p>
 * Edges with only one endpoint, also called loops, selfloops, or reflexive edges, are defined by having the same value for source and target.
 * <p>
 * The optional XML-Attribute directed declares if the edge is directed or undirected.
 * The value true declares a directed edge, the value false an undirected edge.
 * If the direction is not explicitely defined, the default direction is applied to this edge as defined in the enclosing graph.
 * <p>
 * Optionally an identifier for the edge can be specified with the XML Attribute id.
 * When it is necessary to reference the edge, the id XML-Attribute is used.
 * <p>
 * <b>In this class model, edge is used to read the file and then it is directly converted to HyperEdge .
 * It means that edge does not exist in the output file</b>
 * @see com.calpano.graphinout.base.gio.GioEdge {@link com.calpano.graphinout.base.gio.GioEdge}
 */

@AllArgsConstructor
@Data
@NoArgsConstructor
@Builder
class GioEdge extends GioGraphCommonElement {

    /**
     * This is an attribute that can be empty or null.
     * </p>
     * The name of this attribute in graph is <b>id</b>
     */
    protected String id;

    /**
     * This is an attribute that can be true or false or null ot empty.
     * The optional XML-Attribute directed declares if the edge is directed or undirected.
     * The value true declares a directed edge, the value false an undirected edge.
     * If the direction is not explicitely defined, the default direction is applied to this edge as defined in the enclosing graph.
     * </p>
     * The name of this attribute in graph is <b>directed</b>
     */
    protected Boolean directed;

    /**
     * This is an Element that can be empty or null.
     * </p>
     * The name of this Element in graph is <b>data</b>
     */
    protected List<GioData> data;

    /**
     * This is an attribute that can be empty or null.
     * </p>
     * The name of this attribute in graph is <b>source</b>
     * which points to a node and the ID of the desired node is the value of this attribute.
     */
    protected GioNode source;

    /**
     * This is an attribute that can be empty or null.
     * </p>
     * The name of this attribute in graph is <b>target</b>
     * which points to a node and the ID of the desired node is the value of this attribute.
     */
    protected GioNode target;

    /**
     * This is an attribute that can be empty or null.
     * </p>
     * The name of this attribute in graph is <b>port</b>
     * which points to a port and the ID of the desired port is the value of this attribute.
     */
    protected GioPort port;

    /**
     * This is an attribute that can be empty or null.
     * </p>
     * The name of this attribute in graph is <b>sourceport</b>
     * which points to a port and the ID of the desired port is the value of this attribute.
     */
    protected GioPort sourcePort;

    /**
     * This is an attribute that can be empty or null.
     * </p>
     * The name of this attribute in graph is <b>targetport</b>
     * which points to a port and the ID of the desired port is the value of this attribute.
     */
    protected GioPort targetPort;

    /**
     * This is an Element that can be empty or null.
     * </p>
     * The name of this Element in edge is <b>graph</b>.
     */
    private GioGraph graph;


    com.calpano.graphinout.base.gio.GioEdge hyperEdge() {
        com.calpano.graphinout.base.gio.GioEdge gioHyperEdge = new com.calpano.graphinout.base.gio.GioEdge();
        gioHyperEdge.setId(id);
        GioEndpoint gioEndpoint1 = new GioEndpoint();

        if (source != null) {
            gioEndpoint1.setNode(source.getId());
            gioEndpoint1.setType(Direction.In);
        }
        if (sourcePort != null) {
            gioEndpoint1.setPort(sourcePort.getName());
        }
        GioEndpoint gioEndpoint2 = new GioEndpoint();
        if (target != null) {
            gioEndpoint2.setNode(target.getId());
            gioEndpoint2.setType(Direction.Out);
        }
        if (targetPort != null) {
            gioEndpoint2.setPort(targetPort.getName());
        }
        gioHyperEdge.addEndpoint(gioEndpoint1);
        gioHyperEdge.addEndpoint(gioEndpoint2);
        return gioHyperEdge;
    }
}

package com.calpano.graphinout.base;

import com.calpano.graphinout.base.util.GIOUtil;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author rbaba
 * @version 0.0.1
 * @implNote Hyperedges are a generalization of edges in the sense that they do not only relate two endpoints to each other,
 * they express a relation between an arbitrary number of enpoints.
 * Hyperedges are declared by a hyperedge element in GraphML.
 * For each enpoint of the hyperedge, this hyperedge element contains an endpoint element.
 * The endpoint element must have an XML-Attribute node, which contains the identifier of a node in the document.
 * Note that edges can be either specified by an edge element or by a hyperedge element containing two endpoint elements.
 * @see GioHyperEdge {@link GioHyperEdge}
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GioEndpoint implements XMLValue {

    /**
     * This is an attribute that can be empty or null.
     * </p>
     * The name of this attribute in endpoint is <b>id</b>
     */
    private String id;

    /**
     * This is an attribute is optional but dependent to port.
     * In fact, one of the node or port values should be initialized.
     * </p>
     * The name of this attribute in endpoint is <b>node</b>
     * The value of this attribute points to an existing  node, and the ID of the desired node must be stored in this field.
     */
    private String node;

    /**
     * This is an attribute is optional but dependent to node.
     * In fact, one of the node or port values should be initialized.
     * </p>
     * The name of this attribute in endpoint is <b>port</b>
     * The value of this attribute points to an existing  port, and the name of the desired port must be stored in this field.
     */
    private String port;

    /**
     * Defines the attribute for direction on this endpoint (undirected per default).
     * <p>
     * The name of this attribute in endpoint is <b>type</b>
     */
    @Builder.Default
    private Direction type = Direction.Undirected;
    /**
     * This ia an Element That can be empty or null.
     * <p>
     * The name of this element in endpoint is <b>desc</b>
     */
    private GioDescription desc;


    @Override
    public String startTag() {
        LinkedHashMap<String, String> attributes = new LinkedHashMap<>();

        if (id != null) attributes.put("id", id);

        if (node != null) attributes.put("node", node);

        if (port != null) attributes.put("port", port);

        attributes.put("type", type.name());

        if (desc == null) return GIOUtil.makeElement("endpoint", attributes);
        else return GIOUtil.makeStartElement("endpoint", attributes);

    }

    @Override
    public String valueTag() {
        StringBuilder stringBuilder = new StringBuilder();
        if (desc != null) {
            stringBuilder.append(desc.fullTag());
        }
        return stringBuilder.toString();
    }

    @Override
    public String endTag() {
        if (desc == null) return "";
        return GIOUtil.makeEndElement("endpoint");
    }
}

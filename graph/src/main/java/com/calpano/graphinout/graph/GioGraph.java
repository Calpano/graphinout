package com.calpano.graphinout.graph;

import java.util.List;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlID;
import jakarta.xml.bind.annotation.XmlType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Singular;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "graph")

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GioGraph {

    @XmlAttribute(name = "edgedefault")
    protected String edgedefault;
    @XmlID
    @XmlAttribute(name = "id")
    protected String id;
    @XmlElement(name = "node")
    @Singular(ignoreNullCollections = true)
    private List<GioNode> nodes;
    @XmlElement(name = "edge", required = true)
    protected List<GioEdge> edges;
    @XmlElement(name = "hyperedge")
    @Singular(ignoreNullCollections = true)
    protected List<GioHyperEdge> hyperEdges;

}

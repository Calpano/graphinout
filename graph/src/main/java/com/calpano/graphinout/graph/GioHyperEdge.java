package com.calpano.graphinout.graph;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlID;
import jakarta.xml.bind.annotation.XmlType;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Singular;

/**
 *
 * @author rbaba
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "hyperedge")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GioHyperEdge {

    @XmlID
    @XmlAttribute
    protected String id;
    @XmlElement(name = "endpoint")
    @Singular(ignoreNullCollections = true)
    private List<GioEndpoint> endpoints;
}

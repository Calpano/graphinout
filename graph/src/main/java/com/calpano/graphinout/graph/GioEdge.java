package com.calpano.graphinout.graph;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlID;
import jakarta.xml.bind.annotation.XmlIDREF;
import jakarta.xml.bind.annotation.XmlType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *
 * @author rbaba
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "edge")
@AllArgsConstructor
@Data
@NoArgsConstructor
@Builder
public class GioEdge {

    @XmlID
    @XmlAttribute
    protected String id;
    @XmlElement(required = true)
    protected GioEdgeData data;
    @XmlIDREF
    @XmlAttribute(name = "source", required = true)
    protected GioNode source;
    @XmlIDREF
    @XmlAttribute(name = "target", required = true)
    protected GioNode target;
    @XmlAttribute(name = "directed")
    protected String directed;
    @XmlIDREF
    @XmlAttribute
    protected GioPort port;
    @XmlIDREF
    @XmlAttribute(name = "sourceport")
    protected GioPort sourcePort;
    @XmlIDREF
    @XmlAttribute(name = "targetport")
    protected GioPort targetPort;
}

package com.calpano.graphinout.graph;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlID;
import jakarta.xml.bind.annotation.XmlIDREF;
import jakarta.xml.bind.annotation.XmlType;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Singular;

/**
 * @author rbaba
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "node")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GioNode {

    // TODO allow multiple data elements
    @XmlElement
    protected GioNodeData data;
    @XmlID
    @XmlAttribute(name = "id", required = true)
    protected String id;

    @XmlIDREF
    @XmlElement(name = "port")
    @Singular(ignoreNullCollections = true)
    protected List<GioPort> ports;
}

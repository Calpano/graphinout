package com.calpano.graphinout.graph;

import java.util.List;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlID;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Singular;

@XmlAccessorType(XmlAccessType.FIELD)
//
@XmlRootElement(name = "graphml")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GioGraphML {

    @XmlID
    @XmlAttribute
    protected String id;

    @XmlElement(name = "key")
    @Singular(ignoreNullCollections = true)
    protected List<GioKey> keys;

    @XmlElement(name = "graph")
    @Singular(ignoreNullCollections = true)
    protected List<GioGraph> graphs;

}

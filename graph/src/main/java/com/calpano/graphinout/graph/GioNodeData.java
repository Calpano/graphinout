package com.calpano.graphinout.graph;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlID;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.XmlValue;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author rbaba
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "giodatastring")
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class GioNodeData {

    @XmlValue
    protected String value;
    @XmlID
    @XmlAttribute(name = "key")
    private String key;

}

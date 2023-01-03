package com.calpano.graphinout.graph;

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

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "key")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GioKey {

    @XmlAttribute(name = "attr.name", required = true)
    protected String attrName;
    @XmlAttribute(name = "attr.type", required = true)
    protected String attrType;
    @XmlID
    @XmlAttribute(name = "id", required = true)
    protected String id;
    @XmlElement(name = "default")
    private String defaultValue;

}

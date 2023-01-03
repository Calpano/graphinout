/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.calpano.graphinout.graph;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
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
@XmlType(name = "endpoint")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GioEndpoint {

    @XmlID
    @XmlAttribute
    protected String id;
    @XmlIDREF
    private GioNode node;
    @XmlIDREF
    @XmlAttribute
    protected GioPort port;
    @XmlAttribute
    protected String type;
    
}

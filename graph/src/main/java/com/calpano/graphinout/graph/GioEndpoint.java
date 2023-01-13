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
 * @author rbaba
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
// TODO remove all @Xml annotations from model
public class GioEndpoint {

    protected String id;
    // TODO we should refer to nodes by Id to make stream processing easier
    // suggested: String nodeId;
    private GioNode node;
    // TODO make optional
    protected GioPort port;

    // TODO make enum
    protected String type;

    // TODO add data list

}

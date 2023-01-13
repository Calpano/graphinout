package com.calpano.graphinout.graph;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlID;
import jakarta.xml.bind.annotation.XmlType;

import java.io.IOException;
import java.io.Writer;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Singular;

/**
 * Example:
 * <pre>
 *         <hyperedge id="id--hyperedge-4N56">
 *             <endpoint node="id--node4" type="in" port="North"/>
 *             <endpoint node="id--node5" type="in"/>
 *             <endpoint node="id--node6" type="in"/>
 *         </hyperedge>
 * </pre>
 *
 * @author rbaba
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GioHyperEdge {

    protected String id;
    @Singular(ignoreNullCollections = true)
    private List<GioEndpoint> endpoints;

    public void toXML(Writer w) throws IOException {
        w.write("<hyperedge id=");
        w.write(id);
        // TODO ... maybe like this?
    }

}

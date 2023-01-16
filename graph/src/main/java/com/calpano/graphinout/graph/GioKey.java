package com.calpano.graphinout.graph;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author rbaba
 * @version 0.0.1
 * @implNote A GraphML-Attribute is defined by a key element which specifies the identifier, name, type and domain of the attribute.
 * <p>
 * The name of the GraphML-Attribute is defined by the XML-Attribute attr.name and must be unique among all GraphML-Attributes declared in the document.
 * The purpose of the name is that applications can identify the meaning of the attribute.
 * Note that the name of the GraphML-Attribute is not used inside the document, the identifier is used for this purpose.
 * <p>
 * The type of the GraphML-Attribute can be either boolean, int, long, float, double, or string.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GioKey {

    /**
     * The name of the GraphML-Attribute is defined by the XML-Attribute attr.name and must be unique among all GraphML-Attributes declared in the document.
     * <p/>
     * * The name of this attribute in graphMl is <b>attr.name</b>
     */
    protected String attrName;
    /**
     * The name of this attribute in graphMl is <b>attr.type</b>
     */
    protected String attrType;
    /**
     * The name of this attribute in graphMl is <b>id</b>
     */
    protected String id;
    /**
     * * The name of this attribute in graphMl is <b>default</b>
     */
    private String defaultValue;
}

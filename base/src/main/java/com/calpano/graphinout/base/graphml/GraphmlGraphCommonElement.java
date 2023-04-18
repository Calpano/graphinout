package com.calpano.graphinout.base.graphml;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

// IMPROVE split into GraphmlElementWithData and GraphmlElementWithDesc
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class GraphmlGraphCommonElement extends GraphmlElement {
    /**
     * This ia an Element That can be empty or null.
     * <p>
     * The name of this element in graph is <b>desc</b>
     */
    protected GraphmlDescription desc;

}

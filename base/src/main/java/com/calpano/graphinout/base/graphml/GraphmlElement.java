package com.calpano.graphinout.base.graphml;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.HashMap;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class GraphmlElement {

    /**
     * "Users can add attributes to all GraphML elements.".
     * User defined extra attributes, see http://graphml.graphdrawing.org/specification.html, bottom of page
     */
    protected Map<String,String> extraAttrib = new HashMap<>();

}

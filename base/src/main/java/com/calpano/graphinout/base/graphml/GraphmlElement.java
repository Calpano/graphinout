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
     * User defined extra attributes for <key> elements.
     * <p>
     * The name of this attribute in key is <b>key.extra.attrib</b>
     */
    protected Map<String,String> extraAttrib = new HashMap<>();

}

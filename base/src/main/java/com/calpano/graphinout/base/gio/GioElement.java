package com.calpano.graphinout.base.gio;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.HashMap;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public abstract class GioElement {

    /**
     * <a href="http://graphml.graphdrawing.org/specification.html">GraphML</a>
     * "Users can add attributes to all GraphML elements."
     */
    Map<String,String> customAttributes = new HashMap<>();

    public void customAttribute( String attributeName, String attributeValue) {
        customAttributes.put(attributeName, attributeValue);
    }

}

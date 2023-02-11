package com.calpano.graphinout.base.gio;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import javax.annotation.Nullable;
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
    @Nullable Map<String,String> customAttributes;

    public void customAttribute( String attributeName, String attributeValue) {
        if(customAttributes==null) {
            customAttributes = new HashMap<>();
        }
        customAttributes.put(attributeName, attributeValue);
    }

}

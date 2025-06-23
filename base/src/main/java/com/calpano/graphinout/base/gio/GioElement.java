package com.calpano.graphinout.base.gio;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public abstract class GioElement {

    /**
     * <a href="http://graphml.graphdrawing.org/specification.html">GraphML</a>
     * "Users can add attributes to all GraphML elements."
     */
    private @Nullable Map<String,String> customAttributes;

    // Constructor
    public GioElement() {
    }

    public GioElement(@Nullable Map<String, String> customAttributes) {
        this.customAttributes = customAttributes;
    }

    // Getters and Setters
    public @Nullable Map<String, String> getCustomAttributes() {
        return customAttributes;
    }

    public void setCustomAttributes(@Nullable Map<String, String> customAttributes) {
        this.customAttributes = customAttributes;
    }

    // equals, hashCode, toString
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GioElement that = (GioElement) o;
        return Objects.equals(customAttributes, that.customAttributes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(customAttributes);
    }

    @Override
    public String toString() {
        return "GioElement{" +
               "customAttributes=" + customAttributes +
               '}';
    }

    public void customAttribute( String attributeName, String attributeValue) {
        if(customAttributes==null) {
            customAttributes = new HashMap<>();
        }
        customAttributes.put(attributeName, attributeValue);
    }

}
